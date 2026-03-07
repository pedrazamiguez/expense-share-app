package es.pedrazamiguez.expenseshareapp.features.expense.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.extension.asString
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event.AddExpenseUiEvent
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState

/**
 * Complete split configuration section for the Add Expense form.
 * Combines the split type selector, per-user editor, and validation error display.
 */
@Composable
fun SplitSection(
    uiState: AddExpenseUiState,
    onEvent: (AddExpenseUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedSplitType = uiState.selectedSplitType
    val isEqualMode = selectedSplitType?.id == "EQUAL"
    val isPercentMode = selectedSplitType?.id == "PERCENT"

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.add_expense_split_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        SplitTypeSelector(
            splitTypes = uiState.availableSplitTypes,
            selectedSplitType = uiState.selectedSplitType,
            onSplitTypeSelected = { splitTypeId ->
                onEvent(AddExpenseUiEvent.SplitTypeChanged(splitTypeId))
            }
        )

        if (uiState.splits.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SplitEditor(
                        splits = uiState.splits,
                        isEqualMode = isEqualMode,
                        isPercentMode = isPercentMode,
                        onAmountChanged = { userId, amount ->
                            onEvent(AddExpenseUiEvent.SplitAmountChanged(userId, amount))
                        },
                        onPercentageChanged = { userId, percentage ->
                            onEvent(AddExpenseUiEvent.SplitPercentageChanged(userId, percentage))
                        },
                        onExcludedToggled = { userId ->
                            onEvent(AddExpenseUiEvent.SplitExcludedToggled(userId))
                        }
                    )
                }
            }
        }

        // Split validation error
        uiState.splitError?.let { errorUiText ->
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = errorUiText.asString(),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

