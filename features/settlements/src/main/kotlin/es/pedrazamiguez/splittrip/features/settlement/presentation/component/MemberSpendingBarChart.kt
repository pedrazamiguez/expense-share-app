package es.pedrazamiguez.splittrip.features.settlement.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.FlatCard
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.text.SectionHeadingText
import es.pedrazamiguez.splittrip.features.settlement.R
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.MemberSpendingChartUiModel

@Composable
internal fun MemberSpendingBarChart(
    chart: MemberSpendingChartUiModel,
    isCashOnly: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    FlatCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.Default),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.ExtraLarge)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeadingText(
                    text = stringResource(
                        id = if (isCashOnly) {
                            R.string.your_balance_chart_toggle_cash_only
                        } else {
                            R.string.your_balance_chart_toggle_all_expenses
                        }
                    )
                )
                if (chart.hasCashExpenses) {
                    Switch(
                        checked = isCashOnly,
                        onCheckedChange = onToggle
                    )
                }
            }

            val globalMax = chart.bars.maxOfOrNull { it.allowanceCents } ?: 0L

            chart.bars.forEach { bar ->
                MemberSpendingBarRow(
                    bar = bar,
                    globalMaxCents = globalMax
                )
            }
        }
    }
}
