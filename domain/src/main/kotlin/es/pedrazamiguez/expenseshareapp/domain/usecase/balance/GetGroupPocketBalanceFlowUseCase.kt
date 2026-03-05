package es.pedrazamiguez.expenseshareapp.domain.usecase.balance

import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.model.GroupPocketBalance
import es.pedrazamiguez.expenseshareapp.domain.repository.CashWithdrawalRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.ContributionRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Combines contributions, expenses, and cash withdrawals flows for a group
 * to compute the real-time pocket balance.
 *
 * Virtual Balance = Sum(contributions) - Sum(nonCashExpenses.groupAmount) - Sum(withdrawals.deductedBaseAmount)
 *
 * Cash-paid expenses are excluded from the virtual balance because they are funded
 * from the physical cash pocket (already deducted via withdrawals).
 *
 * Cash Balances = Map of currency -> Sum(remainingAmount) for each currency with remaining cash.
 */
class GetGroupPocketBalanceFlowUseCase(
    private val contributionRepository: ContributionRepository,
    private val expenseRepository: ExpenseRepository,
    private val cashWithdrawalRepository: CashWithdrawalRepository
) {
    operator fun invoke(groupId: String, currency: String): Flow<GroupPocketBalance> =
        combine(
            contributionRepository.getGroupContributionsFlow(groupId),
            expenseRepository.getGroupExpensesFlow(groupId),
            cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId)
        ) { contributions, expenses, withdrawals ->
            val totalContributions = contributions.sumOf { it.amount }

            // Total spent for the UI summary (includes ALL expenses)
            val totalExpenses = expenses.sumOf { it.groupAmount }

            // Only non-cash expenses deduct from the virtual bank account.
            // Cash expenses are funded from the physical cash pocket (already
            // deducted via withdrawals), so including them would double-count.
            val virtualExpenses = expenses
                .filter { it.paymentMethod != PaymentMethod.CASH }
                .sumOf { it.groupAmount }

            // Withdrawals deduct from the virtual pocket via deductedBaseAmount
            // (the amount in the group's base currency that was taken from the pocket).
            val totalWithdrawals = withdrawals.sumOf { it.deductedBaseAmount }

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
            val cashEquivalents = withdrawals
                .filter { it.currency != currency && it.remainingAmount > 0 && it.amountWithdrawn > 0 }
                .groupBy { it.currency }
                .mapValues { (_, currencyWithdrawals) ->
                    currencyWithdrawals.sumOf { w ->
                        (w.remainingAmount.toDouble() / w.amountWithdrawn.toDouble() * w.deductedBaseAmount).toLong()
                    }
                }

            // Total cash equivalent in the group's base currency:
            // base-currency cash at face value + foreign cash converted proportionally.
            val baseCurrencyCash = cashBalances[currency] ?: 0L
            val foreignCashEquivalent = cashEquivalents.values.sum()
            val totalCashEquivalent = baseCurrencyCash + foreignCashEquivalent

            GroupPocketBalance(
                totalContributions = totalContributions,
                totalExpenses = totalExpenses,
                virtualBalance = totalContributions - virtualExpenses - totalWithdrawals,
                currency = currency,
                cashBalances = cashBalances,
                cashEquivalents = cashEquivalents,
                totalCashEquivalent = totalCashEquivalent
            )
        }
}

