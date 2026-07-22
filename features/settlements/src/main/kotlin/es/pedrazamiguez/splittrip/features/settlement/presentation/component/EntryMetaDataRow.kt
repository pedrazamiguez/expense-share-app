package es.pedrazamiguez.splittrip.features.settlement.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.text.CaptionText

@Composable
internal fun EntryMetaDataRow(scope: String, rate: String) {
    if (scope.isBlank() && rate.isBlank()) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (scope.isNotBlank()) {
            CaptionText(text = scope)
        }
        if (rate.isNotBlank()) {
            CaptionText(text = rate)
        }
    }
}
