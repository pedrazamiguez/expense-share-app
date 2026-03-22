package es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.extension.asString
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.LocalBottomPadding
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.StyledOutlinedTextField
import es.pedrazamiguez.expenseshareapp.core.designsystem.transition.SharedTransitionSurface
import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType
import es.pedrazamiguez.expenseshareapp.features.balance.R
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.CurrencyUiModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.event.AddCashWithdrawalUiEvent
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.AddCashWithdrawalUiState
import kotlinx.collections.immutable.ImmutableList

/**
 * Shared element transition key for the Withdraw Cash FAB -> Screen transition.
 */
const val ADD_CASH_WITHDRAWAL_SHARED_ELEMENT_KEY = "add_cash_withdrawal_container"

/** Weight ratio for amount input field vs currency dropdown */
private const val AMOUNT_FIELD_WEIGHT = 0.55f
private const val CURRENCY_FIELD_WEIGHT = 0.45f

@Composable
fun AddCashWithdrawalScreen(
    groupId: String? = null,
    uiState: AddCashWithdrawalUiState,
    onEvent: (AddCashWithdrawalUiEvent) -> Unit = {}
) {
    LaunchedEffect(groupId) {
        onEvent(AddCashWithdrawalUiEvent.LoadGroupConfig(groupId))
    }

    SharedTransitionSurface(sharedElementKey = ADD_CASH_WITHDRAWAL_SHARED_ELEMENT_KEY) {
        when {
            uiState.isReady -> {
                AddCashWithdrawalForm(
                    groupId = groupId,
                    uiState = uiState,
                    onEvent = onEvent
                )
            }

            uiState.configLoadFailed -> {
                WithdrawalConfigLoadFailedContent(
                    onRetry = { onEvent(AddCashWithdrawalUiEvent.RetryLoadConfig(groupId)) }
                )
            }

            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun AddCashWithdrawalForm(
    groupId: String?,
    uiState: AddCashWithdrawalUiState,
    onEvent: (AddCashWithdrawalUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    val submitForm = {
        focusManager.clearFocus()
        if (uiState.isFormValid && !uiState.isLoading) {
            onEvent(AddCashWithdrawalUiEvent.SubmitWithdrawal(groupId))
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.ime)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AddCashWithdrawalFormContent(
                uiState = uiState,
                onEvent = onEvent,
                submitForm = submitForm,
                modifier = Modifier.weight(1f)
            )

            WithdrawalSubmitButton(
                isLoading = uiState.isLoading,
                isEnabled = uiState.isFormValid && !uiState.isLoading,
                onSubmit = submitForm
            )
        }
    }
}

@Composable
private fun AddCashWithdrawalFormContent(
    uiState: AddCashWithdrawalUiState,
    onEvent: (AddCashWithdrawalUiEvent) -> Unit,
    submitForm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // ── Amount & Currency Card ─────────────────────────────────────
        WithdrawalAmountCard(uiState = uiState, onEvent = onEvent, submitForm = submitForm)

        // ── Exchange Rate Section (only for foreign currencies) ────────
        AnimatedVisibility(visible = uiState.showExchangeRateSection) {
            WithdrawalExchangeRateCard(uiState = uiState, onEvent = onEvent, submitForm = submitForm)
        }

        // ── Details Card (Title + Notes) ───────────────────────────────
        WithdrawalDetailsCard(uiState = uiState, onEvent = onEvent)

        // ── Withdrawal Scope Selector ──────────────────────────────────
        WithdrawalScopeCard(uiState = uiState, onEvent = onEvent)

        // ── ATM Fee Section (toggle + amount/currency) ─────────────────
        AtmFeeSection(
            uiState = uiState,
            onEvent = onEvent,
            submitForm = submitForm
        )

        // ── Fee Exchange Rate (only when fee is in foreign currency) ───
        AnimatedVisibility(visible = uiState.hasFee && uiState.showFeeExchangeRateSection) {
            FeeExchangeRateCard(
                uiState = uiState,
                onEvent = onEvent,
                submitForm = submitForm
            )
        }

        // ── Error ──────────────────────────────────────────────────────
        uiState.error?.let { WithdrawalErrorSurface(errorText = it.asString()) }
    }
}

@Composable
private fun WithdrawalConfigLoadFailedContent(onRetry: () -> Unit) {
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
            text = stringResource(R.string.withdrawal_error_load_config),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.withdrawal_retry))
        }
    }
}

@Composable
private fun WithdrawalAmountCard(
    uiState: AddCashWithdrawalUiState,
    onEvent: (AddCashWithdrawalUiEvent) -> Unit,
    submitForm: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StyledOutlinedTextField(
                    value = uiState.withdrawalAmount,
                    onValueChange = { onEvent(AddCashWithdrawalUiEvent.WithdrawalAmountChanged(it)) },
                    label = stringResource(R.string.balances_withdraw_cash_amount_hint),
                    modifier = Modifier.weight(AMOUNT_FIELD_WEIGHT),
                    keyboardType = KeyboardType.Decimal,
                    isError = !uiState.isAmountValid,
                    imeAction = if (uiState.showExchangeRateSection) ImeAction.Next else ImeAction.Done,
                    keyboardActions = if (!uiState.showExchangeRateSection) {
                        KeyboardActions(onDone = { submitForm() })
                    } else {
                        KeyboardActions.Default
                    }
                )
                WithdrawalCurrencyDropdown(
                    uiState = uiState,
                    onEvent = onEvent,
                    modifier = Modifier.weight(CURRENCY_FIELD_WEIGHT)
                )
            }
        }
    }
}

@Composable
private fun WithdrawalDetailsCard(
    uiState: AddCashWithdrawalUiState,
    onEvent: (AddCashWithdrawalUiEvent) -> Unit
) {
    val focusManager = LocalFocusManager.current

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
            Text(
                text = stringResource(R.string.withdrawal_details_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            StyledOutlinedTextField(
                value = uiState.title,
                onValueChange = { onEvent(AddCashWithdrawalUiEvent.TitleChanged(it)) },
                label = stringResource(R.string.withdrawal_details_title_hint),
                modifier = Modifier.fillMaxWidth(),
                capitalization = KeyboardCapitalization.Sentences,
                singleLine = true,
                imeAction = ImeAction.Next
            )
            StyledOutlinedTextField(
                value = uiState.notes,
                onValueChange = { onEvent(AddCashWithdrawalUiEvent.NotesChanged(it)) },
                label = stringResource(R.string.withdrawal_details_notes_hint),
                modifier = Modifier.fillMaxWidth(),
                capitalization = KeyboardCapitalization.Sentences,
                singleLine = false,
                maxLines = 3,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )
        }
    }
}

@Composable
private fun WithdrawalCurrencyDropdown(
    uiState: AddCashWithdrawalUiState,
    onEvent: (AddCashWithdrawalUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        var expanded by remember { mutableStateOf(false) }
        StyledOutlinedTextField(
            value = uiState.selectedCurrency?.displayText ?: "",
            onValueChange = {},
            readOnly = true,
            label = stringResource(R.string.balances_withdraw_cash_currency_hint),
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            uiState.availableCurrencies.forEach { currency ->
                DropdownMenuItem(
                    text = { Text(currency.displayText) },
                    onClick = {
                        onEvent(AddCashWithdrawalUiEvent.CurrencySelected(currency.code))
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun WithdrawalExchangeRateCard(
    uiState: AddCashWithdrawalUiState,
    onEvent: (AddCashWithdrawalUiEvent) -> Unit,
    submitForm: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.withdrawal_exchange_rate_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (uiState.isLoadingRate) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StyledOutlinedTextField(
                    value = uiState.displayExchangeRate,
                    onValueChange = { onEvent(AddCashWithdrawalUiEvent.ExchangeRateChanged(it)) },
                    label = uiState.exchangeRateLabel,
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                )
                StyledOutlinedTextField(
                    value = uiState.deductedAmount,
                    onValueChange = { onEvent(AddCashWithdrawalUiEvent.DeductedAmountChanged(it)) },
                    label = uiState.deductedAmountLabel,
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Decimal,
                    imeAction = if (uiState.hasFee) ImeAction.Next else ImeAction.Done,
                    keyboardActions = if (!uiState.hasFee) {
                        KeyboardActions(onDone = { submitForm() })
                    } else {
                        KeyboardActions.Default
                    }
                )
            }
        }
    }
}

@Composable
private fun WithdrawalScopeCard(
    uiState: AddCashWithdrawalUiState,
    onEvent: (AddCashWithdrawalUiEvent) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.balances_withdraw_cash_withdrawing_for),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Column(modifier = Modifier.selectableGroup()) {
                WithdrawalScopeRadioRow(
                    text = stringResource(R.string.balances_withdraw_cash_for_group),
                    selected = uiState.withdrawalScope == PayerType.GROUP,
                    onClick = { onEvent(AddCashWithdrawalUiEvent.WithdrawalScopeSelected(PayerType.GROUP)) }
                )
                uiState.subunitOptions.forEach { option ->
                    WithdrawalScopeRadioRow(
                        text = stringResource(R.string.balances_withdraw_cash_for_subunit, option.name),
                        selected = uiState.withdrawalScope == PayerType.SUBUNIT &&
                            uiState.selectedSubunitId == option.id,
                        onClick = {
                            onEvent(AddCashWithdrawalUiEvent.WithdrawalScopeSelected(PayerType.SUBUNIT, option.id))
                        }
                    )
                }
                WithdrawalScopeRadioRow(
                    text = stringResource(R.string.balances_withdraw_cash_for_me),
                    selected = uiState.withdrawalScope == PayerType.USER,
                    onClick = { onEvent(AddCashWithdrawalUiEvent.WithdrawalScopeSelected(PayerType.USER)) }
                )
            }
        }
    }
}

@Composable
private fun WithdrawalErrorSurface(errorText: String) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = errorText,
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WithdrawalSubmitButton(isLoading: Boolean, isEnabled: Boolean, onSubmit: () -> Unit) {
    val bottomNavPadding = LocalBottomPadding.current
    val isKeyboardVisible = WindowInsets.isImeVisible
    val effectiveBottomPadding = if (isKeyboardVisible) 12.dp else 12.dp + bottomNavPadding

    Surface(
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = effectiveBottomPadding)
                .height(56.dp),
            enabled = isEnabled,
            shape = MaterialTheme.shapes.large
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = stringResource(R.string.balances_withdraw_cash_submit),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun WithdrawalScopeRadioRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun AtmFeeSection(
    uiState: AddCashWithdrawalUiState,
    onEvent: (AddCashWithdrawalUiEvent) -> Unit,
    submitForm: () -> Unit
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
            AtmFeeHeader(
                hasFee = uiState.hasFee,
                onToggle = { onEvent(AddCashWithdrawalUiEvent.FeeToggled(it)) }
            )

            AnimatedVisibility(visible = uiState.hasFee) {
                FeeAmountAndCurrencyRow(
                    uiState = uiState,
                    onEvent = onEvent,
                    submitForm = submitForm
                )
            }
        }
    }
}

@Composable
private fun AtmFeeHeader(
    hasFee: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.withdrawal_fee_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Switch(
            checked = hasFee,
            onCheckedChange = onToggle
        )
    }
}

@Composable
private fun FeeAmountAndCurrencyRow(
    uiState: AddCashWithdrawalUiState,
    onEvent: (AddCashWithdrawalUiEvent) -> Unit,
    submitForm: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StyledOutlinedTextField(
            value = uiState.feeAmount,
            onValueChange = { onEvent(AddCashWithdrawalUiEvent.FeeAmountChanged(it)) },
            label = stringResource(R.string.withdrawal_fee_amount_hint),
            modifier = Modifier.weight(AMOUNT_FIELD_WEIGHT),
            keyboardType = KeyboardType.Decimal,
            isError = !uiState.isFeeAmountValid,
            imeAction = if (uiState.showFeeExchangeRateSection) ImeAction.Next else ImeAction.Done,
            keyboardActions = if (!uiState.showFeeExchangeRateSection) {
                KeyboardActions(onDone = { submitForm() })
            } else {
                KeyboardActions.Default
            }
        )

        FeeCurrencyDropdown(
            selectedCurrency = uiState.feeCurrency,
            availableCurrencies = uiState.availableCurrencies,
            onCurrencySelected = { onEvent(AddCashWithdrawalUiEvent.FeeCurrencySelected(it)) },
            modifier = Modifier.weight(CURRENCY_FIELD_WEIGHT)
        )
    }
}

@Composable
private fun FeeCurrencyDropdown(
    selectedCurrency: CurrencyUiModel?,
    availableCurrencies: ImmutableList<CurrencyUiModel>,
    onCurrencySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        var expanded by remember { mutableStateOf(false) }
        StyledOutlinedTextField(
            value = selectedCurrency?.displayText ?: "",
            onValueChange = {},
            readOnly = true,
            label = stringResource(R.string.withdrawal_fee_currency_hint),
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availableCurrencies.forEach { currency ->
                DropdownMenuItem(
                    text = { Text(currency.displayText) },
                    onClick = {
                        onCurrencySelected(currency.code)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun FeeExchangeRateCard(
    uiState: AddCashWithdrawalUiState,
    onEvent: (AddCashWithdrawalUiEvent) -> Unit,
    submitForm: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.withdrawal_fee_exchange_rate_title),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StyledOutlinedTextField(
                    value = uiState.feeExchangeRate,
                    onValueChange = { onEvent(AddCashWithdrawalUiEvent.FeeExchangeRateChanged(it)) },
                    label = uiState.feeExchangeRateLabel,
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                )

                StyledOutlinedTextField(
                    value = uiState.feeConvertedAmount,
                    onValueChange = { onEvent(AddCashWithdrawalUiEvent.FeeConvertedAmountChanged(it)) },
                    label = uiState.feeConvertedLabel,
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(onDone = { submitForm() })
                )
            }
        }
    }
}
