package es.pedrazamiguez.splittrip.domain.service.impl

import es.pedrazamiguez.splittrip.domain.model.CashTransfer
import es.pedrazamiguez.splittrip.domain.model.CashWithdrawal
import es.pedrazamiguez.splittrip.domain.model.Contribution
import es.pedrazamiguez.splittrip.domain.model.CurrencyAmount
import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.SettlementStatus
import es.pedrazamiguez.splittrip.domain.service.SettlementReconciliationService
import es.pedrazamiguez.splittrip.domain.service.settlement.PocketSettlementReconciliationStrategy
import java.math.BigDecimal
import java.math.RoundingMode

class SettlementReconciliationServiceImpl : SettlementReconciliationService {

    private val strategies = listOf(
        PocketSettlementReconciliationStrategy()
    )

    override fun applyResolvedSettlements(
        balances: List<MemberBalance>,
        settlements: List<SettlementRecord>,
        cashTransfers: List<CashTransfer>,
        contributions: List<Contribution>,
        withdrawals: List<CashWithdrawal>,
        groupCurrency: String
    ): List<MemberBalance> {
        val balanceMap = balances.associateBy { it.userId }.toMutableMap()

        // 1. Apply all cash transfers (event-sourced ledger)
        val sortedTransfers = cashTransfers.sortedBy { it.createdAt }
        for (transfer in sortedTransfers) {
            applyCashTransfer(balanceMap, transfer, groupCurrency)
        }

        // 2. Apply resolved non-cash settlements
        val resolvedSettlements = settlements.filter { it.status == SettlementStatus.RESOLVED }
        for (record in resolvedSettlements) {
            applySettlementRecord(balanceMap, record, contributions, withdrawals, groupCurrency)
        }

        return balances.map { balanceMap[it.userId]!! }
    }

    private fun applyCashTransfer(
        balanceMap: MutableMap<String, MemberBalance>,
        transfer: CashTransfer,
        groupCurrency: String
    ) {
        val fromUser = balanceMap[transfer.fromUserId] ?: return
        val toUser = balanceMap[transfer.toUserId] ?: return

        val amount = transfer.amountCents
        val currency = transfer.currency

        val fromUserEquiv = getEquivalentCents(amount, currency, fromUser, groupCurrency)
        val toUserEquiv = getEquivalentCents(amount, currency, toUser, groupCurrency)

        val fromCashInHandByCurrency = updateCurrencyAmountList(
            list = fromUser.cashInHandByCurrency,
            currency = currency,
            amount = -amount,
            equivalent = -fromUserEquiv,
            addIfMissing = true
        )

        val fromWithdrawnByCurrency = updateCurrencyAmountList(
            list = fromUser.withdrawnByCurrency,
            currency = currency,
            amount = amount,
            equivalent = fromUserEquiv,
            addIfMissing = true
        )

        balanceMap[transfer.fromUserId] = fromUser.copy(
            withdrawn = fromUser.withdrawn + fromUserEquiv,
            cashInHand = fromUser.cashInHand - fromUserEquiv,
            withdrawnByCurrency = fromWithdrawnByCurrency,
            cashInHandByCurrency = fromCashInHandByCurrency
        )

        val finalToCashInHandByCurrency = updateCurrencyAmountList(
            list = toUser.cashInHandByCurrency,
            currency = currency,
            amount = amount,
            equivalent = toUserEquiv,
            addIfMissing = true
        )
        val toWithdrawnByCurrency = updateCurrencyAmountList(
            list = toUser.withdrawnByCurrency,
            currency = currency,
            amount = -amount,
            equivalent = -toUserEquiv,
            addIfMissing = true
        )

        balanceMap[transfer.toUserId] = toUser.copy(
            withdrawn = toUser.withdrawn - toUserEquiv,
            cashInHand = toUser.cashInHand + toUserEquiv,
            cashInHandByCurrency = finalToCashInHandByCurrency,
            withdrawnByCurrency = toWithdrawnByCurrency
        )
    }

    private fun updateCurrencyAmountList(
        list: List<CurrencyAmount>,
        currency: String,
        amount: Long,
        equivalent: Long,
        addIfMissing: Boolean
    ): List<CurrencyAmount> {
        val updated = list.map {
            if (it.currency == currency) {
                it.copy(
                    amountCents = it.amountCents + amount,
                    equivalentCents = it.equivalentCents + equivalent
                )
            } else {
                it
            }
        }
        return if (addIfMissing && updated.none { it.currency == currency }) {
            updated + CurrencyAmount(currency, amount, equivalent)
        } else {
            updated
        }
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

        val userCurrencyAmount = user.cashInHandByCurrency.find { it.currency == currency }
        return if (userCurrencyAmount != null && userCurrencyAmount.amountCents > 0L) {
            val rate = BigDecimal(userCurrencyAmount.equivalentCents).divide(
                BigDecimal(userCurrencyAmount.amountCents),
                10,
                RoundingMode.HALF_UP
            )
            BigDecimal(amount).multiply(rate).setScale(0, RoundingMode.HALF_UP).toLong()
        } else {
            amount
        }
    }

    private fun applySettlementRecord(
        balanceMap: MutableMap<String, MemberBalance>,
        record: SettlementRecord,
        contributions: List<Contribution>,
        withdrawals: List<CashWithdrawal>,
        groupCurrency: String
    ) {
        val fromUser = balanceMap[record.settlement.fromUserId] ?: return
        val toUser = balanceMap[record.settlement.toUserId] ?: return

        val strategy = strategies.firstOrNull { it.appliesTo(record.settlement.sourcePocket) }
        strategy?.apply(balanceMap, record, fromUser, toUser, contributions, withdrawals, groupCurrency)
    }
}
