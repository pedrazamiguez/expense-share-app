package es.pedrazamiguez.splittrip.domain.service.settlement

import es.pedrazamiguez.splittrip.domain.model.CurrencyAmount
import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.domain.model.Settlement
import es.pedrazamiguez.splittrip.domain.model.SettlementPocketType
import java.math.BigDecimal
import java.math.RoundingMode

class CashSettlementReconciliationStrategy : SettlementReconciliationStrategy {

    override fun appliesTo(sourcePocket: SettlementPocketType): Boolean {
        return sourcePocket == SettlementPocketType.CASH
    }

    override fun apply(
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

        balanceMap[settlement.fromUserId] = fromUser.copy(
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

        balanceMap[settlement.toUserId] = toUser.copy(
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
}
