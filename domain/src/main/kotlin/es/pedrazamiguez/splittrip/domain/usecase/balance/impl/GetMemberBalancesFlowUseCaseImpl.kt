package es.pedrazamiguez.splittrip.domain.usecase.balance.impl

import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.domain.service.AddOnCalculationService
import es.pedrazamiguez.splittrip.domain.service.SettlementReconciliationService
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetMemberBalancesFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.support.ExpenseResult
import es.pedrazamiguez.splittrip.domain.usecase.balance.support.MemberBalanceCalculationInputs
import es.pedrazamiguez.splittrip.domain.usecase.balance.support.RemainingResult
import es.pedrazamiguez.splittrip.domain.usecase.balance.support.WithdrawalResult
import es.pedrazamiguez.splittrip.domain.usecase.balance.support.attributeExpensesByPaymentMethod
import es.pedrazamiguez.splittrip.domain.usecase.balance.support.attributeRemainingByScope
import es.pedrazamiguez.splittrip.domain.usecase.balance.support.attributeWithdrawals
import es.pedrazamiguez.splittrip.domain.usecase.balance.support.balanceDistributeByShares
import es.pedrazamiguez.splittrip.domain.usecase.balance.support.balanceDistributeEvenly
import es.pedrazamiguez.splittrip.domain.usecase.balance.support.buildCashInHandByCurrency
import es.pedrazamiguez.splittrip.domain.usecase.balance.support.buildCurrencyAmountList
import es.pedrazamiguez.splittrip.domain.usecase.balance.support.buildWithdrawnByCurrency
import java.math.BigDecimal
class GetMemberBalancesFlowUseCaseImpl(
    private val addOnCalculationService: AddOnCalculationService,
    private val settlementReconciliationService: SettlementReconciliationService
) : GetMemberBalancesFlowUseCase {

    override fun computeMemberBalances(
        inputs: MemberBalanceCalculationInputs
    ): List<MemberBalance> {
        val subunitMap = inputs.subunits.associateBy { it.id }

        val contributedMap = inputs.attributionStrategy.attribute(
            contributions = inputs.contributions,
            subunitMap = subunitMap,
            groupMemberIds = inputs.groupMemberIds
        )
        val withdrawalResult = attributeWithdrawals(
            inputs.withdrawals,
            subunitMap,
            inputs.groupMemberIds,
            addOnCalculationService
        )
        val remainingResult = attributeRemainingByScope(
            inputs.withdrawals,
            subunitMap,
            inputs.groupMemberIds
        )
        val expenseResult = attributeExpensesByPaymentMethod(
            inputs.expenses,
            addOnCalculationService
        )

        val allUserIds = buildSet {
            addAll(inputs.groupMemberIds)
            addAll(contributedMap.keys)
            addAll(withdrawalResult.groupCurrencyMap.keys)
            addAll(expenseResult.cashSpentMap.keys)
            addAll(expenseResult.nonCashSpentMap.keys)
            addAll(expenseResult.refundableSpentMap.keys)
        }

        val rawBalances = allUserIds.map { userId ->
            buildMemberBalance(
                userId = userId,
                contributedMap = contributedMap,
                withdrawalResult = withdrawalResult,
                remainingResult = remainingResult,
                expenseResult = expenseResult,
                groupCurrency = inputs.groupCurrency
            )
        }

        return settlementReconciliationService.applyResolvedSettlements(
            balances = rawBalances,
            settlements = inputs.settlements,
            groupCurrency = inputs.groupCurrency
        )
    }

    private fun buildMemberBalance(
        userId: String,
        contributedMap: Map<String, Long>,
        withdrawalResult: WithdrawalResult,
        remainingResult: RemainingResult,
        expenseResult: ExpenseResult,
        groupCurrency: String
    ): MemberBalance {
        val contributed = contributedMap.getOrDefault(userId, 0L)
        val withdrawn = withdrawalResult.groupCurrencyMap.getOrDefault(userId, 0L)
        val cashSpent = expenseResult.cashSpentMap.getOrDefault(userId, 0L)
        val nonCashSpent = expenseResult.nonCashSpentMap.getOrDefault(userId, 0L)
        val refundableSpent = expenseResult.refundableSpentMap.getOrDefault(userId, 0L)

        return MemberBalance(
            userId = userId,
            contributed = contributed,
            withdrawn = withdrawn,
            cashSpent = cashSpent,
            nonCashSpent = nonCashSpent,
            refundableSpent = refundableSpent,
            totalSpent = cashSpent + nonCashSpent,
            pocketBalance = contributed - withdrawn - nonCashSpent,
            cashInHand = remainingResult.groupCurrencyMap.getOrDefault(userId, 0L),
            cashInHandByCurrency = buildCashInHandByCurrency(
                remainingByCurrency = remainingResult.byCurrency.getOrDefault(userId, emptyMap()),
                groupCurrency = groupCurrency
            ),
            cashSpentByCurrency = buildCurrencyAmountList(
                byCurrencyMap = expenseResult.cashSpentByCurrency.getOrDefault(userId, emptyMap()),
                equivByCurrency = expenseResult.cashEquivByCurrency.getOrDefault(userId, emptyMap()),
                groupCurrency = groupCurrency
            ),
            nonCashSpentByCurrency = buildCurrencyAmountList(
                byCurrencyMap = expenseResult.nonCashSpentByCurrency.getOrDefault(userId, emptyMap()),
                equivByCurrency = expenseResult.nonCashEquivByCurrency.getOrDefault(userId, emptyMap()),
                groupCurrency = groupCurrency
            ),
            refundableSpentByCurrency = buildCurrencyAmountList(
                byCurrencyMap = expenseResult.refundableSpentByCurrency.getOrDefault(userId, emptyMap()),
                equivByCurrency = expenseResult.refundableEquivByCurrency.getOrDefault(userId, emptyMap()),
                groupCurrency = groupCurrency
            ),
            withdrawnByCurrency = buildWithdrawnByCurrency(
                byCurrencyMap = withdrawalResult.byCurrency.getOrDefault(userId, emptyMap()),
                groupCurrency = groupCurrency
            )
        )
    }

    companion object {
        /** Delegates to [balanceDistributeByShares] — accessible via `GetMemberBalancesFlowUseCase.distributeByShares(...)`. */
        internal fun distributeByShares(
            totalAmount: Long,
            memberShares: Map<String, BigDecimal>
        ): Map<String, Long> = balanceDistributeByShares(totalAmount, memberShares)

        /** Delegates to [balanceDistributeEvenly] — accessible via `GetMemberBalancesFlowUseCase.distributeEvenly(...)`. */
        internal fun distributeEvenly(
            totalAmount: Long,
            memberIds: List<String>
        ): Map<String, Long> = balanceDistributeEvenly(totalAmount, memberIds)
    }
}
