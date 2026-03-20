package es.pedrazamiguez.expenseshareapp.features.balance.presentation.preview

import androidx.compose.runtime.Composable
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewThemeWrapper
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.CurrencyUiModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen.AddCashWithdrawalScreen
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.AddCashWithdrawalUiState
import kotlinx.collections.immutable.persistentListOf

private val PREVIEW_CURRENCIES = persistentListOf(
    CurrencyUiModel("EUR", "EUR - Euro", 2),
    CurrencyUiModel("THB", "THB - Thai Baht", 2),
    CurrencyUiModel("USD", "USD - US Dollar", 2)
)

@PreviewComplete
@Composable
private fun AddCashWithdrawalScreenLoadingPreview() {
    PreviewThemeWrapper {
        AddCashWithdrawalScreen(
            uiState = AddCashWithdrawalUiState(isLoading = true)
        )
    }
}

@PreviewComplete
@Composable
private fun AddCashWithdrawalScreenConfigFailedPreview() {
    PreviewThemeWrapper {
        AddCashWithdrawalScreen(
            uiState = AddCashWithdrawalUiState(
                isConfigLoaded = true,
                configLoadFailed = true
            )
        )
    }
}

@PreviewComplete
@Composable
private fun AddCashWithdrawalScreenReadyPreview() {
    PreviewThemeWrapper {
        AddCashWithdrawalScreen(
            groupId = "group-1",
            uiState = AddCashWithdrawalUiState(
                isConfigLoaded = true,
                groupName = "Thai 2.0",
                groupCurrency = CurrencyUiModel("EUR", "EUR - Euro", 2),
                selectedCurrency = CurrencyUiModel("THB", "THB - Thai Baht", 2),
                availableCurrencies = PREVIEW_CURRENCIES,
                showExchangeRateSection = true,
                exchangeRateLabel = "1 EUR = X THB",
                deductedAmountLabel = "Deducted in EUR"
            )
        )
    }
}

@PreviewComplete
@Composable
private fun AddCashWithdrawalScreenFilledPreview() {
    PreviewThemeWrapper {
        AddCashWithdrawalScreen(
            groupId = "group-1",
            uiState = AddCashWithdrawalUiState(
                isConfigLoaded = true,
                groupName = "Thai 2.0",
                groupCurrency = CurrencyUiModel("EUR", "EUR - Euro", 2),
                selectedCurrency = CurrencyUiModel("THB", "THB - Thai Baht", 2),
                availableCurrencies = PREVIEW_CURRENCIES,
                withdrawalAmount = "10000",
                deductedAmount = "270.00",
                displayExchangeRate = "37.037",
                showExchangeRateSection = true,
                exchangeRateLabel = "1 EUR = X THB",
                deductedAmountLabel = "Deducted in EUR"
            )
        )
    }
}
