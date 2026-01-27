package es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper

import es.pedrazamiguez.expenseshareapp.domain.converter.CurrencyConverter
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState

class AddExpenseUiMapper {

    fun mapToDomain(state: AddExpenseUiState, groupId: String): Result<Expense> {
        return try {
            val sourceAmount = CurrencyConverter.parseToCents(state.sourceAmount).getOrThrow()
            // If explicit group amount exists (Revolut case), use it. Else calculate it.
            val groupAmount = CurrencyConverter.parseToCents(state.calculatedGroupAmount).getOrElse { 0L }
            val rate = state.exchangeRate.toDoubleOrNull() ?: 1.0

            val expense = Expense(
                groupId = groupId,
                title = state.expenseTitle,
                sourceAmount = sourceAmount,
                sourceCurrency = state.selectedCurrency?.code ?: "EUR",
                groupAmount = groupAmount,
                groupCurrency = state.groupCurrency?.code ?: "EUR",
                exchangeRate = rate,
                paymentMethod = state.selectedPaymentMethod
            )
            Result.success(expense)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
