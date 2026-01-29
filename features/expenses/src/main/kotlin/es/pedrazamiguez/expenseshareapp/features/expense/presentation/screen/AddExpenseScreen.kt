package es.pedrazamiguez.expenseshareapp.features.expense.presentation.screen

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.transition.LocalAnimatedVisibilityScope
import es.pedrazamiguez.expenseshareapp.core.designsystem.transition.LocalSharedTransitionScope
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.extensions.toStringRes
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event.AddExpenseUiEvent
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState

/**
 * Shared element transition key for the Add Expense FAB -> Screen transition.
 */
const val ADD_EXPENSE_SHARED_ELEMENT_KEY = "add_expense_container"

@Composable
private fun PaymentMethodChips(
    paymentMethods: List<PaymentMethod>,
    selectedPaymentMethod: PaymentMethod,
    onPaymentMethodSelected: (PaymentMethod) -> Unit
) {
    // Simple wrapping: split into rows if needed
    val chunked = paymentMethods.chunked(3)

    Column(
        modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chunked.forEach { rowMethods ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowMethods.forEach { method ->
                    FilterChip(
                        selected = selectedPaymentMethod == method,
                        onClick = { onPaymentMethodSelected(method) },
                        label = { Text(stringResource(method.toStringRes())) },
                        leadingIcon = if (selectedPaymentMethod == method) {
                            { Icon(Icons.Default.Check, null) }
                        } else null,
                        modifier = Modifier.weight(1f))
                }
                // Add empty spacers if row has fewer than 3 items
                repeat(3 - rowMethods.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AddExpenseScreen(
    groupId: String? = null,
    uiState: AddExpenseUiState,
    onEvent: (AddExpenseUiEvent) -> Unit = {},
) {
    LaunchedEffect(groupId) {
        onEvent(AddExpenseUiEvent.LoadGroupConfig(groupId))
    }

    // Get shared transition scope if available
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    // Build the shared element modifier for the container
    val sharedModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            Modifier.sharedBounds(
                sharedContentState = rememberSharedContentState(key = ADD_EXPENSE_SHARED_ELEMENT_KEY),
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
        when {
            // Show the expense form when config is loaded and ready
            uiState.isReady -> {
                AddExpenseForm(
                    groupId = groupId, uiState = uiState, onEvent = onEvent
                )
            }

            // Show error screen with retry button when config fails to load
            uiState.configLoadFailed -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.expense_error_load_group_config),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.expense_error_config_retry_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { onEvent(AddExpenseUiEvent.RetryLoadConfig(groupId)) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.expense_error_retry_button))
                    }
                }
            }

            // Show loading indicator while config is loading (default/initial state)
            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun AddExpenseForm(
    groupId: String?, uiState: AddExpenseUiState, onEvent: (AddExpenseUiEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. TITLE ---
        OutlinedTextField(
            value = uiState.expenseTitle,
            onValueChange = { onEvent(AddExpenseUiEvent.TitleChanged(it)) },
            label = { Text(stringResource(R.string.add_expense_what_for)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = !uiState.isTitleValid,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )

        // --- 2. AMOUNT & CURRENCY ---
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Source Amount
            OutlinedTextField(
                value = uiState.sourceAmount,
                onValueChange = { onEvent(AddExpenseUiEvent.SourceAmountChanged(it)) },
                label = { Text(stringResource(R.string.add_expense_amount_paid)) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = !uiState.isAmountValid
            )

            // Currency Dropdown
            Box(modifier = Modifier.weight(0.4f)) {
                var expanded by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = uiState.selectedCurrency?.code ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.add_expense_currency_label)) },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                    singleLine = true,
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
                            interactionSource = remember { MutableInteractionSource() }) {
                            expanded = true
                        })

                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    uiState.availableCurrencies.forEach { currency ->
                        DropdownMenuItem(text = { Text(currency.formatDisplay()) }, onClick = {
                            onEvent(AddExpenseUiEvent.CurrencySelected(currency))
                            expanded = false
                        })
                    }
                }
            }
        }

        // --- 3. CONVERSION CARD (Conditional) ---
        AnimatedVisibility(visible = uiState.showExchangeRateSection) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ), modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.add_expense_exchange_rate_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Rate Input
                        OutlinedTextField(
                            value = uiState.exchangeRate,
                            onValueChange = { onEvent(AddExpenseUiEvent.ExchangeRateChanged(it)) },
                            label = { Text(stringResource(R.string.add_expense_rate_label)) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )

                        // Group Amount (Charged)
                        OutlinedTextField(
                            value = uiState.calculatedGroupAmount,
                            onValueChange = { onEvent(AddExpenseUiEvent.GroupAmountChanged(it)) },
                            label = {
                                Text(
                                    stringResource(
                                        R.string.add_expense_amount_in,
                                        uiState.groupCurrency?.code ?: ""
                                    )
                                )
                            },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            supportingText = { Text(stringResource(R.string.add_expense_bank_charge_hint)) })
                    }
                }
            }
        }

        // --- 4. PAYMENT METHOD ---
        Text(
            text = stringResource(R.string.add_expense_payment_method_title),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(top = 8.dp)
        )

        PaymentMethodChips(
            paymentMethods = uiState.paymentMethods,
            selectedPaymentMethod = uiState.selectedPaymentMethod,
            onPaymentMethodSelected = { onEvent(AddExpenseUiEvent.PaymentMethodSelected(it)) })

        Spacer(modifier = Modifier.height(24.dp))

        // --- 5. ERROR MESSAGE (if any) ---
        val errorText = when {
            uiState.errorRes != null -> stringResource(uiState.errorRes)
            !uiState.errorMessage.isNullOrBlank() -> uiState.errorMessage
            else -> null
        }

        if (errorText != null) {
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }

        // --- 6. SUBMIT BUTTON ---
        val isFormValid =
            uiState.isTitleValid && uiState.isAmountValid && uiState.expenseTitle.isNotBlank() && uiState.sourceAmount.isNotBlank()

        Button(
            onClick = { onEvent(AddExpenseUiEvent.SubmitAddExpense(groupId)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = isFormValid && !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(stringResource(R.string.add_expense_submit_button))
            }
        }

        // Bottom padding to ensure button is visible above bottom navigation
        Spacer(modifier = Modifier.height(80.dp))

    }
}
