package es.pedrazamiguez.splittrip.features.settlement.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import es.pedrazamiguez.splittrip.core.designsystem.foundation.ChartColors
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.chart.ChartLegendItem
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.text.CaptionText
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.MemberSpendingBarUiModel

@Composable
internal fun MemberSpendingBarRow(
    bar: MemberSpendingBarUiModel,
    globalMaxCents: Long,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.ExtraSmall)
    ) {
        ChartLegendItem(
            color = ChartColors[bar.memberColorIndex % ChartColors.size],
            label = bar.displayName
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CaptionText(text = bar.formattedTotalSpent)
            CaptionText(text = bar.formattedAllowance)
        }

        AnimatedSpendingBar(
            bar = bar,
            globalMaxCents = globalMaxCents,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
