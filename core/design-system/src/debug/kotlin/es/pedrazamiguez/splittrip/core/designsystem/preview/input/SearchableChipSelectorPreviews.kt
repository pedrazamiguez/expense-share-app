package es.pedrazamiguez.splittrip.core.designsystem.preview.input

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.R
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.input.SearchableChipSelector
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewThemeWrapper

private data class CurrencyItem(val code: String, val name: String)

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
            title = stringResource(R.string.preview_title_other_currencies),
            searchLabel = stringResource(R.string.preview_search_currency),
            searchPlaceholder = stringResource(R.string.preview_search_currency_hint),
            helperText = stringResource(R.string.preview_helper_currencies)
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
            title = stringResource(R.string.preview_title_other_currencies),
            searchLabel = stringResource(R.string.preview_search_currency),
            searchPlaceholder = stringResource(R.string.preview_search_currency_hint),
            chipRemoveContentDescription = stringResource(R.string.preview_remove_currency)
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
            title = stringResource(R.string.preview_title_other_currencies),
            searchLabel = stringResource(R.string.preview_search_currency),
            chipRemoveContentDescription = stringResource(R.string.preview_remove_currency)
        )
    }
}
