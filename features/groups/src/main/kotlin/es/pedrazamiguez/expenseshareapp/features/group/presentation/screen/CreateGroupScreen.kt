package es.pedrazamiguez.expenseshareapp.features.group.presentation.screen

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.AppOutlinedTextField
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.SearchableChipSelector
import es.pedrazamiguez.expenseshareapp.core.designsystem.transition.LocalAnimatedVisibilityScope
import es.pedrazamiguez.expenseshareapp.core.designsystem.transition.LocalSharedTransitionScope
import es.pedrazamiguez.expenseshareapp.features.group.R
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.event.CreateGroupUiEvent
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.state.CreateGroupUiState

/**
 * Shared element transition key for the Create Group FAB -> Screen transition.
 */
const val CREATE_GROUP_SHARED_ELEMENT_KEY = "create_group_container"

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CreateGroupScreen(
    uiState: CreateGroupUiState,
    onEvent: (CreateGroupUiEvent) -> Unit = {},
) {
    // Load currencies when screen is first displayed
    LaunchedEffect(Unit) {
        onEvent(CreateGroupUiEvent.LoadCurrencies)
    }

    // Get shared transition scope if available
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    // Build the shared element modifier for the container
    val sharedModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            Modifier.sharedBounds(
                sharedContentState = rememberSharedContentState(key = CREATE_GROUP_SHARED_ELEMENT_KEY),
                animatedVisibilityScope = animatedVisibilityScope,
                resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(ContentScale.Fit),
                boundsTransform = { _, _ ->
                    spring(dampingRatio = 0.8f, stiffness = 300f)
                },
                enter = fadeIn(tween(durationMillis = 300)),
                exit = fadeOut(tween(durationMillis = 300))
            )
        }
    } else {
        Modifier
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .then(sharedModifier),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .imePadding()
                .padding(bottom = 80.dp) // Space for bottom navigation
        ) {
            // --- 1. GROUP NAME ---
            AppOutlinedTextField(
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

            // --- 2. MAIN CURRENCY DROPDOWN ---
            Box(modifier = Modifier.fillMaxWidth()) {
                var expanded by remember { mutableStateOf(false) }
                AppOutlinedTextField(
                    value = uiState.selectedCurrency?.formatDisplay() ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = stringResource(R.string.group_field_currency),
                    trailingIcon = {
                        if (uiState.isLoadingCurrencies) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp), strokeWidth = 2.dp
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

            // --- 3. EXTRA CURRENCIES (Search & Chips) ---
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
                        currency.code.contains(upperQuery) || currency.defaultName.uppercase()
                            .contains(upperQuery) || currency.symbol.contains(query)
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

            // --- 4. DESCRIPTION ---
            AppOutlinedTextField(
                value = uiState.groupDescription,
                onValueChange = { onEvent(CreateGroupUiEvent.DescriptionChanged(it)) },
                label = stringResource(R.string.group_field_description),
                singleLine = false,
                maxLines = 4,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
                capitalization = KeyboardCapitalization.Sentences,
                modifier = Modifier.fillMaxWidth()
            )

            // --- 5. SUBMIT BUTTON ---
            Button(
                onClick = { onEvent(CreateGroupUiEvent.SubmitCreateGroup) },
                enabled = !uiState.isLoading && uiState.isNameValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text(stringResource(R.string.groups_create))
                }
            }

            if (uiState.errorRes != null) {
                Text(
                    stringResource(uiState.errorRes),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

