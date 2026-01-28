package es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper

import es.pedrazamiguez.expenseshareapp.domain.converter.CurrencyConverter
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState
import java.math.BigDecimal
import java.math.RoundingMode

class AddExpenseUiMapper {

    fun mapToDomain(state: AddExpenseUiState, groupId: String): Result<Expense> {
        return try {
            val sourceAmount = CurrencyConverter.parseToCents(state.sourceAmount).getOrThrow()
            val rate = state.exchangeRate.toBigDecimalOrNull() ?: BigDecimal.ONE

            // Calculate groupAmount based on whether it was explicitly set or needs to be calculated
            val groupAmount = if (state.calculatedGroupAmount.isNotBlank()) {
                // User explicitly set the group amount (Revolut case) or it was calculated
                CurrencyConverter.parseToCents(state.calculatedGroupAmount).getOrElse {
                    // If parsing fails, calculate from source amount and rate using BigDecimal
                    BigDecimal(sourceAmount)
                        .multiply(rate)
                        .setScale(0, RoundingMode.HALF_UP)
                        .toLong()
                }
            } else {
                // Not set, calculate from source amount and rate using BigDecimal
                BigDecimal(sourceAmount)
                    .multiply(rate)
                    .setScale(0, RoundingMode.HALF_UP)
                    .toLong()
            }

            val expense = Expense(
                groupId = groupId,
                title = state.expenseTitle,
                sourceAmount = sourceAmount,
                sourceCurrency = state.selectedCurrency?.code ?: "EUR",
                groupAmount = groupAmount,
                groupCurrency = state.groupCurrency?.code ?: "EUR",
                exchangeRate = rate.toDouble(),
                paymentMethod = state.selectedPaymentMethod
            )
            Result.success(expense)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
