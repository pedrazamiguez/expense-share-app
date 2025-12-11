package es.pedrazamiguez.expenseshareapp.ui.balance.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import es.pedrazamiguez.expenseshareapp.ui.balance.presentation.model.BalanceUiState
import es.pedrazamiguez.expenseshareapp.ui.balance.presentation.view.BalanceView

@Preview
@Composable
fun BalanceScreenPreview() {
    BalancesScreen(
        uiState = BalanceUiState(
            balances = listOf(
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
