package es.pedrazamiguez.splittrip.features.settlement.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.BuildingBank
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Cash
import es.pedrazamiguez.splittrip.features.settlement.R
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.PersonalPositionUiModel

@Composable
internal fun YourPositionPocketCashRow(
    personalPosition: PersonalPositionUiModel,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Medium)
    ) {
        PocketCashTile(
            icon = TablerIcons.Outline.BuildingBank,
            label = stringResource(R.string.your_position_label_pocket_balance),
            formattedAmount = personalPosition.formattedPocketBalance
        )

        PocketCashTile(
            icon = TablerIcons.Outline.Cash,
            label = stringResource(R.string.your_position_label_cash_in_hand),
            formattedAmount = personalPosition.formattedCashInHand,
            amountColor = if (personalPosition.hasNegativeCashInHand) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}
