package es.pedrazamiguez.splittrip.features.settlement.presentation.component

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.text.LabelText
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.SettlementRowUiModel
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun SettlementSection(
    @StringRes labelRes: Int,
    settlements: ImmutableList<SettlementRowUiModel>,
    onConfirm: (String) -> Unit = {},
    onDispute: (String) -> Unit = {}
) {
    if (settlements.isNotEmpty()) {
        LabelText(text = stringResource(labelRes))
        settlements.forEach { settlement ->
            GroupSettlementItem(
                settlement = settlement,
                onConfirm = { onConfirm(settlement.settlementId) },
                onDispute = { onDispute(settlement.settlementId) }
            )
        }
    }
}
