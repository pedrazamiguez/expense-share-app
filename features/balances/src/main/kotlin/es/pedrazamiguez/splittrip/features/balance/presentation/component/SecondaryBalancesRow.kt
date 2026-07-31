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

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Medium, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Medium)
    ) {
        if (showAvailable) {
            SecondaryBalanceColumn(
                label = stringResource(R.string.balances_available),
                amount = balance.formattedAvailableBalance!!
            )
        }

        if (showScheduled) {
            SecondaryBalanceColumn(
                label = stringResource(R.string.balances_scheduled),
                amount = balance.formattedScheduledHoldAmount!!
            )
        }

        if (showRefundable) {
            SecondaryBalanceColumn(
                label = stringResource(R.string.balances_on_hold),
                amount = balance.formattedRefundableHoldAmount!!
            )
        }
    }
}
