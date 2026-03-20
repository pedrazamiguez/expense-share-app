package es.pedrazamiguez.expenseshareapp.features.expense.presentation.preview

import androidx.compose.runtime.Composable
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewThemeWrapper
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.CurrencyUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.screen.AddExpenseScreen
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState
import kotlinx.collections.immutable.persistentListOf

private val PREVIEW_CURRENCIES = persistentListOf(
    CurrencyUiModel("EUR", "EUR - Euro", 2),
    CurrencyUiModel("THB", "THB - Thai Baht", 2),
    CurrencyUiModel("USD", "USD - US Dollar", 2)
)

@PreviewComplete
@Composable
private fun AddExpenseScreenLoadingPreview() {
    PreviewThemeWrapper {
        AddExpenseScreen(
            uiState = AddExpenseUiState(isLoading = true)
        )
    }
}

@PreviewComplete
@Composable
private fun AddExpenseScreenConfigFailedPreview() {
    PreviewThemeWrapper {
        AddExpenseScreen(
            uiState = AddExpenseUiState(
                isConfigLoaded = true,
                configLoadFailed = true
            )
        )
    }
}

@PreviewComplete
@Composable
private fun AddExpenseScreenReadyPreview() {
    PreviewThemeWrapper {
        AddExpenseScreen(
            groupId = "group-1",
            uiState = AddExpenseUiState(
                isConfigLoaded = true,
                groupName = "Thai 2.0",
                availableCurrencies = PREVIEW_CURRENCIES,
                selectedCurrency = CurrencyUiModel("EUR", "EUR - Euro", 2),
                groupCurrency = CurrencyUiModel("EUR", "EUR - Euro", 2)
            )
        )
    }
}

@PreviewComplete
@Composable
private fun AddExpenseScreenFilledPreview() {
    PreviewThemeWrapper {
        AddExpenseScreen(
            groupId = "group-1",
            uiState = AddExpenseUiState(
                isConfigLoaded = true,
                groupName = "Thai 2.0",
                expenseTitle = "Dinner at Pad Thai place",
                sourceAmount = "450",
                availableCurrencies = PREVIEW_CURRENCIES,
                selectedCurrency = CurrencyUiModel("THB", "THB - Thai Baht", 2),
                groupCurrency = CurrencyUiModel("EUR", "EUR - Euro", 2),
                showExchangeRateSection = true,
                displayExchangeRate = "37.037",
                calculatedGroupAmount = "12.15",
                exchangeRateLabel = "1 EUR = X THB",
                groupAmountLabel = "Cost in EUR"
            )
        )
    }
}
