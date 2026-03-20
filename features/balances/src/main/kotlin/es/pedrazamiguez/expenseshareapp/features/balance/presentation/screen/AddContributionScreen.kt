package es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.StyledOutlinedTextField
import es.pedrazamiguez.expenseshareapp.core.designsystem.transition.SharedTransitionSurface
import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType
import es.pedrazamiguez.expenseshareapp.features.balance.R
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.event.AddContributionUiEvent
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.AddContributionUiState

/**
 * Shared element transition key for the Add Money FAB -> Screen transition.
 */
const val ADD_CONTRIBUTION_SHARED_ELEMENT_KEY = "add_contribution_container"

@Composable
fun AddContributionScreen(
    groupId: String? = null,
    uiState: AddContributionUiState,
    onEvent: (AddContributionUiEvent) -> Unit = {}
) {
    LaunchedEffect(groupId) {
        onEvent(AddContributionUiEvent.LoadSubunitOptions(groupId))
    }

    val focusManager = LocalFocusManager.current

    val submitForm = {
        focusManager.clearFocus()
        if (!uiState.isLoading) {
            onEvent(AddContributionUiEvent.Submit(groupId))
        }
    }

    SharedTransitionSurface(sharedElementKey = ADD_CONTRIBUTION_SHARED_ELEMENT_KEY) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ContributionAmountCard(uiState = uiState, onEvent = onEvent, submitForm = submitForm)
            ContributionScopeCard(uiState = uiState, onEvent = onEvent)

            Button(
                onClick = { submitForm() },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.balances_add_money_submit))
            }
        }
    }
}

@Composable
private fun ContributionAmountCard(
    uiState: AddContributionUiState,
    onEvent: (AddContributionUiEvent) -> Unit,
    submitForm: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            StyledOutlinedTextField(
                value = uiState.amountInput,
                onValueChange = { onEvent(AddContributionUiEvent.UpdateAmount(it)) },
                label = stringResource(R.string.balances_add_money_amount_hint),
                modifier = Modifier.fillMaxWidth(),
                keyboardType = KeyboardType.Decimal,
                isError = uiState.amountError,
                supportingText = if (uiState.amountError) {
                    stringResource(R.string.balances_add_money_error_amount)
                } else {
                    null
                },
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(onDone = { submitForm() })
            )
        }
    }
}

@Composable
private fun ContributionScopeCard(
    uiState: AddContributionUiState,
    onEvent: (AddContributionUiEvent) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.balances_add_money_contributing_for),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Column(modifier = Modifier.selectableGroup()) {
                ScopeRadioRow(
                    text = stringResource(R.string.balances_add_money_for_group),
                    selected = uiState.contributionScope == PayerType.GROUP,
                    onClick = { onEvent(AddContributionUiEvent.ContributionScopeSelected(PayerType.GROUP)) }
                )
                uiState.subunitOptions.forEach { option ->
                    ScopeRadioRow(
                        text = stringResource(R.string.balances_add_money_for_subunit, option.name),
                        selected = uiState.contributionScope == PayerType.SUBUNIT &&
                            uiState.selectedSubunitId == option.id,
                        onClick = {
                            onEvent(AddContributionUiEvent.ContributionScopeSelected(PayerType.SUBUNIT, option.id))
                        }
                    )
                }
                ScopeRadioRow(
                    text = stringResource(R.string.balances_add_money_for_me),
                    selected = uiState.contributionScope == PayerType.USER,
                    onClick = { onEvent(AddContributionUiEvent.ContributionScopeSelected(PayerType.USER)) }
                )
            }
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
