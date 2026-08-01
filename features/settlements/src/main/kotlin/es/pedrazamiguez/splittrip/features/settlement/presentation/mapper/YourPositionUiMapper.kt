package es.pedrazamiguez.splittrip.features.settlement.presentation.mapper

import es.pedrazamiguez.splittrip.core.common.provider.LocaleProvider
import es.pedrazamiguez.splittrip.core.common.provider.ResourceProvider
import es.pedrazamiguez.splittrip.core.designsystem.presentation.formatter.formatCurrencyAmount
import es.pedrazamiguez.splittrip.core.designsystem.presentation.formatter.formatForDisplay
import es.pedrazamiguez.splittrip.core.designsystem.presentation.formatter.formatShortDate
import es.pedrazamiguez.splittrip.core.designsystem.presentation.model.CashBreakdownItemUiModel
import es.pedrazamiguez.splittrip.domain.enums.AddOnType
import es.pedrazamiguez.splittrip.domain.enums.PayerType
import es.pedrazamiguez.splittrip.domain.model.CashWithdrawal
import es.pedrazamiguez.splittrip.domain.model.CurrencyAmount
import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.domain.model.Subunit
import es.pedrazamiguez.splittrip.features.settlement.R
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.CurrencyBreakdownUiModel
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.NetPositionStatus
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.PersonalPositionUiModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

class YourPositionUiMapper(
    private val localeProvider: LocaleProvider,
    private val resourceProvider: ResourceProvider
) {
    companion object {
        internal const val EM_DASH = "\u2014"
    }

    fun toPersonalPosition(
        memberBalance: MemberBalance,
        groupCurrencyCode: String,
        withdrawals: List<CashWithdrawal> = emptyList(),
        subunitsMap: Map<String, Subunit> = emptyMap(),
        groupMemberIds: List<String> = emptyList()
    ): PersonalPositionUiModel {
        val locale = localeProvider.getCurrentLocale()
        val isNegativeCash = memberBalance.cashInHand < 0

        val formattedTotalFees = computeFormattedTotalFees(
            userId = memberBalance.userId,
            isNegativeCash = isNegativeCash,
            withdrawals = withdrawals,
            subunitsMap = subunitsMap,
            groupMemberIds = groupMemberIds,
            groupCurrency = groupCurrencyCode,
            locale = locale
        )

        val cashBreakdown = resolveCashBreakdown(
            memberBalance = memberBalance,
            isNegativeCash = isNegativeCash,
            withdrawals = withdrawals,
            subunitsMap = subunitsMap,
            groupMemberIds = groupMemberIds,
            groupCurrencyCode = groupCurrencyCode,
            locale = locale
        )

        return buildPersonalPositionUiModel(
            memberBalance = memberBalance,
            groupCurrencyCode = groupCurrencyCode,
            isNegativeCash = isNegativeCash,
            formattedTotalFees = formattedTotalFees,
            cashBreakdown = cashBreakdown,
            locale = locale
        )
    }

    private fun resolveCashBreakdown(
        memberBalance: MemberBalance,
        isNegativeCash: Boolean,
        withdrawals: List<CashWithdrawal>,
        subunitsMap: Map<String, Subunit>,
        groupMemberIds: List<String>,
        groupCurrencyCode: String,
        locale: Locale
    ): ImmutableList<CashBreakdownItemUiModel> {
        if (isNegativeCash) return persistentListOf()
        return mapCashBreakdown(
            userId = memberBalance.userId,
            withdrawals = withdrawals,
            subunitsMap = subunitsMap,
            groupMemberIds = groupMemberIds,
            groupCurrency = groupCurrencyCode,
            locale = locale
        )
    }

    private fun buildPersonalPositionUiModel(
        memberBalance: MemberBalance,
        groupCurrencyCode: String,
        isNegativeCash: Boolean,
        formattedTotalFees: String?,
        cashBreakdown: ImmutableList<CashBreakdownItemUiModel>,
        locale: Locale
    ): PersonalPositionUiModel {
        val formattedCashInHand = if (isNegativeCash) {
            EM_DASH
        } else {
            formatAmount(memberBalance.cashInHand, groupCurrencyCode, locale)
        }

        return PersonalPositionUiModel(
            groupCurrencyCode = groupCurrencyCode,
            formattedNetPosition = formatAmount(memberBalance.totalBalance, groupCurrencyCode, locale),
            netPositionStatus = resolveNetPositionStatus(memberBalance.totalBalance),
            formattedPocketBalance = formatAmount(memberBalance.pocketBalance, groupCurrencyCode, locale),
            formattedCashInHand = formattedCashInHand,
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
            formattedTotalFees = formattedTotalFees,
            cashInHandByCurrency = mapCurrencyBreakdowns(memberBalance.cashInHandByCurrency, groupCurrencyCode, locale),
            cashSpentByCurrency = mapCurrencyBreakdowns(memberBalance.cashSpentByCurrency, groupCurrencyCode, locale),
            nonCashSpentByCurrency = mapCurrencyBreakdowns(
                memberBalance.nonCashSpentByCurrency,
                groupCurrencyCode,
                locale
            ),
            refundableSpentByCurrency = mapCurrencyBreakdowns(
                memberBalance.refundableSpentByCurrency,
                groupCurrencyCode,
                locale
            ),
            cashBreakdown = cashBreakdown
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

    private fun computeFormattedTotalFees(
        userId: String,
        isNegativeCash: Boolean,
        withdrawals: List<CashWithdrawal>,
        subunitsMap: Map<String, Subunit>,
        groupMemberIds: List<String>,
        groupCurrency: String,
        locale: Locale
    ): String? {
        if (isNegativeCash) return null
        val totalFeeCents = computeMemberTotalFees(
            userId = userId,
            withdrawals = withdrawals,
            groupMemberIds = groupMemberIds,
            subunitsMap = subunitsMap
        )
        return if (totalFeeCents > 0L) {
            formatCurrencyAmount(totalFeeCents, groupCurrency, locale)
        } else {
            null
        }
    }

    private fun mapCashBreakdown(
        userId: String,
        withdrawals: List<CashWithdrawal>,
        subunitsMap: Map<String, Subunit>,
        groupMemberIds: List<String>,
        groupCurrency: String,
        locale: Locale
    ): ImmutableList<CashBreakdownItemUiModel> {
        val scopeOrder = mapOf(PayerType.GROUP to 0, PayerType.SUBUNIT to 1, PayerType.USER to 2)
        return withdrawals
            .sortedWith(
                compareBy<CashWithdrawal> { scopeOrder[it.withdrawalScope] ?: 3 }
                    .thenByDescending { it.createdAt }
            )
            .mapNotNull { withdrawal ->
                if (withdrawal.amountWithdrawn == 0L || withdrawal.remainingAmount <= 0L) return@mapNotNull null
                val nativeShare = computeUserNativeShare(withdrawal, userId, groupMemberIds, subunitsMap)
                if (nativeShare <= 0L) return@mapNotNull null
                buildCashBreakdownEntry(
                    withdrawal = withdrawal,
                    nativeShare = nativeShare,
                    userId = userId,
                    groupMemberIds = groupMemberIds,
                    groupCurrency = groupCurrency,
                    locale = locale,
                    subunitsMap = subunitsMap
                )
            }
            .toImmutableList()
    }

    private fun buildCashBreakdownEntry(
        withdrawal: CashWithdrawal,
        nativeShare: Long,
        userId: String,
        groupMemberIds: List<String>,
        groupCurrency: String,
        locale: Locale,
        subunitsMap: Map<String, Subunit>
    ): CashBreakdownItemUiModel {
        val groupEquivalent = BigDecimal(nativeShare)
            .multiply(BigDecimal(withdrawal.deductedBaseAmount))
            .divide(BigDecimal(withdrawal.amountWithdrawn), 0, RoundingMode.HALF_UP)
            .toLong()
        val isForeign = withdrawal.currency != groupCurrency
        val dateText = withdrawal.createdAt?.formatShortDate(locale) ?: ""
        val label = if (withdrawal.title.isNullOrBlank()) {
            resourceProvider.getString(R.string.your_position_cash_breakdown_atm_fallback, dateText)
        } else {
            withdrawal.title ?: ""
        }
        return CashBreakdownItemUiModel(
            withdrawalLabel = label,
            dateText = dateText,
            formattedRate = if (isForeign) {
                resourceProvider.getString(
                    R.string.your_position_cash_breakdown_rate,
                    withdrawal.exchangeRate.formatForDisplay(locale, maxDecimalPlaces = 6),
                    withdrawal.currency,
                    groupCurrency
                )
            } else {
                ""
            },
            formattedNativeRemaining = formatCurrencyAmount(nativeShare, withdrawal.currency, locale),
            formattedEquivalent = if (isForeign) {
                formatCurrencyAmount(groupEquivalent, groupCurrency, locale)
            } else {
                ""
            },
            scopeLabel = when (withdrawal.withdrawalScope) {
                PayerType.GROUP -> resourceProvider.getString(R.string.your_position_cash_breakdown_group_scope)
                PayerType.USER -> resourceProvider.getString(R.string.your_position_cash_breakdown_personal_scope)
                PayerType.SUBUNIT -> withdrawal.subunitId?.let { subunitsMap[it]?.name }
                    ?: resourceProvider.getString(R.string.your_position_cash_breakdown_unknown_subunit)
            },
            isEstimatedShare = withdrawal.withdrawalScope == PayerType.GROUP,
            formattedAddOns = formatWithdrawalAddOns(
                withdrawal = withdrawal,
                userId = userId,
                groupMemberIds = groupMemberIds,
                subunitsMap = subunitsMap,
                groupCurrency = groupCurrency,
                locale = locale
            )
        )
    }

    private fun formatWithdrawalAddOns(
        withdrawal: CashWithdrawal,
        userId: String,
        groupMemberIds: List<String>,
        subunitsMap: Map<String, Subunit>,
        groupCurrency: String,
        locale: Locale
    ): String {
        val nonDiscountAddOns = withdrawal.addOns.filter { it.type != AddOnType.DISCOUNT }
        if (nonDiscountAddOns.isEmpty()) return ""
        val subunit = withdrawal.subunitId?.let { subunitsMap[it] }
        val totalUserAddOnCents = nonDiscountAddOns.sumOf { addOn ->
            computeAddOnShare(
                addOn = addOn,
                scope = withdrawal.withdrawalScope,
                withdrawnBy = withdrawal.withdrawnBy,
                userId = userId,
                groupMemberIds = groupMemberIds,
                subunit = subunit
            )
        }
        if (totalUserAddOnCents <= 0L) return ""
        return formatCurrencyAmount(totalUserAddOnCents, groupCurrency, locale)
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
