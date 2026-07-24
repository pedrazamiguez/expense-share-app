package es.pedrazamiguez.splittrip.features.settlement.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.form.GradientButton
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.form.SecondaryButton
import es.pedrazamiguez.splittrip.features.settlement.R
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.SettlementConsensusItemUiModel

@Composable
internal fun ConsensusCardActions(
    item: SettlementConsensusItemUiModel,
    onConfirm: () -> Unit,
    onDispute: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Small)
    ) {
        if (item.canConfirm) {
            GradientButton(
                text = item.confirmLabel,
                onClick = onConfirm,
                modifier = if (item.canDispute) Modifier.weight(1f) else Modifier.fillMaxWidth()
            )
        }
        if (item.canDispute) {
            SecondaryButton(
                text = stringResource(R.string.your_position_settlement_dispute),
                onClick = onDispute,
                modifier = if (item.canConfirm) Modifier.weight(1f) else Modifier.fillMaxWidth()
            )
        }
    }
}
