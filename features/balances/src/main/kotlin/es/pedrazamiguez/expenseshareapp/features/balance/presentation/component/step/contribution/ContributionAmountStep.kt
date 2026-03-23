package es.pedrazamiguez.expenseshareapp.features.balance.presentation.component.step.contribution

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.StyledOutlinedTextField
import es.pedrazamiguez.expenseshareapp.features.balance.R
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.event.AddContributionUiEvent
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.AddContributionUiState

/**
 * Step 1: Amount input.
 * The amount field auto-focuses so the keyboard opens immediately.
 */
@Composable
fun ContributionAmountStep(
    uiState: AddContributionUiState,
    onEvent: (AddContributionUiEvent) -> Unit,
    onSubmitKeyboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        AmountCard(
            uiState = uiState,
            onEvent = onEvent,
            onSubmitKeyboard = onSubmitKeyboard,
            focusRequester = focusRequester,
            focusManager = focusManager
        )
    }
}

@Composable
private fun AmountCard(
    uiState: AddContributionUiState,
    onEvent: (AddContributionUiEvent) -> Unit,
    onSubmitKeyboard: () -> Unit,
    focusRequester: FocusRequester,
    focusManager: FocusManager
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
            StyledOutlinedTextField(
                value = uiState.amountInput,
                onValueChange = { onEvent(AddContributionUiEvent.UpdateAmount(it)) },
                label = stringResource(R.string.balances_add_money_amount_hint),
                modifier = Modifier.fillMaxWidth(),
                keyboardType = KeyboardType.Decimal,
                isError = uiState.amountError,
                suffix = uiState.groupCurrencySymbol.takeIf { it.isNotBlank() }?.let { symbol ->
                    {
                        Text(
                            text = symbol,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                supportingText = if (uiState.amountError) {
                    stringResource(R.string.balances_add_money_error_amount)
                } else {
                    null
                },
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (uiState.isCurrentStepValid) {
                            onSubmitKeyboard()
                        }
                    }
                ),
                focusRequester = focusRequester
            )
        }
    }
}
