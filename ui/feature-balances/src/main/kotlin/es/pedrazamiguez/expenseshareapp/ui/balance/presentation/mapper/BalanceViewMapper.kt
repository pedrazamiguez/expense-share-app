package es.pedrazamiguez.expenseshareapp.ui.balance.presentation.mapper

import es.pedrazamiguez.expenseshareapp.domain.model.Balance
import es.pedrazamiguez.expenseshareapp.ui.balance.presentation.view.BalanceView

fun Balance.toView(): BalanceView {
    val formatted = if (amount >= 0) "+$amount" else "$amount"
    return BalanceView(
        userId = userId,
        balanceId = "",
        amount = formatted,
        currencyCode = currency.code,
    )
}
