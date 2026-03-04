package es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.extension.asString
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.StyledOutlinedTextField
import es.pedrazamiguez.expenseshareapp.features.balance.R
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.event.AddCashWithdrawalUiEvent
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.AddCashWithdrawalUiState

@Composable
fun AddCashWithdrawalScreen(
    groupId: String? = null,
    uiState: AddCashWithdrawalUiState,
    onEvent: (AddCashWithdrawalUiEvent) -> Unit = {}
) {
    LaunchedEffect(groupId) {
        onEvent(AddCashWithdrawalUiEvent.LoadGroupConfig(groupId))
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when {
            uiState.isReady -> {
                AddCashWithdrawalForm(
                    groupId = groupId,
                    uiState = uiState,
                    onEvent = onEvent
                )
            }

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
                        text = stringResource(R.string.withdrawal_error_load_config),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = {
                        onEvent(AddCashWithdrawalUiEvent.RetryLoadConfig(groupId))
                    }) {
                        Text(stringResource(R.string.withdrawal_retry))
                    }
                }
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // ── Amount & Currency Card ─────────────────────────────────────
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Amount withdrawn
                    StyledOutlinedTextField(
                        value = uiState.withdrawalAmount,
                        onValueChange = {
                            onEvent(AddCashWithdrawalUiEvent.WithdrawalAmountChanged(it))
                        },
                        label = stringResource(R.string.balances_withdraw_cash_amount_hint),
                        modifier = Modifier.weight(0.55f),
                        keyboardType = KeyboardType.Decimal,
                        isError = !uiState.isAmountValid,
                        imeAction = if (uiState.showExchangeRateSection) ImeAction.Next else ImeAction.Done,
                        keyboardActions = if (!uiState.showExchangeRateSection) {
                            KeyboardActions(onDone = { submitForm() })
                        } else KeyboardActions.Default
                    )

                    // Currency dropdown
                    Box(modifier = Modifier.weight(0.45f)) {
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

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            uiState.availableCurrencies.forEach { currency ->
                                DropdownMenuItem(
                                    text = { Text(currency.displayText) },
                                    onClick = {
                                        onEvent(
                                            AddCashWithdrawalUiEvent.CurrencySelected(currency.code)
                                        )
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Exchange Rate Section (only for foreign currencies) ────────
        AnimatedVisibility(visible = uiState.showExchangeRateSection) {
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StyledOutlinedTextField(
                            value = uiState.displayExchangeRate,
                            onValueChange = {
                                onEvent(AddCashWithdrawalUiEvent.ExchangeRateChanged(it))
                            },
                            label = uiState.exchangeRateLabel,
                            modifier = Modifier.weight(1f),
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        )

                        StyledOutlinedTextField(
                            value = uiState.deductedAmount,
                            onValueChange = {
                                onEvent(AddCashWithdrawalUiEvent.DeductedAmountChanged(it))
                            },
                            label = uiState.deductedAmountLabel,
                            modifier = Modifier.weight(1f),
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done,
                            keyboardActions = KeyboardActions(onDone = { submitForm() })
                        )
                    }
                }
            }
        }

        // ── Error ──────────────────────────────────────────────────────
        uiState.error?.let { errorUiText ->
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = errorUiText.asString(),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // ── Submit Button ──────────────────────────────────────────────
        Button(
            onClick = { submitForm() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = uiState.isFormValid && !uiState.isLoading,
            shape = MaterialTheme.shapes.large
        ) {
            if (uiState.isLoading) {
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

