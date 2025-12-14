package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.viewmodel.SharedViewModel
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.GetGroupExpensesFlowUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class ListGroupExpensesViewModel(
    private val getGroupExpensesFlowUseCase: GetGroupExpensesFlowUseCase,
    private val sharedViewModel: SharedViewModel
) : ViewModel() {

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var currentJob: Job? = null
    private var currentGroupId: String? = null

    init {
        // Observe group changes and fetch expenses automatically
        // StateFlow already emits only distinct values, no need for distinctUntilChanged()
        viewModelScope.launch {
            sharedViewModel.selectedGroupId
                .filterNotNull()
                .collect { groupId ->
                    fetchExpensesFlow(groupId)
                }
        }
    }

    private fun fetchExpensesFlow(groupId: String) {
        // Skip if we're already observing this group
        if (currentGroupId == groupId && currentJob?.isActive == true) {
            return
        }

        currentJob?.cancel()

        // If we're switching to a different group, clear the old data immediately
        // This prevents showing stale expenses from a different group
        val isGroupChange = currentGroupId != null && currentGroupId != groupId
        if (isGroupChange) {
            _expenses.value = emptyList()
        }

        currentGroupId = groupId

        currentJob = viewModelScope.launch {
            // Show loading if we don't have data (initial load or group change)
            if (_expenses.value.isEmpty()) {
                _loading.value = true
            }
            _error.value = null

            getGroupExpensesFlowUseCase
                .invoke(groupId)
                .catch { e ->
                    _error.value = e.localizedMessage ?: "Unknown error"
                    _loading.value = false
                }
                .collect { expenses ->
                    _expenses.value = expenses
                    _loading.value = false
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentJob?.cancel()
    }

}
