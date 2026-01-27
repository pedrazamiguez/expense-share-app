package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state

import androidx.annotation.StringRes
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.model.Currency

data class AddExpenseUiState(
    val isLoading: Boolean = false,

    // Inputs
    val expenseTitle: String = "",
    val sourceAmount: String = "",

    // Selection
    val selectedCurrency: Currency? = null,
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.CASH,

    // Calculated / Display Data
    val groupCurrency: Currency? = null,
    val exchangeRate: String = "1.0",
    val calculatedGroupAmount: String = "", // "Cost in EUR"
    val showExchangeRateSection: Boolean = false,

    // Data Lists
    val availableCurrencies: List<Currency> = emptyList(),
    val paymentMethods: List<PaymentMethod> = PaymentMethod.entries.toList(),

    // Errors
    @param:StringRes val errorRes: Int? = null,
    val errorMessage: String? = null,
    val isTitleValid: Boolean = true,
    val isAmountValid: Boolean = true
)
