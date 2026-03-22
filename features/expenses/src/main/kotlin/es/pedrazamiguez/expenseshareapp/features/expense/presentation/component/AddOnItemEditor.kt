package es.pedrazamiguez.expenseshareapp.features.expense.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.StyledOutlinedTextField
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnMode
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnType
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnValueType
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.extensions.toStringRes
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.AddOnUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.CurrencyUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.PaymentMethodUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event.AddExpenseUiEvent
import kotlinx.collections.immutable.ImmutableList

/**
 * Editor for a single add-on within the expense form.
 *
 * Displays type/mode/value-type chip selectors, an amount input field,
 * optional currency and payment method dropdowns, and a description field.
 *
 * Stateless: takes pure data and emits [AddExpenseUiEvent]s via [onEvent].
 */
@Composable
fun AddOnItemEditor(
    addOn: AddOnUiModel,
    availableCurrencies: ImmutableList<CurrencyUiModel>,
    paymentMethods: ImmutableList<PaymentMethodUiModel>,
    showCurrencySelector: Boolean,
    focusManager: FocusManager,
    onEvent: (AddExpenseUiEvent) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AddOnEditorHeader(
            addOn = addOn,
            onRemove = onRemove
        )

        AddOnChipSelectors(
            addOn = addOn,
            onEvent = onEvent
        )

        AddOnAmountInput(
            addOn = addOn,
            focusManager = focusManager,
            onAmountChanged = { amount ->
                onEvent(AddExpenseUiEvent.AddOnAmountChanged(addOn.id, amount))
            }
        )

        AddOnCurrencySelector(
            addOn = addOn,
            availableCurrencies = availableCurrencies,
            showCurrencySelector = showCurrencySelector,
            onCurrencySelected = { code ->
                onEvent(AddExpenseUiEvent.AddOnCurrencySelected(addOn.id, code))
            }
        )

        AddOnExchangeRateSection(
            addOn = addOn,
            focusManager = focusManager,
            onRateChanged = { rate ->
                onEvent(AddExpenseUiEvent.AddOnExchangeRateChanged(addOn.id, rate))
            },
            onGroupAmountChanged = { amount ->
                onEvent(AddExpenseUiEvent.AddOnGroupAmountChanged(addOn.id, amount))
            }
        )

        AddOnPaymentMethodSelector(
            addOn = addOn,
            paymentMethods = paymentMethods,
            onPaymentMethodSelected = { methodId ->
                onEvent(
                    AddExpenseUiEvent.AddOnPaymentMethodSelected(addOn.id, methodId)
                )
            }
        )

        StyledOutlinedTextField(
            value = addOn.description,
            onValueChange = { desc ->
                onEvent(AddExpenseUiEvent.AddOnDescriptionChanged(addOn.id, desc))
            },
            label = stringResource(R.string.add_expense_add_on_description_hint),
            modifier = Modifier.fillMaxWidth(),
            capitalization = KeyboardCapitalization.Sentences,
            singleLine = false,
            maxLines = 2,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            )
        )

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun AddOnEditorHeader(
    addOn: AddOnUiModel,
    onRemove: () -> Unit
) {
    // ── Header: Type label + Remove button ──────────────────
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(addOn.type.toStringRes()),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(
                    R.string.add_expense_add_on_remove
                ),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun AddOnChipSelectors(
    addOn: AddOnUiModel,
    onEvent: (AddExpenseUiEvent) -> Unit
) {
    // ── Type Chips ──────────────────────────────────────────
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AddOnType.entries.forEach { type ->
            FilterChip(
                selected = addOn.type == type,
                onClick = {
                    onEvent(AddExpenseUiEvent.AddOnTypeChanged(addOn.id, type))
                },
                label = {
                    Text(
                        text = stringResource(type.toStringRes()),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            )
        }
    }

    // ── Mode Chips (On top / Included) ──────────────────────
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AddOnMode.entries.forEach { mode ->
            FilterChip(
                selected = addOn.mode == mode,
                onClick = {
                    onEvent(AddExpenseUiEvent.AddOnModeChanged(addOn.id, mode))
                },
                label = {
                    Text(
                        text = stringResource(mode.toStringRes()),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            )
        }
    }

    // ── Value Type Chips (Amount / Percentage) ──────────────
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AddOnValueType.entries.forEach { valueType ->
            FilterChip(
                selected = addOn.valueType == valueType,
                onClick = {
                    onEvent(
                        AddExpenseUiEvent.AddOnValueTypeChanged(addOn.id, valueType)
                    )
                },
                label = {
                    Text(
                        text = stringResource(valueType.toStringRes()),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            )
        }
    }
}

@Composable
private fun AddOnAmountInput(
    addOn: AddOnUiModel,
    focusManager: FocusManager,
    onAmountChanged: (String) -> Unit
) {
    // ── Amount Input ────────────────────────────────────────
    val amountSuffix = if (addOn.valueType == AddOnValueType.PERCENTAGE) "%" else null

    StyledOutlinedTextField(
        value = addOn.amountInput,
        onValueChange = onAmountChanged,
        label = stringResource(R.string.add_expense_add_on_amount_hint),
        modifier = Modifier.fillMaxWidth(),
        keyboardType = KeyboardType.Decimal,
        imeAction = ImeAction.Next,
        isError = !addOn.isAmountValid,
        suffix = amountSuffix?.let { { Text(it) } },
        keyboardActions = KeyboardActions(
            onNext = { focusManager.clearFocus() }
        )
    )
}

@Composable
private fun AddOnCurrencySelector(
    addOn: AddOnUiModel,
    availableCurrencies: ImmutableList<CurrencyUiModel>,
    showCurrencySelector: Boolean,
    onCurrencySelected: (String) -> Unit
) {
    // ── Currency Selector (only when multi-currency) ────────
    AnimatedVisibility(
        visible = showCurrencySelector,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Box {
            var currencyExpanded by remember { mutableStateOf(false) }
            StyledOutlinedTextField(
                value = addOn.currency?.displayText ?: "",
                onValueChange = {},
                readOnly = true,
                label = stringResource(R.string.add_expense_currency_label),
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                onClick = { currencyExpanded = true },
                modifier = Modifier.fillMaxWidth()
            )

            DropdownMenu(
                expanded = currencyExpanded,
                onDismissRequest = { currencyExpanded = false }
            ) {
                availableCurrencies.forEach { currency ->
                    DropdownMenuItem(
                        text = { Text(currency.displayText) },
                        onClick = {
                            onCurrencySelected(currency.code)
                            currencyExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AddOnPaymentMethodSelector(
    addOn: AddOnUiModel,
    paymentMethods: ImmutableList<PaymentMethodUiModel>,
    onPaymentMethodSelected: (String) -> Unit
) {
    // ── Payment Method Selector ─────────────────────────────
    Box {
        var methodExpanded by remember { mutableStateOf(false) }
        StyledOutlinedTextField(
            value = addOn.paymentMethod?.displayText ?: "",
            onValueChange = {},
            readOnly = true,
            label = stringResource(R.string.add_expense_payment_method_title),
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
            onClick = { methodExpanded = true },
            modifier = Modifier.fillMaxWidth()
        )

        DropdownMenu(
            expanded = methodExpanded,
            onDismissRequest = { methodExpanded = false }
        ) {
            paymentMethods.forEach { method ->
                DropdownMenuItem(
                    text = { Text(method.displayText) },
                    onClick = {
                        onPaymentMethodSelected(method.id)
                        methodExpanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun AddOnExchangeRateSection(
    addOn: AddOnUiModel,
    focusManager: FocusManager,
    onRateChanged: (String) -> Unit,
    onGroupAmountChanged: (String) -> Unit
) {
    AnimatedVisibility(
        visible = addOn.showExchangeRateSection,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.add_expense_exchange_rate_title),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (addOn.isLoadingRate) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StyledOutlinedTextField(
                        value = addOn.displayExchangeRate,
                        onValueChange = onRateChanged,
                        label = addOn.exchangeRateLabel,
                        modifier = Modifier.weight(1f),
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    )
                    StyledOutlinedTextField(
                        value = addOn.calculatedGroupAmount,
                        onValueChange = onGroupAmountChanged,
                        label = addOn.groupAmountLabel,
                        modifier = Modifier.weight(1f),
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done,
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        )
                    )
                }
            }
        }
    }
}
