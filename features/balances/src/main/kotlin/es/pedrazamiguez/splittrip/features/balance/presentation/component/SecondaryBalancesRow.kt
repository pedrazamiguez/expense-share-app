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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        if (balance.formattedAvailableBalance != null) {
            SecondaryBalanceColumn(
                label = stringResource(R.string.balances_available),
                amount = balance.formattedAvailableBalance,
                modifier = if (balance.formattedRefundableHoldAmount != null) Modifier.weight(1f) else Modifier
            )
        }

        if (balance.formattedRefundableHoldAmount != null) {
            SecondaryBalanceColumn(
                label = stringResource(R.string.balances_on_hold),
                amount = balance.formattedRefundableHoldAmount,
                modifier = if (balance.formattedAvailableBalance != null) Modifier.weight(1f) else Modifier
            )
        }
    }
}
