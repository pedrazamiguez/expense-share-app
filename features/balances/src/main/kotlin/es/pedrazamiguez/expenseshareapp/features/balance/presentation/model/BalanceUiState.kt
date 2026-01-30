package es.pedrazamiguez.expenseshareapp.features.balance.presentation.model

import es.pedrazamiguez.expenseshareapp.features.balance.presentation.view.BalanceView
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class BalanceUiState(
    val isLoading: Boolean = false,
    val balances: ImmutableList<BalanceView> = persistentListOf(),
    val error: String? = null
)
