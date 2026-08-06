package es.pedrazamiguez.splittrip.features.settlement.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
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
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.ExtraSmall)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val legendColor = MemberSpendingColors[bar.memberColorIndex % MemberSpendingColors.size]
            Box(
                modifier = Modifier
                    .size(MaterialTheme.spacing.Medium)
                    .clip(RoundedCornerShape(MaterialTheme.spacing.ExtraSmall))
                    .background(legendColor)
            )
            CardTitleText(text = bar.displayName)
        }

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
