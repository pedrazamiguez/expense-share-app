package es.pedrazamiguez.splittrip.features.balance.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.features.balance.R
import es.pedrazamiguez.splittrip.features.balance.presentation.model.BalanceMetricType
import es.pedrazamiguez.splittrip.features.balance.presentation.model.GroupPocketBalanceUiModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SecondaryBalancesRow(
    balance: GroupPocketBalanceUiModel,
    onShowMetricInfo: (BalanceMetricType) -> Unit = {}
) {
    val items = buildList {
        if (balance.formattedAvailableBalance != null) {
            add(
                Triple(
                    stringResource(R.string.balances_available),
                    balance.formattedAvailableBalance,
                    BalanceMetricType.AVAILABLE
                )
            )
        }
        if (balance.formattedScheduledHoldAmount != null) {
            add(
                Triple(
                    stringResource(R.string.balances_scheduled),
                    balance.formattedScheduledHoldAmount,
                    BalanceMetricType.SCHEDULED
                )
            )
        }
        if (balance.formattedRefundableHoldAmount != null) {
            add(
                Triple(
                    stringResource(R.string.balances_refundable),
                    balance.formattedRefundableHoldAmount,
                    BalanceMetricType.REFUNDABLE
                )
            )
        }
    }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Medium)
    ) {
        items.forEachIndexed { index, (label, amount, metricType) ->
            val alignment = when {
                items.size == 1 -> Alignment.CenterHorizontally
                index == 0 -> Alignment.Start
                index == items.lastIndex -> Alignment.End
                else -> Alignment.CenterHorizontally
            }
            SecondaryBalanceColumn(
                label = label,
                amount = amount,
                horizontalAlignment = alignment,
                onInfoClick = { onShowMetricInfo(metricType) }
            )
        }
    }
}
