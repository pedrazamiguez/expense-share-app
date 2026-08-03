package es.pedrazamiguez.splittrip.features.settlement.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.text.CaptionText
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.text.CardTitleText
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.MemberSpendingBarUiModel

@Composable
internal fun MemberSpendingBarRow(
    bar: MemberSpendingBarUiModel,
    globalMaxCents: Long,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        CardTitleText(text = bar.displayName)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CaptionText(text = "0")
            CaptionText(text = bar.formattedTotalCash)
        }

        AnimatedSpendingBar(
            bar = bar,
            globalMaxCents = globalMaxCents,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
