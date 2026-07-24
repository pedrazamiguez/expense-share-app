package es.pedrazamiguez.splittrip.domain.usecase.balance.impl

import es.pedrazamiguez.splittrip.domain.model.CurrencyAmount
import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.domain.model.Settlement
import es.pedrazamiguez.splittrip.domain.model.SettlementPocketType
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.SettlementStatus
import es.pedrazamiguez.splittrip.domain.service.AddOnCalculationService
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
import java.math.RoundingMode

class GetMemberBalancesFlowUseCaseImpl(
    private val addOnCalculationService: AddOnCalculationService
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

        return applyResolvedSettlements(rawBalances, inputs.settlements, inputs.groupCurrency)
    }

    private fun applyResolvedSettlements(
        balances: List<MemberBalance>,
        settlements: List<SettlementRecord>,
        groupCurrency: String
    ): List<MemberBalance> {
        val resolvedSettlements = settlements.filter { it.status == SettlementStatus.RESOLVED }
        if (resolvedSettlements.isEmpty()) return balances

        val balanceMap = balances.associateBy { it.userId }.toMutableMap()

        for (record in resolvedSettlements) {
            applySettlementRecord(balanceMap, record.settlement, groupCurrency)
        }

        return balances.map { balanceMap[it.userId]!! }
    }

    private fun applySettlementRecord(
        balanceMap: MutableMap<String, MemberBalance>,
        settlement: Settlement,
        groupCurrency: String
    ) {
        val fromUser = balanceMap[settlement.fromUserId] ?: return
        val toUser = balanceMap[settlement.toUserId] ?: return

        when (settlement.sourcePocket) {
            SettlementPocketType.POCKET, SettlementPocketType.NET -> {
                applyPocketSettlement(balanceMap, settlement, fromUser)
            }
            SettlementPocketType.CASH -> {
                applyCashSettlement(balanceMap, settlement, fromUser, toUser, groupCurrency)
            }
        }
    }

    private fun applyPocketSettlement(
        balanceMap: MutableMap<String, MemberBalance>,
        settlement: Settlement,
        fromUser: MemberBalance
    ) {
        val isAlreadyMaterialized = fromUser.contributed >= settlement.amount
        if (!isAlreadyMaterialized) {
            balanceMap[settlement.fromUserId] = fromUser.copy(
                contributed = fromUser.contributed + settlement.amount,
                pocketBalance = fromUser.pocketBalance + settlement.amount
            )
        }
    }

    private fun applyCashSettlement(
        balanceMap: MutableMap<String, MemberBalance>,
        settlement: Settlement,
        fromUser: MemberBalance,
        toUser: MemberBalance,
        groupCurrency: String
    ) {
        val amount = settlement.amount
        val currency = settlement.currency

        val fromUserEquiv = getEquivalentCents(amount, currency, fromUser, groupCurrency)
        val toUserEquiv = getEquivalentCents(amount, currency, toUser, groupCurrency)

        val fromCashInHandByCurrency = fromUser.cashInHandByCurrency.map {
            if (it.currency == currency) {
                it.copy(
                    amountCents = it.amountCents - amount,
                    equivalentCents = it.equivalentCents - fromUserEquiv
                )
            } else {
                it
            }
        }
        balanceMap[settlement.fromUserId] = fromUser.copy(
            cashSpent = fromUser.cashSpent + fromUserEquiv,
            cashInHand = fromUser.cashInHand - fromUserEquiv,
            cashInHandByCurrency = fromCashInHandByCurrency
        )

        val toCashInHandByCurrency = toUser.cashInHandByCurrency.map {
            if (it.currency == currency) {
                it.copy(
                    amountCents = it.amountCents + amount,
                    equivalentCents = it.equivalentCents + toUserEquiv
                )
            } else {
                it
            }
        }
        val finalToCashInHandByCurrency = if (toUser.cashInHandByCurrency.none { it.currency == currency }) {
            toUser.cashInHandByCurrency + CurrencyAmount(
                currency = currency,
                amountCents = amount,
                equivalentCents = toUserEquiv
            )
        } else {
            toCashInHandByCurrency
        }

        balanceMap[settlement.toUserId] = toUser.copy(
            contributed = toUser.contributed + toUserEquiv,
            withdrawn = toUser.withdrawn + toUserEquiv,
            cashInHand = toUser.cashInHand + toUserEquiv,
            cashInHandByCurrency = finalToCashInHandByCurrency
        )
    }

    private fun getEquivalentCents(
        amount: Long,
        currency: String,
        user: MemberBalance,
        groupCurrency: String
    ): Long {
        if (currency == groupCurrency || currency.isEmpty()) {
            return amount
        }
        val currencyAttr = user.cashInHandByCurrency.find { it.currency == currency }
        return if (currencyAttr != null && currencyAttr.amountCents != 0L) {
            BigDecimal(amount)
                .multiply(BigDecimal(currencyAttr.equivalentCents))
                .divide(BigDecimal(currencyAttr.amountCents), 0, RoundingMode.HALF_UP)
                .toLong()
        } else {
            amount
        }
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
