package es.pedrazamiguez.expenseshareapp.ui.balance.presentation.mapper

import es.pedrazamiguez.expenseshareapp.domain.model.Balance
import es.pedrazamiguez.expenseshareapp.ui.balance.presentation.uimodel.BalanceUi

fun Balance.toUi(): BalanceUi {
    val formatted = if (amount >= 0) "+$amount" else "$amount"
    if (amount >= 0) android.R.drawable.arrow_up_float else android.R.drawable.arrow_down_float
    return BalanceUi(
        userId = userId,
        balanceId = "",
        amount = formatted,
        currencyCode = currency.code,
    )
}
