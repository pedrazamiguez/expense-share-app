package es.pedrazamiguez.splittrip.features.group.presentation.component

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
import es.pedrazamiguez.splittrip.features.group.R

@Composable
internal fun SettlementActionButtons(
    canConfirm: Boolean,
    canDispute: Boolean,
    onConfirm: () -> Unit,
    onDispute: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Small)
    ) {
        if (canConfirm) {
            GradientButton(
                text = stringResource(R.string.settlement_overview_confirm),
                onClick = onConfirm,
                modifier = Modifier.weight(1f)
            )
        }
        if (canDispute) {
            SecondaryButton(
                text = stringResource(R.string.settlement_overview_dispute),
                onClick = onDispute,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
