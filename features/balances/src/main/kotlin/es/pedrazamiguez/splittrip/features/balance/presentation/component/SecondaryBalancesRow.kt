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
import es.pedrazamiguez.splittrip.features.balance.presentation.model.GroupPocketBalanceUiModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SecondaryBalancesRow(balance: GroupPocketBalanceUiModel) {
    val showAvailable = balance.formattedAvailableBalance != null
    val showScheduled = balance.formattedScheduledHoldAmount != null
    val showRefundable = balance.formattedRefundableHoldAmount != null

    val items = buildList {
        if (showAvailable) add(stringResource(R.string.balances_available) to balance.formattedAvailableBalance)
        if (showScheduled) add(stringResource(R.string.balances_scheduled) to balance.formattedScheduledHoldAmount)
        if (showRefundable) add(stringResource(R.string.balances_on_hold) to balance.formattedRefundableHoldAmount)
    }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Medium)
    ) {
        items.forEachIndexed { index, (label, amount) ->
            val alignment = when {
                items.size == 1 -> Alignment.CenterHorizontally
                index == 0 -> Alignment.Start
                index == items.lastIndex -> Alignment.End
                else -> Alignment.CenterHorizontally
            }
            SecondaryBalanceColumn(
                label = label,
                amount = amount,
                horizontalAlignment = alignment
            )
        }
    }
}
