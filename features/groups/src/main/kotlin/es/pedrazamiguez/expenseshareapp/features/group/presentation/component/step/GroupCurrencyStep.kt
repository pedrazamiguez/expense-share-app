package es.pedrazamiguez.expenseshareapp.features.group.presentation.component.step

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.currency.CurrencyDropdown
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.SearchableChipSelector
import es.pedrazamiguez.expenseshareapp.features.group.R
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.event.CreateGroupUiEvent
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.state.CreateGroupUiState

/**
 * Step 2: Primary currency dropdown + optional extra currencies chip selector.
 */
@Composable
fun GroupCurrencyStep(
    uiState: CreateGroupUiState,
    onEvent: (CreateGroupUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CurrencyDropdown(
                    selectedCurrency = uiState.selectedCurrency,
                    availableCurrencies = uiState.availableCurrencies,
                    onCurrencySelected = { onEvent(CreateGroupUiEvent.CurrencySelected(it)) },
                    label = stringResource(R.string.group_field_currency),
                    isLoading = uiState.isLoadingCurrencies,
                    modifier = Modifier.fillMaxWidth()
                )
                if (uiState.availableCurrencies.isNotEmpty()) {
                    SearchableChipSelector(
                        availableItems = uiState.availableCurrencies,
                        selectedItems = uiState.extraCurrencies,
                        onItemAdded = { onEvent(CreateGroupUiEvent.ExtraCurrencyToggled(it.code)) },
                        onItemRemoved = { onEvent(CreateGroupUiEvent.ExtraCurrencyToggled(it.code)) },
                        itemKey = { it.code },
                        itemDisplayText = { it.displayText },
                        itemSecondaryText = { it.defaultName },
                        itemMatchesQuery = { currency, query ->
                            val upper = query.uppercase()
                            currency.code.contains(upper) ||
                                currency.defaultName.uppercase().contains(upper) ||
                                currency.displayText.uppercase().contains(upper)
                        },
                        excludedItems = listOfNotNull(uiState.selectedCurrency),
                        title = stringResource(R.string.group_field_extra_currencies),
                        searchLabel = stringResource(R.string.group_extra_currency_search),
                        searchPlaceholder = stringResource(R.string.group_extra_currency_search_hint),
                        helperText = stringResource(R.string.group_field_extra_currencies_hint),
                        chipRemoveContentDescription = stringResource(R.string.group_extra_currency_remove),
                        clearSearchContentDescription = stringResource(R.string.group_extra_currency_clear),
                        keyboardCapitalization = KeyboardCapitalization.Characters
                    )
                }
            }
        }
    }
}
