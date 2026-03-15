package es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state

import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.CurrencyUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class AddCashWithdrawalUiState(
    val isLoading: Boolean = false,
    val isLoadingRate: Boolean = false,
    val isConfigLoaded: Boolean = false,
    val configLoadFailed: Boolean = false,
    val loadedGroupId: String? = null,
    val groupName: String? = null,

    // Currency
    val groupCurrency: CurrencyUiModel? = null,
    val selectedCurrency: CurrencyUiModel? = null,
    val availableCurrencies: ImmutableList<CurrencyUiModel> = persistentListOf(),

    // Inputs
    val withdrawalAmount: String = "",
    val deductedAmount: String = "",
    val displayExchangeRate: String = "1.0",

    // Exchange rate section visibility
    val showExchangeRateSection: Boolean = false,
    val exchangeRateLabel: String = "",
    val deductedAmountLabel: String = "",

    // Validation
    val isAmountValid: Boolean = true,
    val error: UiText? = null
) {
    val isReady: Boolean
        get() = isConfigLoaded && !configLoadFailed && !isLoading

    val isFormValid: Boolean
        get() {
            val hasAmount = withdrawalAmount.isNotBlank() && isAmountValid
            val hasDeducted = if (showExchangeRateSection) deductedAmount.isNotBlank() else true
            return hasAmount && hasDeducted
        }
}
