package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state

import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.CurrencyUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.PaymentMethodUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class AddExpenseUiState(
    val isLoading: Boolean = false,
    val isLoadingRate: Boolean = false,
    val isConfigLoaded: Boolean = false,
    val configLoadFailed: Boolean = false,
    val loadedGroupId: String? = null,
    val groupName: String? = null,

    // Inputs
    val expenseTitle: String = "",
    val sourceAmount: String = "",

    // Selection
    val selectedCurrency: CurrencyUiModel? = null,
    val selectedPaymentMethod: PaymentMethodUiModel? = null,

    // Calculated / Display Data
    val groupCurrency: CurrencyUiModel? = null,
    /**
     * User-friendly exchange rate displayed in the UI.
     * Represents "1 [GroupCurrency] = X [SourceCurrency]" (e.g., "1 EUR = 37 THB").
     * This is the INVERSE of the internal calculation rate.
     */
    val displayExchangeRate: String = "1.0",
    val calculatedGroupAmount: String = "", // "Cost in EUR"
    val showExchangeRateSection: Boolean = false,

    // Pre-formatted labels for the exchange rate section
    val exchangeRateLabel: String = "",
    val groupAmountLabel: String = "",

    // Data Lists
    val availableCurrencies: ImmutableList<CurrencyUiModel> = persistentListOf(),
    val paymentMethods: ImmutableList<PaymentMethodUiModel> = persistentListOf(),

    // Errors
    val error: UiText? = null,
    val isTitleValid: Boolean = true,
    val isAmountValid: Boolean = true
) {
    /**
     * Returns true when the screen is ready for user interaction.
     * The form should only be shown when config is loaded and not failed.
     */
    val isReady: Boolean
        get() = isConfigLoaded && !configLoadFailed && !isLoading

    /**
     * Returns true when the form inputs are valid and ready for submission.
     */
    val isFormValid: Boolean
        get() = isTitleValid && isAmountValid && expenseTitle.isNotBlank() && sourceAmount.isNotBlank()
}
