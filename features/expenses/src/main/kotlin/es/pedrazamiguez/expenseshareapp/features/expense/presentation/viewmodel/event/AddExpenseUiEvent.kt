package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event

sealed interface AddExpenseUiEvent {
    data class LoadGroupConfig(val groupId: String?) : AddExpenseUiEvent
    data class RetryLoadConfig(val groupId: String?) : AddExpenseUiEvent
    data class TitleChanged(val title: String) : AddExpenseUiEvent
    data class SourceAmountChanged(val amount: String) : AddExpenseUiEvent
    data class CurrencySelected(val currencyCode: String) : AddExpenseUiEvent
    data class PaymentMethodSelected(val methodId: String) : AddExpenseUiEvent
    data class ExchangeRateChanged(val rate: String) : AddExpenseUiEvent
    data class GroupAmountChanged(val amount: String) : AddExpenseUiEvent
    data class CategorySelected(val categoryId: String) : AddExpenseUiEvent
    data class VendorChanged(val vendor: String) : AddExpenseUiEvent
    data class PaymentStatusSelected(val statusId: String) : AddExpenseUiEvent
    data class DueDateSelected(val dateMillis: Long) : AddExpenseUiEvent
    data class ReceiptImageSelected(val uri: String) : AddExpenseUiEvent
    data object RemoveReceiptImage : AddExpenseUiEvent
    data class SubmitAddExpense(val groupId: String?) : AddExpenseUiEvent
}
