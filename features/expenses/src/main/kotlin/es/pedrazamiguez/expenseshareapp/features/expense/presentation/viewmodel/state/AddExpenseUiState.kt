package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state

import androidx.annotation.StringRes
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

data class AddExpenseUiState(
    val isLoading: Boolean = false,
    val isConfigLoaded: Boolean = false,
    val configLoadFailed: Boolean = false,
    val loadedGroupId: String? = null,

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
    val availableCurrencies: ImmutableList<Currency> = persistentListOf(),
    val paymentMethods: ImmutableList<PaymentMethod> = PaymentMethod.entries.toImmutableList(),

    // Errors
    @param:StringRes
    val errorRes: Int? = null,
    val errorMessage: String? = null,
    val isTitleValid: Boolean = true,
    val isAmountValid: Boolean = true
) {
    /**
     * Returns true when the screen is ready for user interaction.
     * The form should only be shown when config is loaded and not failed.
     */
    val isReady: Boolean
        get() = isConfigLoaded && !configLoadFailed && !isLoading
}
