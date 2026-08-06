package es.pedrazamiguez.splittrip.features.settlement.presentation.preview

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.foundation.SplitTripTheme
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.splittrip.features.settlement.presentation.component.MemberSpendingBarChart
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.MemberSpendingBarUiModel
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.MemberSpendingChartUiModel
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.SpilloverSegment
import kotlinx.collections.immutable.persistentListOf

@PreviewComplete
@Composable
private fun MemberSpendingBarChartPreview() {
    SplitTripTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            val chart = MemberSpendingChartUiModel(
                bars = persistentListOf(
                    MemberSpendingBarUiModel(
                        userId = "1",
                        displayName = "You",
                        isCurrentUser = true,
                        totalCashCents = 166666L,
                        formattedTotalCash = "€1,666.66",
                        formattedTotalSpent = "€3,000.00",
                        ownSpendingCents = 166666L,
                        spilloverSegments = persistentListOf(),
                        memberColorIndex = 0
                    ),
                    MemberSpendingBarUiModel(
                        userId = "2",
                        displayName = "Andrés",
                        isCurrentUser = false,
                        totalCashCents = 166666L,
                        formattedTotalCash = "€1,666.66",
                        formattedTotalSpent = "€200.00",
                        ownSpendingCents = 20000L,
                        spilloverSegments = persistentListOf(
                            SpilloverSegment(ownerColorIndex = 0, amountCents = 66667L)
                        ),
                        memberColorIndex = 1
                    ),
                    MemberSpendingBarUiModel(
                        userId = "3",
                        displayName = "Pepe",
                        isCurrentUser = false,
                        totalCashCents = 166666L,
                        formattedTotalCash = "€1,666.66",
                        formattedTotalSpent = "€0.00",
                        ownSpendingCents = 0L,
                        spilloverSegments = persistentListOf(
                            SpilloverSegment(ownerColorIndex = 0, amountCents = 66667L)
                        ),
                        memberColorIndex = 2
                    )
                ),
                formattedGroupTotal = "€5,000.00",
                isCashOnly = true
            )

            MemberSpendingBarChart(
                chart = chart,
                isCashOnly = true,
                onToggle = {}
            )
        }
    }
}
