package es.pedrazamiguez.expenseshareapp.ui.balance.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import es.pedrazamiguez.expenseshareapp.ui.balance.presentation.uimodel.BalanceUi

@Preview
@Composable
fun BalanceListPreview() {
    BalanceList(
        balances = listOf(
            BalanceUi(
                userId = "1", balanceId = "b1", amount = "+20.0", currencyCode = "USD"
            ), BalanceUi(
                userId = "2", balanceId = "b2", amount = "-15.5", currencyCode = "EUR"
            ), BalanceUi(
                userId = "3", balanceId = "b3", amount = "+5.0", currencyCode = "GBP"
            )
        ), onGroupClick = {})
}
