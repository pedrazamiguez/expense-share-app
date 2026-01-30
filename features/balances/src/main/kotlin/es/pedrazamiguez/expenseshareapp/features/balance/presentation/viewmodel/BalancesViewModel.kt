package es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetBalancesUseCase
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.mapper.toView
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.action.BalancesUiAction
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.event.BalancesUiEvent
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.BalancesUiState
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

class BalancesViewModel(
    private val getBalances: GetBalancesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BalancesUiState())
    val uiState: StateFlow<BalancesUiState> = _uiState.asStateFlow()

    private val _actions = MutableSharedFlow<BalancesUiAction>()
    val actions: SharedFlow<BalancesUiAction> = _actions.asSharedFlow()

    fun onEvent(event: BalancesUiEvent) {
        when (event) {
            BalancesUiEvent.LoadBalances -> loadBalances()
            is BalancesUiEvent.OnGroupSelected -> {
                viewModelScope.launch {
                    _actions.emit(BalancesUiAction.NavigateToGroup(event.groupId))
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
                    _uiState.value = BalancesUiState(
                        balances = balances
                            .getOrElse { emptyList() }
                            .map { it.toView() }
                            .toImmutableList()
                    )
                }
                .onFailure { e ->
                    _uiState.value = BalancesUiState(error = e.message)
                    _actions.emit(BalancesUiAction.ShowError(e.message ?: "Unknown error"))
                }
        }
    }

}
