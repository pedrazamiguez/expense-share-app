package es.pedrazamiguez.splittrip.features.expense.presentation.component.step.expense

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.currency.CurrencyConversionCard
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.currency.CurrencyConversionCardState
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.FlatCard
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.wizard.WizardStepLayout
import es.pedrazamiguez.splittrip.features.expense.R
import es.pedrazamiguez.splittrip.features.expense.presentation.model.CashTranchePreviewUiModel
import es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.event.AddExpenseUiEvent
import es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.state.AddExpenseUiState

/**
 * Step 4: Exchange rate + calculated group amount.
 * Only shown when a foreign currency is selected.
 *
 * When the payment method is CASH and a positive amount has been entered, a "Funded from"
 * section is shown below the conversion card, listing the ATM withdrawal tranche(s) that
 * will cover this expense. This is a read-only preview — actual FIFO tranches are
 * determined at save time.
 */
@Composable
fun ExchangeRateStep(
    uiState: AddExpenseUiState,
    onEvent: (AddExpenseUiEvent) -> Unit,
    onImeNext: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    WizardStepLayout(modifier = modifier) {
        CurrencyConversionCard(
            state = CurrencyConversionCardState(
                title = stringResource(R.string.add_expense_exchange_rate_title),
                exchangeRateValue = uiState.displayExchangeRate,
                exchangeRateLabel = uiState.exchangeRateLabel,
                groupAmountValue = uiState.calculatedGroupAmount,
                groupAmountLabel = uiState.groupAmountLabel,
                isLoadingRate = uiState.isLoadingRate,
                isExchangeRateLocked = uiState.isExchangeRateLocked,
                exchangeRateLockedHint = uiState.exchangeRateLockedHint,
                isInsufficientCash = uiState.isInsufficientCash,
                isGroupAmountError = !uiState.isAmountValid,
                isExchangeRateStale = uiState.isExchangeRateStale,
                autoFocus = true
            ),
            onExchangeRateChanged = { onEvent(AddExpenseUiEvent.ExchangeRateChanged(it)) },
            onGroupAmountChanged = { onEvent(AddExpenseUiEvent.GroupAmountChanged(it)) },
            onDone = onImeNext
        )

        if (uiState.cashTranchePreviews.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            CashTrancheFundedFromSection(tranches = uiState.cashTranchePreviews)
        }
    }
}

@Composable
private fun CashTrancheFundedFromSection(
    tranches: List<CashTranchePreviewUiModel>,
    modifier: Modifier = Modifier
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val isMultiTranche = tranches.size > 1

    FlatCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = if (isMultiTranche) Modifier.clickable { isExpanded = !isExpanded } else Modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.add_expense_cash_tranche_funded_from),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                if (isMultiTranche) {
                    Text(
                        text = if (isExpanded) {
                            "▲"
                        } else {
                            pluralStringResource(
                                R.plurals.add_expense_cash_tranche_count,
                                tranches.size,
                                tranches.size
                            )
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!isMultiTranche || isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                tranches.forEach { tranche ->
                    CashTrancheRow(tranche = tranche)
                    Spacer(modifier = Modifier.height(6.dp))
                }
            } else {
                // Collapsed multi-tranche: show only first tranche as a hint
                Spacer(modifier = Modifier.height(8.dp))
                CashTrancheRow(tranche = tranches.first())
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.add_expense_cash_tranche_disclaimer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CashTrancheRow(
    tranche: CashTranchePreviewUiModel,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = tranche.withdrawalLabel,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = tranche.formattedAmountConsumed,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "@ ${tranche.formattedRate}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = stringResource(R.string.add_expense_cash_tranche_remaining, tranche.formattedRemainingAfter),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
