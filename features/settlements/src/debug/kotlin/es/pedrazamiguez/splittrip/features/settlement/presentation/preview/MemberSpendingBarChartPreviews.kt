package es.pedrazamiguez.splittrip.features.settlement.presentation.preview

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.splittrip.features.settlement.presentation.component.MemberSpendingBarChart

@PreviewComplete
@Composable
private fun MemberSpendingBarChartPreviewScenarioA() {
    MemberSpendingBarChartPreviewHelper(
        domainBalances = PREVIEW_MEMBER_BALANCES_SCENARIO_A
    ) { chart ->
        Surface(modifier = Modifier.padding(16.dp)) {
            MemberSpendingBarChart(
                chart = chart,
                isCashOnly = true,
                onToggle = {}
            )
        }
    }
}

@PreviewComplete
@Composable
private fun MemberSpendingBarChartPreviewScenarioB() {
    MemberSpendingBarChartPreviewHelper(
        domainBalances = PREVIEW_MEMBER_BALANCES_SCENARIO_B
    ) { chart ->
        Surface(modifier = Modifier.padding(16.dp)) {
            MemberSpendingBarChart(
                chart = chart,
                isCashOnly = true,
                onToggle = {}
            )
        }
    }
}
