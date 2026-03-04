package es.pedrazamiguez.expenseshareapp.features.balance.presentation.mapper

import es.pedrazamiguez.expenseshareapp.core.common.constant.AppConstants
import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.common.provider.ResourceProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.formatCurrencyAmount
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.formatShortDate
import es.pedrazamiguez.expenseshareapp.domain.converter.CurrencyConverter
import es.pedrazamiguez.expenseshareapp.domain.model.Contribution
import es.pedrazamiguez.expenseshareapp.domain.model.GroupPocketBalance
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.ContributionUiModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.GroupPocketBalanceUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Currency

class BalancesUiMapper(
    private val localeProvider: LocaleProvider,
    private val resourceProvider: ResourceProvider
) {

    fun mapBalance(balance: GroupPocketBalance, groupName: String): GroupPocketBalanceUiModel {
        val locale = localeProvider.getCurrentLocale()
        return GroupPocketBalanceUiModel(
            groupName = groupName,
            formattedBalance = formatCurrencyAmount(balance.balance, balance.currency, locale),
            formattedTotalContributed = formatCurrencyAmount(
                balance.totalContributions,
                balance.currency,
                locale
            ),
            formattedTotalSpent = formatCurrencyAmount(
                balance.totalExpenses,
                balance.currency,
                locale
            ),
            currency = balance.currency
        )
    }

    fun mapContributions(contributions: List<Contribution>): ImmutableList<ContributionUiModel> {
        val locale = localeProvider.getCurrentLocale()
        return contributions.map { contribution ->
            ContributionUiModel(
                id = contribution.id,
                userId = contribution.userId,
                formattedAmount = formatCurrencyAmount(
                    contribution.amount,
                    contribution.currency,
                    locale
                ),
                dateText = contribution.createdAt?.formatShortDate(locale) ?: ""
            )
        }.toImmutableList()
    }

    /**
     * Parses a user-entered amount string to the currency's smallest unit (e.g., cents for EUR,
     * yen for JPY, millimes for TND).
     *
     * Correctly handles:
     * - Any currency's decimal places (0 for JPY, 2 for EUR/USD, 3 for KWD/TND)
     * - Locale-specific separators via [CurrencyConverter.normalizeAmountString]
     * - Deterministic rounding via [RoundingMode.HALF_UP] (no silent truncation)
     *
     * Examples:
     * - "25.50" with EUR (2 decimals) → 2550
     * - "1000"  with JPY (0 decimals) → 1000
     * - "10.500" with TND (3 decimals) → 10500
     * - "1.999" with EUR (2 decimals) → 200 (rounds, never truncates)
     *
     * @param amountString The raw user input (may use locale-specific separators)
     * @param currencyCode ISO 4217 currency code used to determine decimal places
     * @return Amount in the currency's smallest unit, or 0 if input is unparseable
     */
    fun parseAmountToSmallestUnit(amountString: String, currencyCode: String): Long {
        val normalizedString = CurrencyConverter.normalizeAmountString(amountString.trim())
        val amount = normalizedString.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val decimalPlaces = runCatching {
            Currency.getInstance(currencyCode).defaultFractionDigits
        }.getOrElse {
            Currency.getInstance(AppConstants.DEFAULT_CURRENCY_CODE).defaultFractionDigits
        }
        val multiplier = BigDecimal.TEN.pow(decimalPlaces)
        return amount.multiply(multiplier).setScale(0, RoundingMode.HALF_UP).toLong()
    }
}

