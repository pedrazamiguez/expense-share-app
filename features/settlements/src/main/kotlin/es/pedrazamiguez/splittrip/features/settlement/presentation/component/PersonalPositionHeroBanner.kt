package es.pedrazamiguez.splittrip.features.settlement.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.AnimatedAmount
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.text.CaptionText
import es.pedrazamiguez.splittrip.features.settlement.R
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.NetPositionStatus
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.PersonalPositionUiModel

@Composable
internal fun PersonalPositionHeroBanner(
    personalPosition: PersonalPositionUiModel,
    modifier: Modifier = Modifier
) {
    val netPositionColor = when (personalPosition.netPositionStatus) {
        NetPositionStatus.POSITIVE -> MaterialTheme.colorScheme.primary
        NetPositionStatus.NEUTRAL -> MaterialTheme.colorScheme.onSurface
        NetPositionStatus.NEGATIVE -> MaterialTheme.colorScheme.error
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(MaterialTheme.spacing.Medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CaptionText(
            text = stringResource(R.string.my_position_label_net_position).uppercase()
        )
        AnimatedAmount(
            formattedAmount = personalPosition.formattedNetPosition,
            shouldAnimate = true,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = netPositionColor
        )
    }
}
