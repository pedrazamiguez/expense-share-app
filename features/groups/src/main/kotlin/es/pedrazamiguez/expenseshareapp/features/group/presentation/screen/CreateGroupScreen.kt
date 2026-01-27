package es.pedrazamiguez.expenseshareapp.features.group.presentation.screen

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewThemeWrapper
import es.pedrazamiguez.expenseshareapp.core.designsystem.transition.LocalAnimatedVisibilityScope
import es.pedrazamiguez.expenseshareapp.core.designsystem.transition.LocalSharedTransitionScope
import es.pedrazamiguez.expenseshareapp.features.group.R
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.event.CreateGroupUiEvent
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.state.CreateGroupUiState

/**
 * Shared element transition key for the Create Group FAB -> Screen transition.
 */
const val CREATE_GROUP_SHARED_ELEMENT_KEY = "create_group_container"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class, ExperimentalLayoutApi::class)
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
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(bottom = 100.dp) // Space for bottom navigation
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

                // --- 3. EXTRA CURRENCIES ---
                if (uiState.availableCurrencies.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.group_field_extra_currencies),
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = stringResource(R.string.group_field_extra_currencies_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            uiState.availableCurrencies
                                .filter { it.code != uiState.selectedCurrency?.code }
                                .forEach { currency ->
                                    val isSelected = uiState.extraCurrencies.any { it.code == currency.code }
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { onEvent(CreateGroupUiEvent.ExtraCurrencyToggled(currency)) },
                                        label = { Text(currency.formatDisplay()) },
                                        leadingIcon = if (isSelected) {
                                            { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
                                        } else null
                                    )
                                }
                        }
                    }
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
