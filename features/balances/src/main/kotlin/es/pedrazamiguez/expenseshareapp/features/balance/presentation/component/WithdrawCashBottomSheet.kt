package es.pedrazamiguez.expenseshareapp.features.balance.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.features.balance.R
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.event.BalancesUiEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawCashBottomSheet(
    amountInput: String,
    currencyInput: String,
    deductedInput: String,
    exchangeRateInput: String,
    amountError: Boolean,
    deductedError: Boolean,
    onEvent: (BalancesUiEvent) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { onEvent(BalancesUiEvent.DismissWithdrawCashSheet) },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.balances_withdraw_cash_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Currency input
            OutlinedTextField(
                value = currencyInput,
                onValueChange = { onEvent(BalancesUiEvent.UpdateWithdrawalCurrency(it)) },
                label = { Text(stringResource(R.string.balances_withdraw_cash_currency_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Amount withdrawn
            OutlinedTextField(
                value = amountInput,
                onValueChange = { onEvent(BalancesUiEvent.UpdateWithdrawalAmount(it)) },
                label = { Text(stringResource(R.string.balances_withdraw_cash_amount_hint)) },
                isError = amountError,
                supportingText = if (amountError) {
                    { Text(stringResource(R.string.balances_withdraw_cash_error_amount)) }
                } else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Deducted base amount
            OutlinedTextField(
                value = deductedInput,
                onValueChange = { onEvent(BalancesUiEvent.UpdateWithdrawalDeducted(it)) },
                label = { Text(stringResource(R.string.balances_withdraw_cash_deducted_hint)) },
                isError = deductedError,
                supportingText = if (deductedError) {
                    { Text(stringResource(R.string.balances_withdraw_cash_error_deducted)) }
                } else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Exchange rate (read-only / auto-calculated, but editable)
            OutlinedTextField(
                value = exchangeRateInput,
                onValueChange = { onEvent(BalancesUiEvent.UpdateWithdrawalExchangeRate(it)) },
                label = { Text(stringResource(R.string.balances_withdraw_cash_rate_hint)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Submit button
            Button(
                onClick = { onEvent(BalancesUiEvent.SubmitWithdrawal) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.balances_withdraw_cash_submit))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

