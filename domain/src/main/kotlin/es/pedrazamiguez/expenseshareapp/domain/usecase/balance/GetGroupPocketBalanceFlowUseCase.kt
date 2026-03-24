package es.pedrazamiguez.expenseshareapp.domain.usecase.balance

import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentStatus
import es.pedrazamiguez.expenseshareapp.domain.model.GroupPocketBalance
import es.pedrazamiguez.expenseshareapp.domain.repository.CashWithdrawalRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.ContributionRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.ExpenseRepository
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseCalculatorService
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Combines contributions, expenses, and cash withdrawals flows for a group
 * to compute the real-time pocket balance.
 *
 * Virtual Balance = Sum(contributions) - Sum(nonCashExpenses.effectiveGroupAmount) - Sum(withdrawals.effectiveDeducted)
 *
 * Cash-paid expenses are excluded from the virtual balance because they are funded
 * from the physical cash pocket (already deducted via withdrawals).
 *
 * Scheduled expenses whose due date is in the future are excluded from both
 * totalExpenses and virtualExpenses. Their groupAmount is reported as
 * scheduledHoldAmount so the UI can show "Available = Balance − Hold".
 *
 * Cash Balances = Map of currency -> Sum(remainingAmount) for each currency with remaining cash.
 *
 * Add-ons (fees, tips, surcharges) on expenses increase their effective group amount.
 * Add-ons (ATM fees) on cash withdrawals increase their effective deducted amount.
 *
 * Total Extras = sum of all non-discount add-ons on expenses (both ON_TOP and INCLUDED)
 * plus ATM fee add-ons on withdrawals. INCLUDED add-ons (e.g., tips embedded in the bill)
 * are surfaced here alongside ON_TOP add-ons so the group sees the full extra-cost picture.
 * Discounts are excluded — they reduce effective amounts but are not extra costs to surface.
 */
class GetGroupPocketBalanceFlowUseCase(
    private val contributionRepository: ContributionRepository,
    private val expenseRepository: ExpenseRepository,
    private val cashWithdrawalRepository: CashWithdrawalRepository,
    private val expenseCalculatorService: ExpenseCalculatorService = ExpenseCalculatorService()
) {
    operator fun invoke(groupId: String, currency: String): Flow<GroupPocketBalance> = combine(
        contributionRepository.getGroupContributionsFlow(groupId),
        expenseRepository.getGroupExpensesFlow(groupId),
        cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId)
    ) { contributions, expenses, withdrawals ->
        val today = LocalDate.now()
        val totalContributions = contributions.sumOf { it.amount }

        // Separate future scheduled expenses (reservations not yet paid).
        // A scheduled expense is "future" when its dueDate is strictly after today.
        val (futureScheduled, effectiveExpenses) = expenses.partition { expense ->
            expense.paymentStatus == PaymentStatus.SCHEDULED &&
                expense.dueDate?.toLocalDate()?.isAfter(today) == true
        }

        val scheduledHoldAmount = futureScheduled.sumOf { expense ->
            expenseCalculatorService.calculateEffectiveGroupAmount(
                expense.groupAmount,
                expense.addOns
            )
        }

        // Precompute effective group amount per expense once to avoid
        // repeated calculateEffectiveGroupAmount() calls across aggregates.
        data class ExpenseAmounts(val base: Long, val effective: Long, val isCash: Boolean, val allExtras: Long)

        val expenseAmounts = effectiveExpenses.map { expense ->
            ExpenseAmounts(
                base = expense.groupAmount,
                effective = expenseCalculatorService.calculateEffectiveGroupAmount(
                    expense.groupAmount,
                    expense.addOns
                ),
                isCash = expense.paymentMethod == PaymentMethod.CASH,
                allExtras = expenseCalculatorService.calculateTotalAddOnExtras(expense.addOns)
            )
        }

        // Total base cost for the UI summary (excludes future scheduled, excludes add-ons).
        // Shows the pure service/goods cost without extras.
        val totalExpenses = expenseAmounts.sumOf { it.base }

        // Only non-cash expenses deduct from the virtual bank account.
        // Cash expenses are funded from the physical cash pocket (already
        // deducted via withdrawals), so including them would double-count.
        // Uses effective amount (base + add-ons) for correct balance math.
        val virtualExpenses = expenseAmounts
            .filter { !it.isCash }
            .sumOf { it.effective }

        // Precompute effective deducted amount per withdrawal once.
        data class WithdrawalAmounts(val effective: Long, val addOnDelta: Long)

        val withdrawalAmounts = withdrawals.map { withdrawal ->
            val effective = expenseCalculatorService.calculateEffectiveDeductedAmount(
                withdrawal.deductedBaseAmount,
                withdrawal.addOns
            )
            WithdrawalAmounts(
                effective = effective,
                addOnDelta = effective - withdrawal.deductedBaseAmount
            )
        }

        // Withdrawals deduct from the virtual pocket via deductedBaseAmount
        // (the amount in the group's base currency that was taken from the pocket).
        // ATM fee add-ons increase the effective deducted amount.
        val totalWithdrawals = withdrawalAmounts.sumOf { it.effective }

        // Compute cash balances: sum remaining amounts per currency,
        // excluding currencies with zero remaining.
        val cashBalances = withdrawals
            .groupBy { it.currency }
            .mapValues { (_, currencyWithdrawals) ->
                currencyWithdrawals.sumOf { it.remainingAmount }
            }
            .filterValues { it > 0 }

        // Compute approximate group-currency equivalent for foreign cash.
        // For each withdrawal, the remaining proportion of deductedBaseAmount is:
        // (remainingAmount / amountWithdrawn) * deductedBaseAmount
        // Uses BigDecimal to avoid floating-point precision issues.
        val cashEquivalents = withdrawals
            .filter { it.currency != currency && it.remainingAmount > 0 && it.amountWithdrawn > 0 }
            .groupBy { it.currency }
            .mapValues { (_, currencyWithdrawals) ->
                currencyWithdrawals.sumOf { w ->
                    BigDecimal(w.remainingAmount)
                        .multiply(BigDecimal(w.deductedBaseAmount))
                        .divide(BigDecimal(w.amountWithdrawn), 0, RoundingMode.HALF_UP)
                        .toLong()
                }
            }

        // Total cash equivalent in the group's base currency:
        // base-currency cash at face value + foreign cash converted proportionally.
        val baseCurrencyCash = cashBalances[currency] ?: 0L
        val foreignCashEquivalent = cashEquivalents.values.sum()
        val totalCashEquivalent = baseCurrencyCash + foreignCashEquivalent

        // Total extras: ALL non-discount add-ons on expenses (both ON_TOP and INCLUDED)
        // plus ATM fee add-ons on withdrawals.
        // INCLUDED add-ons (e.g., tips already in the bill) and ON_TOP add-ons (e.g., ATM fee)
        // are both surfaced here so the group can see the full cost of extras.
        // Discounts are excluded — they are informational and not extra costs to surface.
        val expenseExtras = expenseAmounts.sumOf { it.allExtras }
        val withdrawalExtras = withdrawalAmounts.sumOf { it.addOnDelta }
        val totalExtras = expenseExtras + withdrawalExtras

        GroupPocketBalance(
            totalContributions = totalContributions,
            totalExpenses = totalExpenses,
            virtualBalance = totalContributions - virtualExpenses - totalWithdrawals,
            currency = currency,
            cashBalances = cashBalances,
            cashEquivalents = cashEquivalents,
            totalCashEquivalent = totalCashEquivalent,
            scheduledHoldAmount = scheduledHoldAmount,
            totalExtras = totalExtras
        )
    }
}
