package es.pedrazamiguez.expenseshareapp.features.group.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.currency.CurrencyDropdown
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.form.FormErrorBanner
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.form.FormSubmitButton
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.AsyncSearchableChipSelector
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.SearchableChipSelector
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.StyledOutlinedTextField
import es.pedrazamiguez.expenseshareapp.features.group.R
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.event.CreateGroupUiEvent
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.state.CreateGroupUiState

@Composable
fun CreateGroupForm(
    uiState: CreateGroupUiState,
    onEvent: (CreateGroupUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val isFormValid = uiState.isNameValid && uiState.groupName.isNotBlank()

    val submitForm = {
        focusManager.clearFocus()
        if (isFormValid && !uiState.isLoading) {
            onEvent(CreateGroupUiEvent.SubmitCreateGroup)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.ime)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 24.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                GroupInfoSection(uiState = uiState, onEvent = onEvent, submitForm = submitForm)
                GroupCurrencySection(uiState = uiState, onEvent = onEvent)
                GroupMembersSection(uiState = uiState, onEvent = onEvent)
                FormErrorBanner(error = uiState.error)
            }

            FormSubmitButton(
                label = stringResource(R.string.groups_create),
                isEnabled = isFormValid,
                isLoading = uiState.isLoading,
                onSubmit = submitForm
            )
        }
    }
}

@Composable
private fun GroupInfoSection(
    uiState: CreateGroupUiState,
    onEvent: (CreateGroupUiEvent) -> Unit,
    submitForm: () -> Unit
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
            StyledOutlinedTextField(
                value = uiState.groupName,
                onValueChange = { onEvent(CreateGroupUiEvent.NameChanged(it)) },
                label = stringResource(R.string.group_field_name),
                isError = !uiState.isNameValid,
                supportingText = if (!uiState.isNameValid) stringResource(R.string.group_field_name_required) else null,
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
}

@Composable
private fun GroupCurrencySection(
    uiState: CreateGroupUiState,
    onEvent: (CreateGroupUiEvent) -> Unit
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

@Composable
private fun GroupMembersSection(
    uiState: CreateGroupUiState,
    onEvent: (CreateGroupUiEvent) -> Unit
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
}
