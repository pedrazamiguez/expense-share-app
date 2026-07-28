package es.pedrazamiguez.splittrip.features.settlement.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.CashBreakdownUiModel

@Composable
internal fun CashBreakdownEntryRow(
    item: CashBreakdownUiModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(MaterialTheme.spacing.Medium),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.ExtraSmall)
    ) {
        EntryHeaderRow(label = item.withdrawalLabel, amount = item.formattedNativeRemaining)
        EntrySubRow(date = item.dateText, equivalent = item.formattedEquivalent)
        EntryMetaDataRow(scope = item.scopeLabel, rate = item.formattedRate)
        EntryFeesRow(fees = item.formattedAddOns)
    }
}
