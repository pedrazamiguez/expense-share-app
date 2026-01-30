package es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.layout.ErrorView
import es.pedrazamiguez.expenseshareapp.features.balance.R
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.component.BalanceList
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.view.BalanceView
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.event.BalancesUiEvent
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.BalancesUiState
import kotlinx.collections.immutable.persistentListOf

@Composable
fun BalancesScreen(
    uiState: BalancesUiState,
    onEvent: (BalancesUiEvent) -> Unit = {},
    onNavigateToGroup: (String) -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(stringResource(R.string.balances_placeholder))
    }

    when {
        uiState.isLoading -> CircularProgressIndicator()
        uiState.error != null -> ErrorView()
        else -> BalanceList(
            balances = uiState.balances,
            onGroupClick = { id ->
                onEvent(BalancesUiEvent.OnGroupSelected(id))
                onNavigateToGroup(id)
            })
    }

}

@Preview
@Composable
private fun BalanceScreenPreview() {
    BalancesScreen(
        uiState = BalancesUiState(
            balances = persistentListOf(
                BalanceView(
                    userId = "1",
                    balanceId = "b1",
                    amount = "+20.0",
                    currencyCode = "USD"
                ),
                BalanceView(
                    userId = "2",
                    balanceId = "b2",
                    amount = "-15.5",
                    currencyCode = "EUR"
                ),
                BalanceView(
                    userId = "3",
                    balanceId = "b3",
                    amount = "+5.0",
                    currencyCode = "GBP"
                )
            )
        )
    )
}
