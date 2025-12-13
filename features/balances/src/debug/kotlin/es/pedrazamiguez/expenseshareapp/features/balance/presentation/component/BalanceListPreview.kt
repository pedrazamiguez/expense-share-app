package es.pedrazamiguez.expenseshareapp.features.balance.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.view.BalanceView

@Preview
@Composable
fun BalanceListPreview() {
    BalanceList(
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
        ),
        onGroupClick = {})
}
