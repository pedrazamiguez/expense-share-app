package es.pedrazamiguez.splittrip.features.balance.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.features.balance.presentation.model.BalanceMetricType
import es.pedrazamiguez.splittrip.features.balance.presentation.model.CashBalanceUiModel
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun CashBalancesSection(
    cashBalances: ImmutableList<CashBalanceUiModel>,
    formattedTotalCashEquivalent: String,
    modifier: Modifier = Modifier,
    onShowMetricInfo: (BalanceMetricType) -> Unit = {}
) {
    Column(modifier = modifier) {
        CashBalancesHeaderRow(
            formattedTotalCashEquivalent = formattedTotalCashEquivalent,
            onShowMetricInfo = onShowMetricInfo
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.Small))
        cashBalances.forEach { cashBalance ->
            CashBalanceItemRow(cashBalance = cashBalance)
        }
    }
}
