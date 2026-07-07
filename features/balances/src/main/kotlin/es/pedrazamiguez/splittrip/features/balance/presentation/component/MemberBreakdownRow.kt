package es.pedrazamiguez.splittrip.features.balance.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.features.balance.R
import es.pedrazamiguez.splittrip.features.balance.presentation.model.MemberBalanceUiModel

@Composable
internal fun MemberBreakdownRow(
    memberBalance: MemberBalanceUiModel,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Small)
    ) {
        BreakdownLabel(
            label = stringResource(R.string.balances_member_contributed_label),
            value = memberBalance.formattedContributed
        )
        BreakdownLabel(
            label = stringResource(R.string.balances_member_cash_in_hand_label),
            value = memberBalance.formattedCashInHand
        )
        BreakdownLabel(
            label = stringResource(R.string.balances_member_spent_label),
            value = memberBalance.formattedTotalSpent
        )
    }
}
