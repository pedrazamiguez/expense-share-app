package es.pedrazamiguez.splittrip.features.group.presentation.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.form.DestructiveButton
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.form.GradientButton
import es.pedrazamiguez.splittrip.features.group.R

@Composable
internal fun SettlementCloseTripButton(
    allResolved: Boolean,
    isArchiving: Boolean,
    onCloseTrip: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!allResolved) return

    if (isArchiving) {
        GradientButton(
            text = stringResource(R.string.settlement_overview_closing_trip),
            onClick = {},
            enabled = false,
            modifier = modifier.fillMaxWidth()
        )
    } else {
        DestructiveButton(
            text = stringResource(R.string.settlement_overview_close_trip),
            onClick = onCloseTrip,
            modifier = modifier.fillMaxWidth()
        )
    }
}
