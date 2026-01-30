package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.viewmodel.SharedViewModel
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.GetGroupExpensesFlowUseCase
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper.ExpenseUiMapper
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event.ExpensesUiEvent
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.ExpensesUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExpensesViewModel(
    private val getGroupExpensesFlowUseCase: GetGroupExpensesFlowUseCase,
    private val expenseUiMapper: ExpenseUiMapper,
    private val sharedViewModel: SharedViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpensesUiState())
    val uiState: StateFlow<ExpensesUiState> = _uiState.asStateFlow()

    private var currentJob: Job? = null
    private var currentGroupId: String? = null

    fun onEvent(event: ExpensesUiEvent) {
        when (event) {
            ExpensesUiEvent.LoadExpenses -> observeSelectedGroup()
            is ExpensesUiEvent.ScrollPositionChanged -> saveScrollPosition(
                event.index,
                event.offset
            )
        }
    }

    private fun observeSelectedGroup() {
        viewModelScope.launch {
            sharedViewModel.selectedGroupId.filterNotNull().collect { groupId ->
                loadExpenses(groupId)
            }
        }
    }

    private fun saveScrollPosition(firstVisibleItemIndex: Int, firstVisibleItemScrollOffset: Int) {
        _uiState.update {
            it.copy(
                scrollPosition = firstVisibleItemIndex,
                scrollOffset = firstVisibleItemScrollOffset
            )
        }
    }

    private fun loadExpenses(groupId: String) {
        if (currentGroupId == groupId && currentJob?.isActive == true) {
            return
        }

        currentJob?.cancel()
        currentGroupId = groupId

        currentJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            getGroupExpensesFlowUseCase.invoke(groupId)
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.localizedMessage ?: "Unknown error"
                        )
                    }
                }
                .collect { expenses ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            expenses = expenseUiMapper.mapList(expenses)
                        )
                    }
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentJob?.cancel()
    }

}
