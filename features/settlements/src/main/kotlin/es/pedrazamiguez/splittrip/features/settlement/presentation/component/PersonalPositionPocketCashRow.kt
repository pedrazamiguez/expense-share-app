package es.pedrazamiguez.splittrip.features.settlement.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.text.AmountText
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.text.CaptionText
import es.pedrazamiguez.splittrip.features.settlement.R
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.PersonalPositionUiModel

@Composable
internal fun PersonalPositionPocketCashRow(
    personalPosition: PersonalPositionUiModel,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Medium)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(MaterialTheme.spacing.Medium)
        ) {
            CaptionText(text = stringResource(R.string.my_position_label_pocket_balance))
            AmountText(text = personalPosition.formattedPocketBalance)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(MaterialTheme.spacing.Medium)
        ) {
            CaptionText(text = stringResource(R.string.my_position_label_cash_in_hand))
            AmountText(
                text = personalPosition.formattedCashInHand,
                color = if (personalPosition.hasNegativeCashInHand) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}
