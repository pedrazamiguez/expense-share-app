package es.pedrazamiguez.splittrip.features.balance.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.chip.PassportChip

@Composable
internal fun SettlementPocketTypeChip(
    label: String,
    modifier: Modifier = Modifier
) {
    PassportChip(
        label = label,
        selected = false,
        onClick = {},
        modifier = modifier
    )
}
