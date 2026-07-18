package es.pedrazamiguez.splittrip.features.balance.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.splittrip.features.balance.R
import es.pedrazamiguez.splittrip.features.balance.presentation.model.GroupPocketBalanceUiModel

@Composable
internal fun SecondaryBalancesRow(balance: GroupPocketBalanceUiModel) {
    val showAvailable = balance.formattedAvailableBalance != null
    val showScheduled = balance.formattedScheduledHoldAmount != null
    val showRefundable = balance.formattedRefundableHoldAmount != null

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        val useWeight = listOf(showAvailable, showScheduled, showRefundable).count { it } > 1
        val modifier = if (useWeight) Modifier.weight(1f) else Modifier

        if (showAvailable) {
            SecondaryBalanceColumn(
                label = stringResource(R.string.balances_available),
                amount = balance.formattedAvailableBalance!!,
                modifier = modifier
            )
        }

        if (showScheduled) {
            SecondaryBalanceColumn(
                label = stringResource(R.string.balances_scheduled),
                amount = balance.formattedScheduledHoldAmount!!,
                modifier = modifier
            )
        }

        if (showRefundable) {
            SecondaryBalanceColumn(
                label = stringResource(R.string.balances_on_hold),
                amount = balance.formattedRefundableHoldAmount!!,
                modifier = modifier
            )
        }
    }
}
