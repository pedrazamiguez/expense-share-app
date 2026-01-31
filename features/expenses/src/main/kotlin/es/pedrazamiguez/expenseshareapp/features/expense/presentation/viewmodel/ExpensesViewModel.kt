package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.GetGroupExpensesFlowUseCase
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper.ExpenseUiMapper
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.ExpenseUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event.ExpensesUiEvent
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.ExpensesUiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class ExpensesViewModel(
    private val getGroupExpensesFlowUseCase: GetGroupExpensesFlowUseCase,
    private val expenseUiMapper: ExpenseUiMapper
) : ViewModel() {

    private val _scrollState = MutableStateFlow(Pair(0, 0))
    private val _selectedGroupId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ExpensesUiState> = _selectedGroupId
        .filterNotNull()
        .flatMapLatest { groupId ->
            getGroupExpensesFlowUseCase(groupId)
                .map { expenseUiMapper.mapList(it) }
                .transformLatest<ImmutableList<ExpenseUiModel>, UiStateUpdate> { uiList ->
                    if (uiList.isNotEmpty()) {
                        emit(UiStateUpdate.Success(uiList))
                    } else {
                        // Grace period to avoid empty state flicker
                        emit(UiStateUpdate.LoadingEmpty)
                        delay(EMPTY_STATE_GRACE_PERIOD_MS)
                        emit(UiStateUpdate.Success(uiList))
                    }
                }
                .onStart { emit(UiStateUpdate.Loading) }
                .catch { emit(UiStateUpdate.Error(it.localizedMessage ?: "Unknown error")) }
                .map { update ->
                    when (update) {
                        is UiStateUpdate.Loading,
                        is UiStateUpdate.LoadingEmpty -> ExpensesUiState(
                            isLoading = true,
                            groupId = groupId
                        )

                        is UiStateUpdate.Success -> ExpensesUiState(
                            expenses = update.data,
                            isLoading = false,
                            groupId = groupId
                        )

                        is UiStateUpdate.Error -> ExpensesUiState(
                            isLoading = false,
                            errorMessage = update.msg,
                            groupId = groupId
                        )
                    }
                }
        }
        .combineWithScroll(_scrollState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ExpensesUiState(isLoading = true)
        )

    fun onEvent(event: ExpensesUiEvent) {
        when (event) {
            ExpensesUiEvent.LoadExpenses -> {
                _selectedGroupId.value?.let { current ->
                    _selectedGroupId.value = null
                    _selectedGroupId.value = current
                }
            }

            is ExpensesUiEvent.ScrollPositionChanged -> {
                _scrollState.update { event.index to event.offset }
            }
        }
    }

    fun setSelectedGroup(groupId: String?) {
        if (groupId != _selectedGroupId.value) {
            _selectedGroupId.value = groupId
        }
    }

    private sealed interface UiStateUpdate {
        data object Loading : UiStateUpdate
        data object LoadingEmpty : UiStateUpdate
        data class Success(val data: ImmutableList<ExpenseUiModel>) : UiStateUpdate
        data class Error(val msg: String) : UiStateUpdate
    }

    private fun Flow<ExpensesUiState>.combineWithScroll(
        scrollFlow: StateFlow<Pair<Int, Int>>
    ): Flow<ExpensesUiState> = combine(this, scrollFlow) { state, scroll ->
        state.copy(scrollPosition = scroll.first, scrollOffset = scroll.second)
    }

    companion object {
        private const val EMPTY_STATE_GRACE_PERIOD_MS = 300L
    }
}
