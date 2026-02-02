package es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper

import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.formatNumberForDisplay
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.formatRateForDisplay
import es.pedrazamiguez.expenseshareapp.domain.converter.CurrencyConverter
import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState
import java.math.BigDecimal
import java.math.RoundingMode

class AddExpenseUiMapper(
    private val localeProvider: LocaleProvider
) {

    companion object {
        private const val RATE_PRECISION = 6
    }

    /**
     * Formats an internal number string (dot decimal) to locale-aware display format.
     *
     * @param internalValue The number in internal format (e.g., "200.08")
     * @param maxDecimalPlaces Maximum decimal places to display
     * @param minDecimalPlaces Minimum decimal places to display (pads with zeros if needed).
     *                         Use this to respect currency decimal digits (e.g., 2 for EUR).
     * @return Locale-formatted string (e.g., "200,08" for Spanish)
     */
    fun formatForDisplay(
        internalValue: String,
        maxDecimalPlaces: Int,
        minDecimalPlaces: Int = 0
    ): String {
        return internalValue.formatNumberForDisplay(
            locale = localeProvider.getCurrentLocale(),
            maxDecimalPlaces = maxDecimalPlaces,
            minDecimalPlaces = minDecimalPlaces
        )
    }

    /**
     * Formats an exchange rate for display using locale-aware formatting.
     *
     * @param internalValue The rate in internal format (e.g., "37.22")
     * @return Locale-formatted string (e.g., "37,22" for Spanish)
     */
    fun formatRateForDisplay(internalValue: String): String {
        return internalValue.formatRateForDisplay(locale = localeProvider.getCurrentLocale())
    }

    fun mapToDomain(state: AddExpenseUiState, groupId: String): Result<Expense> {
        return try {
            val sourceCurrency = state.selectedCurrency
            val groupCurrency = state.groupCurrency

            val sourceAmount = parseToSmallestUnit(state.sourceAmount, sourceCurrency)

            // Convert display rate (1 GroupCurrency = X SourceCurrency) to internal rate (1 SourceCurrency = X GroupCurrency)
            // Normalize the rate string to handle locale-specific decimal separators (comma vs dot)
            val normalizedDisplayRate = CurrencyConverter.normalizeAmountString(state.displayExchangeRate.trim())
            val displayRate = normalizedDisplayRate.toBigDecimalOrNull() ?: BigDecimal.ONE
            val internalRate = if (displayRate.compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal.ONE.divide(displayRate, RATE_PRECISION, RoundingMode.HALF_UP)
            } else {
                BigDecimal.ZERO
            }

            // Calculate groupAmount based on whether it was explicitly set or needs to be calculated
            val groupAmount = if (state.calculatedGroupAmount.isNotBlank()) {
                // User explicitly set the group amount (Revolut case) or it was calculated
                parseToSmallestUnit(state.calculatedGroupAmount, groupCurrency)
            } else {
                // Not set, calculate from source amount and internal rate using BigDecimal
                BigDecimal(sourceAmount).multiply(internalRate).setScale(0, RoundingMode.HALF_UP).toLong()
            }

            val expense = Expense(
                groupId = groupId,
                title = state.expenseTitle,
                sourceAmount = sourceAmount,
                sourceCurrency = sourceCurrency?.code ?: "EUR",
                groupAmount = groupAmount,
                groupCurrency = groupCurrency?.code ?: "EUR",
                exchangeRate = internalRate.toDouble(),
                paymentMethod = state.selectedPaymentMethod
            )
            Result.success(expense)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Parses an amount string to the smallest currency unit (e.g., cents for EUR, yen for JPY).
     * Uses the currency's decimal digits to determine the multiplier.
     *
     * Examples:
     * - "10.50" with EUR (2 decimals) → 1050 (cents)
     * - "10" with JPY (0 decimals) → 10 (yen)
     * - "10.500" with TND (3 decimals) → 10500 (millimes)
     */
    private fun parseToSmallestUnit(amountString: String, currency: Currency?): Long {
        val decimalPlaces = currency?.decimalDigits ?: 2
        val normalizedString = CurrencyConverter.normalizeAmountString(amountString.trim())

        val amount = normalizedString.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val multiplier = BigDecimal.TEN.pow(decimalPlaces)

        return amount.multiply(multiplier).setScale(0, RoundingMode.HALF_UP).toLong()
    }
}
