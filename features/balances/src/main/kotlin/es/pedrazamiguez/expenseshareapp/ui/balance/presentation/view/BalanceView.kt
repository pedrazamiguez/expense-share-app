package es.pedrazamiguez.expenseshareapp.ui.balance.presentation.view

data class BalanceView(
    val userId: String,
    val balanceId: String,
    val amount: String,
    val currencyCode: String
)