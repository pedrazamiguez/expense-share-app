package es.pedrazamiguez.expenseshareapp.features.group.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.AsyncSearchableChipSelector
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.SearchableChipSelector
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.StyledOutlinedTextField
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.formatDisplay
import es.pedrazamiguez.expenseshareapp.features.group.R
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.event.CreateGroupUiEvent
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.state.CreateGroupUiState

@Composable
fun CreateGroupForm(uiState: CreateGroupUiState, onEvent: (CreateGroupUiEvent) -> Unit, modifier: Modifier = Modifier) {
    val focusManager = LocalFocusManager.current
    val isFormValid = uiState.isNameValid && uiState.groupName.isNotBlank()

    val submitForm = {
        focusManager.clearFocus()
        if (isFormValid && !uiState.isLoading) {
            onEvent(CreateGroupUiEvent.SubmitCreateGroup)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StyledOutlinedTextField(
                    value = uiState.groupName,
                    onValueChange = { onEvent(CreateGroupUiEvent.NameChanged(it)) },
                    label = stringResource(R.string.group_field_name),
                    isError = !uiState.isNameValid,
                    supportingText = if (!uiState.isNameValid) {
                        stringResource(
                            R.string.group_field_name_required
                        )
                    } else {
                        null
                    },
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                    capitalization = KeyboardCapitalization.Sentences,
                    modifier = Modifier.fillMaxWidth()
                )

                StyledOutlinedTextField(
                    value = uiState.groupDescription,
                    onValueChange = { onEvent(CreateGroupUiEvent.DescriptionChanged(it)) },
                    label = stringResource(R.string.group_field_description),
                    singleLine = false,
                    maxLines = 4,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                    capitalization = KeyboardCapitalization.Sentences,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardActions = KeyboardActions(onDone = { submitForm() })
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    var expanded by remember { mutableStateOf(false) }
                    StyledOutlinedTextField(
                        value = uiState.selectedCurrency?.formatDisplay() ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = stringResource(R.string.group_field_currency),
                        trailingIcon = {
                            if (uiState.isLoadingCurrencies) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        },
                        onClick = { if (!uiState.isLoadingCurrencies) expanded = true },
                        modifier = Modifier.fillMaxWidth()
                    )

                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        uiState.availableCurrencies.forEach { currency ->
                            DropdownMenuItem(text = { Text(currency.formatDisplay()) }, onClick = {
                                onEvent(CreateGroupUiEvent.CurrencySelected(currency))
                                expanded = false
                            })
                        }
                    }
                }

                if (uiState.availableCurrencies.isNotEmpty()) {
                    SearchableChipSelector(
                        availableItems = uiState.availableCurrencies,
                        selectedItems = uiState.extraCurrencies,
                        onItemAdded = { onEvent(CreateGroupUiEvent.ExtraCurrencyToggled(it)) },
                        onItemRemoved = { onEvent(CreateGroupUiEvent.ExtraCurrencyToggled(it)) },
                        itemKey = { it.code },
                        itemDisplayText = { it.formatDisplay() },
                        itemSecondaryText = { it.defaultName },
                        itemMatchesQuery = { currency, query ->
                            val upperQuery = query.uppercase()
                            currency.code.contains(upperQuery) ||
                                currency.defaultName.uppercase()
                                    .contains(upperQuery) ||
                                currency.symbol.contains(query)
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

        // Members section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AsyncSearchableChipSelector(
                    searchResults = uiState.memberSearchResults,
                    selectedItems = uiState.selectedMembers,
                    onSearchQueryChanged = { onEvent(CreateGroupUiEvent.MemberSearchQueryChanged(it)) },
                    onItemAdded = { onEvent(CreateGroupUiEvent.MemberSelected(it)) },
                    onItemRemoved = { onEvent(CreateGroupUiEvent.MemberRemoved(it)) },
                    itemKey = { it.userId },
                    itemDisplayText = { it.displayName ?: it.email },
                    itemSecondaryText = { it.email },
                    isSearching = uiState.isSearchingMembers,
                    title = stringResource(R.string.group_field_members),
                    searchLabel = stringResource(R.string.group_member_search),
                    searchPlaceholder = stringResource(R.string.group_member_search_hint),
                    helperText = stringResource(R.string.group_member_search_helper),
                    noResultsText = stringResource(R.string.group_member_search_no_results),
                    chipRemoveContentDescription = stringResource(R.string.group_member_remove),
                    clearSearchContentDescription = stringResource(R.string.group_member_clear_search)
                )
            }
        }

        if (uiState.errorRes != null) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(uiState.errorRes),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Button(
            onClick = { submitForm() },
            enabled = isFormValid && !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.large
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = stringResource(R.string.groups_create),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
