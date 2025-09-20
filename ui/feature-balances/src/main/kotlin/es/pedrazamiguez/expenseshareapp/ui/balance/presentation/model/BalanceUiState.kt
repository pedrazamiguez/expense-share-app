package es.pedrazamiguez.expenseshareapp.ui.balance.presentation.model

import es.pedrazamiguez.expenseshareapp.ui.balance.presentation.uimodel.BalanceUi

data class BalanceUiState(
    val isLoading: Boolean = false,
    val balances: List<BalanceUi> = emptyList(),
    val error: String? = null
)
