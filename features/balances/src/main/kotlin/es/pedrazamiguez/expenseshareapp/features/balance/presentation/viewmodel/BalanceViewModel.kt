package es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetBalancesUseCase
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.mapper.toView
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.BalanceUiAction
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.BalanceUiEvent
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.BalanceUiState
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class BalanceViewModel(
    private val getBalances: GetBalancesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BalanceUiState())
    val uiState: StateFlow<BalanceUiState> = _uiState.asStateFlow()

    private val _actions = MutableSharedFlow<BalanceUiAction>()
    val actions: SharedFlow<BalanceUiAction> = _actions.asSharedFlow()

    fun onEvent(event: BalanceUiEvent) {
        when (event) {
            BalanceUiEvent.Refresh -> loadBalances()
            is BalanceUiEvent.OnGroupSelected -> {
                viewModelScope.launch {
                    _actions.emit(BalanceUiAction.NavigateToGroup(event.groupId))
                }
            }
        }
    }

    private fun loadBalances() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { getBalances() }
                .onSuccess { balances ->
                    Timber.d("Balances loaded: $balances")
                    _uiState.value = BalanceUiState(
                        balances = balances
                            .getOrElse { emptyList() }
                            .map { it.toView() }
                            .toImmutableList()
                    )
                }
                .onFailure { e ->
                    _uiState.value = BalanceUiState(error = e.message)
                    _actions.emit(BalanceUiAction.ShowError(e.message ?: "Unknown error"))
                }
        }
    }

}
