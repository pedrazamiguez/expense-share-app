package es.pedrazamiguez.splittrip.features.settlement.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.CashBreakdownUiModel
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun CashBreakdownListSection(
    breakdown: ImmutableList<CashBreakdownUiModel>,
    formattedTotal: String,
    formattedTotalFees: String?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Small)
    ) {
        breakdown.forEach { item ->
            CashBreakdownEntryRow(item = item)
        }
    }

    Spacer(Modifier.height(MaterialTheme.spacing.Large))

    CashBreakdownSummaryFooter(
        formattedTotal = formattedTotal,
        formattedTotalFees = formattedTotalFees
    )
}
