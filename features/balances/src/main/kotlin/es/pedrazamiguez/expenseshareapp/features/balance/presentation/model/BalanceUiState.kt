package es.pedrazamiguez.expenseshareapp.features.balance.presentation.model

import es.pedrazamiguez.expenseshareapp.features.balance.presentation.view.BalanceView

data class BalanceUiState(
    val isLoading: Boolean = false,
    val balances: List<BalanceView> = emptyList(),
    val error: String? = null
)
