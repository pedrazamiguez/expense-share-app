package es.pedrazamiguez.expenseshareapp.core.designsystem.preview.input

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.SearchableChipSelector
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewThemeWrapper

private data class CurrencyItem(
    val code: String,
    val name: String
)

private val sampleCurrencies = listOf(
    CurrencyItem("USD", "US Dollar"),
    CurrencyItem("EUR", "Euro"),
    CurrencyItem("GBP", "British Pound"),
    CurrencyItem("JPY", "Japanese Yen"),
    CurrencyItem("MXN", "Mexican Peso"),
    CurrencyItem("CAD", "Canadian Dollar")
)

@PreviewComplete
@Composable
private fun SearchableChipSelectorEmptyPreview() {
    PreviewThemeWrapper {
        SearchableChipSelector(
            availableItems = sampleCurrencies,
            selectedItems = emptyList(),
            onItemAdded = {},
            onItemRemoved = {},
            itemKey = { it.code },
            itemDisplayText = { "${it.code} - ${it.name}" },
            itemMatchesQuery = { item, query ->
                item.code.contains(query, ignoreCase = true) ||
                        item.name.contains(query, ignoreCase = true)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            title = "Other currencies",
            searchLabel = "Search currency",
            searchPlaceholder = "e.g. USD, EUR, Pound…",
            helperText = "Add the currencies you'll use on your trip"
        )
    }
}

@PreviewComplete
@Composable
private fun SearchableChipSelectorWithSelectionsPreview() {
    PreviewThemeWrapper {
        SearchableChipSelector(
            availableItems = sampleCurrencies,
            selectedItems = listOf(
                CurrencyItem("USD", "US Dollar"),
                CurrencyItem("GBP", "British Pound")
            ),
            onItemAdded = {},
            onItemRemoved = {},
            itemKey = { it.code },
            itemDisplayText = { "${it.code} - ${it.name}" },
            itemMatchesQuery = { item, query ->
                item.code.contains(query, ignoreCase = true) ||
                        item.name.contains(query, ignoreCase = true)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            title = "Other currencies",
            searchLabel = "Search currency",
            searchPlaceholder = "e.g. USD, EUR, Pound…",
            chipRemoveContentDescription = "Remove currency"
        )
    }
}

@PreviewComplete
@Composable
private fun SearchableChipSelectorManySelectionsPreview() {
    PreviewThemeWrapper {
        SearchableChipSelector(
            availableItems = sampleCurrencies,
            selectedItems = listOf(
                CurrencyItem("USD", "US Dollar"),
                CurrencyItem("GBP", "British Pound"),
                CurrencyItem("JPY", "Japanese Yen"),
                CurrencyItem("MXN", "Mexican Peso")
            ),
            onItemAdded = {},
            onItemRemoved = {},
            itemKey = { it.code },
            itemDisplayText = { "${it.code} - ${it.name}" },
            itemMatchesQuery = { item, query ->
                item.code.contains(query, ignoreCase = true) ||
                        item.name.contains(query, ignoreCase = true)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            title = "Other currencies",
            searchLabel = "Search currency",
            chipRemoveContentDescription = "Remove currency"
        )
    }
}
