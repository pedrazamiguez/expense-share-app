package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event

import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.model.Currency

sealed interface AddExpenseUiEvent {
    data class LoadGroupConfig(val groupId: String?) : AddExpenseUiEvent
    data class TitleChanged(val title: String) : AddExpenseUiEvent
    data class SourceAmountChanged(val amount: String) : AddExpenseUiEvent
    data class CurrencySelected(val currency: Currency) : AddExpenseUiEvent
    data class PaymentMethodSelected(val method: PaymentMethod) : AddExpenseUiEvent
    data class ExchangeRateChanged(val rate: String) : AddExpenseUiEvent
    data class GroupAmountChanged(val amount: String) : AddExpenseUiEvent
    data class SubmitAddExpense(val groupId: String?) : AddExpenseUiEvent
}
