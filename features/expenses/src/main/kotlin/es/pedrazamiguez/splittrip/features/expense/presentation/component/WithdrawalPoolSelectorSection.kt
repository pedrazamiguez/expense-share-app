package es.pedrazamiguez.splittrip.features.expense.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.FlatCard
import es.pedrazamiguez.splittrip.domain.enums.PayerType
import es.pedrazamiguez.splittrip.features.expense.R
import es.pedrazamiguez.splittrip.features.expense.presentation.model.WithdrawalPoolOptionUiModel
import kotlinx.collections.immutable.ImmutableList

/**
 * Displays a pool-selection widget when multiple cash withdrawal pools are available for the
 * current expense's currency and scope.
 *
 * Shown in the Exchange Rate step **above** the "Funded from" tranche breakdown, so the user
 * can pick which pool (e.g. "My personal cash" vs "Group cash") to draw from before seeing the
 * tranche details that correspond to their selection.
 *
 * Layout:
 * - Section title ("Draw cash from") styled like other wizard step headers.
 * - [FlatCard] container with a horizontal row of [FilterChip]s — one per available pool.
 *
 * @param pools         Non-empty list of available pool options (must have at least 2 entries,
 *                      caller is responsible for guarding on size > 1).
 * @param selectedPool  Currently selected pool, or null when no explicit selection has been made.
 * @param onPoolSelected Callback invoked with the selected pool's [PayerType] and subunit ID
 *                       (null for non-SUBUNIT scopes) when the user taps a chip.
 * @param modifier      Modifier applied to the root [Column].
 */
@Composable
fun WithdrawalPoolSelectorSection(
    pools: ImmutableList<WithdrawalPoolOptionUiModel>,
    selectedPool: WithdrawalPoolOptionUiModel?,
    onPoolSelected: (scope: PayerType, subunitId: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.add_expense_cash_pool_selection_title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))

        FlatCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pools.forEach { pool ->
                    val isSelected = pool == selectedPool
                    FilterChip(
                        selected = isSelected,
                        onClick = { onPoolSelected(pool.scope, pool.ownerId) },
                        label = {
                            Text(
                                text = pool.displayLabel,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        modifier = Modifier.semantics {
                            stateDescription = if (isSelected) {
                                pool.displayLabel
                            } else {
                                pool.displayLabel
                            }
                        }
                    )
                }
            }
        }
    }
}
