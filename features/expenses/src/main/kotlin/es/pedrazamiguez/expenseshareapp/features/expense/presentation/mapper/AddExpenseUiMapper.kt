package es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper

import es.pedrazamiguez.expenseshareapp.domain.converter.CurrencyConverter
import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState
import java.math.BigDecimal
import java.math.RoundingMode

class AddExpenseUiMapper {

    fun mapToDomain(state: AddExpenseUiState, groupId: String): Result<Expense> {
        return try {
            val sourceCurrency = state.selectedCurrency
            val groupCurrency = state.groupCurrency

            val sourceAmount = parseToSmallestUnit(state.sourceAmount, sourceCurrency)
            val rate = state.exchangeRate.toBigDecimalOrNull() ?: BigDecimal.ONE

            // Calculate groupAmount based on whether it was explicitly set or needs to be calculated
            val groupAmount = if (state.calculatedGroupAmount.isNotBlank()) {
                // User explicitly set the group amount (Revolut case) or it was calculated
                parseToSmallestUnit(state.calculatedGroupAmount, groupCurrency)
            } else {
                // Not set, calculate from source amount and rate using BigDecimal
                BigDecimal(sourceAmount).multiply(rate).setScale(0, RoundingMode.HALF_UP).toLong()
            }

            val expense = Expense(
                groupId = groupId,
                title = state.expenseTitle,
                sourceAmount = sourceAmount,
                sourceCurrency = sourceCurrency?.code ?: "EUR",
                groupAmount = groupAmount,
                groupCurrency = groupCurrency?.code ?: "EUR",
                exchangeRate = rate.toDouble(),
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
