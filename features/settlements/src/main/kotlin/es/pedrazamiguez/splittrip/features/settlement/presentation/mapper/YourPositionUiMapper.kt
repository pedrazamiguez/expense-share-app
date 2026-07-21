package es.pedrazamiguez.splittrip.features.settlement.presentation.mapper

import es.pedrazamiguez.splittrip.core.common.provider.LocaleProvider
import es.pedrazamiguez.splittrip.core.designsystem.presentation.formatter.formatCurrencyAmount
import es.pedrazamiguez.splittrip.domain.model.CurrencyAmount
import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.CurrencyBreakdownUiModel
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.NetPositionStatus
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.PersonalPositionUiModel
import java.util.Locale
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

class YourPositionUiMapper(
    private val localeProvider: LocaleProvider
) {
    companion object {
        internal const val EM_DASH = "\u2014"
    }

    fun toPersonalPosition(
        memberBalance: MemberBalance,
        groupCurrencyCode: String
    ): PersonalPositionUiModel {
        val locale = localeProvider.getCurrentLocale()
        val isNegativeCash = memberBalance.cashInHand < 0

        return PersonalPositionUiModel(
            groupCurrencyCode = groupCurrencyCode,
            formattedNetPosition = formatAmount(memberBalance.totalBalance, groupCurrencyCode, locale),
            netPositionStatus = resolveNetPositionStatus(memberBalance.totalBalance),
            formattedPocketBalance = formatAmount(memberBalance.pocketBalance, groupCurrencyCode, locale),
            formattedCashInHand = if (isNegativeCash) {
                EM_DASH
            } else {
                formatAmount(
                    memberBalance.cashInHand,
                    groupCurrencyCode,
                    locale
                )
            },
            hasNegativeCashInHand = isNegativeCash,
            formattedTotalContributed = formatAmount(memberBalance.contributed, groupCurrencyCode, locale),
            formattedTotalSpent = formatAmount(memberBalance.totalSpent, groupCurrencyCode, locale),
            formattedCashSpent = formatAmount(memberBalance.cashSpent, groupCurrencyCode, locale),
            formattedNonCashSpent = formatAmount(memberBalance.nonCashSpent, groupCurrencyCode, locale),
            formattedRefundableSpent = if (memberBalance.refundableSpent > 0L) {
                formatAmount(memberBalance.refundableSpent, groupCurrencyCode, locale)
            } else {
                null
            },
            cashInHandByCurrency = mapCurrencyBreakdowns(memberBalance.cashInHandByCurrency, groupCurrencyCode, locale),
            cashSpentByCurrency = mapCurrencyBreakdowns(memberBalance.cashSpentByCurrency, groupCurrencyCode, locale),
            nonCashSpentByCurrency = mapCurrencyBreakdowns(
                memberBalance.nonCashSpentByCurrency,
                groupCurrencyCode,
                locale
            )
        )
    }

    private fun formatAmount(amount: Long, currencyCode: String, locale: Locale): String {
        return formatCurrencyAmount(amount = amount, currencyCode = currencyCode, locale = locale)
    }

    private fun resolveNetPositionStatus(totalBalance: Long): NetPositionStatus = when {
        totalBalance > 0 -> NetPositionStatus.POSITIVE
        totalBalance < 0 -> NetPositionStatus.NEGATIVE
        else -> NetPositionStatus.NEUTRAL
    }

    private fun mapCurrencyBreakdowns(
        amounts: List<CurrencyAmount>,
        groupCurrency: String,
        locale: Locale
    ): ImmutableList<CurrencyBreakdownUiModel> {
        return amounts.map { ca ->
            CurrencyBreakdownUiModel(
                currency = ca.currency,
                formattedAmount = formatCurrencyAmount(ca.amountCents, ca.currency, locale),
                formattedEquivalent = if (ca.currency != groupCurrency && ca.equivalentCents > 0) {
                    formatCurrencyAmount(ca.equivalentCents, groupCurrency, locale)
                } else {
                    ""
                }
            )
        }.toImmutableList()
    }
}
