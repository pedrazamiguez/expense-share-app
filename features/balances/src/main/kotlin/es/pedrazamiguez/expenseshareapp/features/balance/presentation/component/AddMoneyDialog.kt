package es.pedrazamiguez.expenseshareapp.features.balance.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.features.balance.R
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.SubunitOptionUiModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.event.BalancesUiEvent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun AddMoneyDialog(
    amountInput: String,
    amountError: Boolean,
    subunitOptions: ImmutableList<SubunitOptionUiModel> = persistentListOf(),
    selectedSubunitId: String? = null,
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
                    } else {
                        null
                    },
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

                // Sub-unit selector — only visible when user belongs to at least one sub-unit
                if (subunitOptions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.balances_add_money_contributing_for),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Column(modifier = Modifier.selectableGroup()) {
                        // "For me" option
                        SubunitRadioOption(
                            text = stringResource(R.string.balances_add_money_for_me),
                            selected = selectedSubunitId == null,
                            onClick = { onEvent(BalancesUiEvent.SelectContributionSubunit(null)) }
                        )
                        // Sub-unit options
                        subunitOptions.forEach { option ->
                            SubunitRadioOption(
                                text = stringResource(
                                    R.string.balances_add_money_for_subunit,
                                    option.name
                                ),
                                selected = selectedSubunitId == option.id,
                                onClick = {
                                    onEvent(BalancesUiEvent.SelectContributionSubunit(option.id))
                                }
                            )
                        }
                    }
                }
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

@Composable
private fun SubunitRadioOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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

