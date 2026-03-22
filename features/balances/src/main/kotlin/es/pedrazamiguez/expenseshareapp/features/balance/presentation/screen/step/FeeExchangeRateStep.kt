package es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen.step

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.StyledOutlinedTextField
import es.pedrazamiguez.expenseshareapp.features.balance.R
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.event.AddCashWithdrawalUiEvent
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.AddCashWithdrawalUiState

/**
 * Step FEE_EXCHANGE_RATE: ATM fee exchange rate + converted amount in group currency.
 *
 * Only shown when the fee currency differs from the group currency, mirroring the
 * [ExchangeRateStep] pattern used for the withdrawal amount.
 */
@Composable
fun FeeExchangeRateStep(
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
        FeeExchangeRateCard(uiState = uiState, onEvent = onEvent)
    }
}

@Composable
private fun FeeExchangeRateCard(
    uiState: AddCashWithdrawalUiState,
    onEvent: (AddCashWithdrawalUiEvent) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.withdrawal_fee_exchange_rate_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                    imeAction = ImeAction.Done
                )
            }
        }
    }
}
