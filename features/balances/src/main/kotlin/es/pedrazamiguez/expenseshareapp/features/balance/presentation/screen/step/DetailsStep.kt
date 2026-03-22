package es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen.step

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.StyledOutlinedTextField
import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType
import es.pedrazamiguez.expenseshareapp.features.balance.R
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.event.AddCashWithdrawalUiEvent
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.AddCashWithdrawalUiState

/**
 * Step 4: Scope selector, title, notes, and ATM fee toggle.
 *
 * The ATM fee toggle lives here so the user can opt-in without having a dedicated
 * step visible by default. When toggled on, the ATM_FEE step is dynamically added.
 */
@Composable
fun DetailsStep(
    uiState: AddCashWithdrawalUiState,
    onEvent: (AddCashWithdrawalUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        WithdrawalScopeCard(uiState = uiState, onEvent = onEvent)
        TitleNotesCard(uiState = uiState, onEvent = onEvent)
        AtmFeeToggleCard(uiState = uiState, onEvent = onEvent)
    }
}

@Composable
private fun WithdrawalScopeCard(
    uiState: AddCashWithdrawalUiState,
    onEvent: (AddCashWithdrawalUiEvent) -> Unit
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
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.balances_withdraw_cash_withdrawing_for),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Column(modifier = Modifier.selectableGroup()) {
                ScopeRadioRow(
                    text = stringResource(R.string.balances_withdraw_cash_for_group),
                    selected = uiState.withdrawalScope == PayerType.GROUP,
                    onClick = {
                        onEvent(AddCashWithdrawalUiEvent.WithdrawalScopeSelected(PayerType.GROUP))
                    }
                )
                uiState.subunitOptions.forEach { option ->
                    ScopeRadioRow(
                        text = stringResource(R.string.balances_withdraw_cash_for_subunit, option.name),
                        selected = uiState.withdrawalScope == PayerType.SUBUNIT &&
                            uiState.selectedSubunitId == option.id,
                        onClick = {
                            onEvent(
                                AddCashWithdrawalUiEvent.WithdrawalScopeSelected(
                                    PayerType.SUBUNIT,
                                    option.id
                                )
                            )
                        }
                    )
                }
                ScopeRadioRow(
                    text = stringResource(R.string.balances_withdraw_cash_for_me),
                    selected = uiState.withdrawalScope == PayerType.USER,
                    onClick = {
                        onEvent(AddCashWithdrawalUiEvent.WithdrawalScopeSelected(PayerType.USER))
                    }
                )
            }
        }
    }
}

@Composable
private fun TitleNotesCard(
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
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )
        }
    }
}

@Composable
private fun AtmFeeToggleCard(
    uiState: AddCashWithdrawalUiState,
    onEvent: (AddCashWithdrawalUiEvent) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
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
                checked = uiState.hasFee,
                onCheckedChange = { onEvent(AddCashWithdrawalUiEvent.FeeToggled(it)) }
            )
        }
    }
}

@Composable
private fun ScopeRadioRow(
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
