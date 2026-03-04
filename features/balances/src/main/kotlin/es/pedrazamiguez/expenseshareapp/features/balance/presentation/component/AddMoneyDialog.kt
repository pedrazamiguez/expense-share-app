package es.pedrazamiguez.expenseshareapp.features.balance.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.features.balance.R
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.event.BalancesUiEvent

@Composable
fun AddMoneyDialog(
    amountInput: String,
    amountError: Boolean,
    onEvent: (BalancesUiEvent) -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = { onEvent(BalancesUiEvent.DismissAddMoneyDialog) },
        title = {
            Text(
                text = stringResource(R.string.balances_add_money_title),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { onEvent(BalancesUiEvent.UpdateContributionAmount(it)) },
                    label = { Text(stringResource(R.string.balances_add_money_amount_hint)) },
                    isError = amountError,
                    supportingText = if (amountError) {
                        { Text(stringResource(R.string.balances_add_money_error_amount)) }
                    } else null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onEvent(BalancesUiEvent.SubmitContribution) }
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = { onEvent(BalancesUiEvent.SubmitContribution) }
            ) {
                Text(stringResource(R.string.balances_add_money_submit))
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(
                onClick = { onEvent(BalancesUiEvent.DismissAddMoneyDialog) }
            ) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}

