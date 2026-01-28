package es.pedrazamiguez.expenseshareapp.features.group.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewThemeWrapper
import es.pedrazamiguez.expenseshareapp.core.designsystem.transition.LocalAnimatedVisibilityScope
import es.pedrazamiguez.expenseshareapp.core.designsystem.transition.LocalSharedTransitionScope
import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.features.group.R
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.event.CreateGroupUiEvent
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.state.CreateGroupUiState

/**
 * Shared element transition key for the Create Group FAB -> Screen transition.
 */
const val CREATE_GROUP_SHARED_ELEMENT_KEY = "create_group_container"

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalSharedTransitionApi::class,
    ExperimentalLayoutApi::class
)
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
            OutlinedTextField(
                value = uiState.groupName,
                onValueChange = { onEvent(CreateGroupUiEvent.NameChanged(it)) },
                label = { Text(stringResource(R.string.group_field_name)) },
                singleLine = true,
                isError = !uiState.isNameValid,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                    capitalization = KeyboardCapitalization.Sentences
                ),
                modifier = Modifier.fillMaxWidth()
            )
            if (!uiState.isNameValid) {
                Text(
                    text = stringResource(R.string.group_field_name_required),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // --- 2. MAIN CURRENCY DROPDOWN ---
            Box(modifier = Modifier.fillMaxWidth()) {
                var expanded by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = uiState.selectedCurrency?.formatDisplay() ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.group_field_currency)) },
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
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                // Invisible overlay to capture click without ripple
                Box(
                    Modifier
                        .matchParentSize()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { if (!uiState.isLoadingCurrencies) expanded = true }
                )

                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    uiState.availableCurrencies.forEach { currency ->
                        DropdownMenuItem(
                            text = { Text(currency.formatDisplay()) },
                            onClick = {
                                onEvent(CreateGroupUiEvent.CurrencySelected(currency))
                                expanded = false
                            }
                        )
                    }
                }
            }

            // --- 3. EXTRA CURRENCIES (Search & Chips) ---
            if (uiState.availableCurrencies.isNotEmpty()) {
                ExtraCurrencySelector(
                    availableCurrencies = uiState.availableCurrencies,
                    selectedMainCurrency = uiState.selectedCurrency,
                    extraCurrencies = uiState.extraCurrencies,
                    onCurrencyAdded = { onEvent(CreateGroupUiEvent.ExtraCurrencyToggled(it)) },
                    onCurrencyRemoved = { onEvent(CreateGroupUiEvent.ExtraCurrencyToggled(it)) }
                )
            }

            // --- 4. DESCRIPTION ---
            OutlinedTextField(
                value = uiState.groupDescription,
                onValueChange = { onEvent(CreateGroupUiEvent.DescriptionChanged(it)) },
                label = { Text(stringResource(R.string.group_field_description)) },
                singleLine = false,
                maxLines = 4,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                    capitalization = KeyboardCapitalization.Sentences
                ),
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

@PreviewComplete
@Composable
private fun CreateGroupScreenPreview() {
    PreviewThemeWrapper {
        CreateGroupScreen(
            uiState = CreateGroupUiState()
        )
    }
}

/**
 * A search-based currency selector with autocomplete and removable chips.
 * Much cleaner UX than showing 170+ FilterChips.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ExtraCurrencySelector(
    availableCurrencies: List<Currency>,
    selectedMainCurrency: Currency?,
    extraCurrencies: List<Currency>,
    onCurrencyAdded: (Currency) -> Unit,
    onCurrencyRemoved: (Currency) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Filter currencies based on search query, excluding main currency and already selected extras
    val filteredCurrencies by remember(
        searchQuery,
        selectedMainCurrency,
        extraCurrencies,
        availableCurrencies
    ) {
        derivedStateOf {
            if (searchQuery.length < 2) {
                emptyList()
            } else {
                val query = searchQuery.uppercase()
                availableCurrencies
                    .filter { currency ->
                        currency.code != selectedMainCurrency?.code &&
                                extraCurrencies.none { it.code == currency.code } &&
                                (currency.code.contains(query) ||
                                        currency.defaultName.uppercase().contains(query) ||
                                        currency.symbol.contains(query))
                    }
                    .take(5) // Limit suggestions for clean UI
            }
        }
    }

    // Show suggestions when we have filtered results
    LaunchedEffect(filteredCurrencies) {
        expanded = filteredCurrencies.isNotEmpty()
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.group_field_extra_currencies),
            style = MaterialTheme.typography.labelLarge
        )

        // Selected extra currencies as removable chips
        if (extraCurrencies.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                extraCurrencies.forEach { currency ->
                    InputChip(
                        selected = true,
                        onClick = { onCurrencyRemoved(currency) },
                        label = { Text(currency.formatDisplay()) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.group_extra_currency_remove),
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = InputChipDefaults.inputChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }

        // Search field with autocomplete using ExposedDropdownMenuBox
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text(stringResource(R.string.group_extra_currency_search)) },
                placeholder = { Text(stringResource(R.string.group_extra_currency_search_hint)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.group_extra_currency_clear),
                            modifier = Modifier.clickable {
                                searchQuery = ""
                                expanded = false
                            }
                        )
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                    capitalization = KeyboardCapitalization.Characters
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        expanded = false
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
            )

            // Only show dropdown when there are results
            if (filteredCurrencies.isNotEmpty()) {
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    filteredCurrencies.forEach { currency ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = currency.formatDisplay(),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = currency.defaultName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                onCurrencyAdded(currency)
                                searchQuery = ""
                                expanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
        }

        // Helper text
        AnimatedVisibility(visible = extraCurrencies.isEmpty() && searchQuery.isEmpty()) {
            Text(
                text = stringResource(R.string.group_field_extra_currencies_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
