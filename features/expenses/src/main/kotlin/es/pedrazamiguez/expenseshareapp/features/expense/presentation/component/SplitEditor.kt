package es.pedrazamiguez.expenseshareapp.features.expense.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.StyledOutlinedTextField
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.SplitUiModel
import kotlinx.collections.immutable.ImmutableList

/**
 * Displays per-user split entries.
 *
 * - EQUAL mode: shows read-only calculated amounts.
 * - EXACT mode: allows editing the amount per participant.
 * - PERCENT mode: allows editing the percentage per participant.
 */
@Composable
fun SplitEditor(
    splits: ImmutableList<SplitUiModel>,
    isEqualMode: Boolean,
    isPercentMode: Boolean,
    onAmountChanged: (userId: String, amount: String) -> Unit,
    onPercentageChanged: (userId: String, percentage: String) -> Unit,
    onExcludedToggled: (userId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        splits.forEach { split ->
            SplitMemberRow(
                split = split,
                isEqualMode = isEqualMode,
                isPercentMode = isPercentMode,
                onAmountChanged = { amount -> onAmountChanged(split.userId, amount) },
                onPercentageChanged = { pct -> onPercentageChanged(split.userId, pct) },
                onExcludedToggled = { onExcludedToggled(split.userId) },
                onDone = { focusManager.clearFocus() }
            )
        }
    }
}

@Composable
private fun SplitMemberRow(
    split: SplitUiModel,
    isEqualMode: Boolean,
    isPercentMode: Boolean,
    onAmountChanged: (String) -> Unit,
    onPercentageChanged: (String) -> Unit,
    onExcludedToggled: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Member name
        Text(
            text = split.displayName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = if (split.isExcluded) {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )

        AnimatedVisibility(visible = !split.isExcluded) {
            if (isEqualMode) {
                // Read-only display of calculated share
                Text(
                    text = split.formattedAmount,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (isPercentMode) {
                StyledOutlinedTextField(
                    value = split.percentageInput,
                    onValueChange = onPercentageChanged,
                    label = stringResource(R.string.add_expense_split_percentage_label),
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next,
                    keyboardActions = KeyboardActions(onDone = { onDone() })
                )
            } else {
                // EXACT mode
                StyledOutlinedTextField(
                    value = split.amountInput,
                    onValueChange = onAmountChanged,
                    label = stringResource(R.string.add_expense_split_amount_label),
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next,
                    keyboardActions = KeyboardActions(onDone = { onDone() })
                )
            }
        }

        // Exclude toggle
        Switch(
            checked = !split.isExcluded,
            onCheckedChange = { onExcludedToggled() }
        )
    }
}

