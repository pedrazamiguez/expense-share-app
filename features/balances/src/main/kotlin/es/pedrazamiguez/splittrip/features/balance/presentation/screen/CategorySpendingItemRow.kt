package es.pedrazamiguez.splittrip.features.balance.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.chart.ChartLegendItem
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.text.CaptionText
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.text.CardTitleText
import es.pedrazamiguez.splittrip.features.balance.presentation.model.CategorySpendingUiModel
import java.text.NumberFormat

@Composable
fun CategorySpendingItemRow(
    item: CategorySpendingUiModel,
    modifier: Modifier = Modifier
) {
    val progressPercent = NumberFormat.getPercentInstance().apply {
        maximumFractionDigits = 1
    }.format(item.progress)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ChartLegendItem(
            color = item.color,
            label = item.category.name, // Will ideally need string resource mapping
            modifier = Modifier.weight(1f)
        )

        Column(
            horizontalAlignment = Alignment.End
        ) {
            CardTitleText(text = item.formattedAmount)
            CaptionText(text = progressPercent)
        }
    }
}
