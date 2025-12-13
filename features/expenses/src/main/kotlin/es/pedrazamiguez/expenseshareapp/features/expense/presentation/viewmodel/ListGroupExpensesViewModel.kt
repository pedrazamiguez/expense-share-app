package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.GetGroupExpensesFlowUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ListGroupExpensesViewModel(
    private val getGroupExpensesFlowUseCase: GetGroupExpensesFlowUseCase
) : ViewModel() {

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var currentJob: Job? = null

    fun fetchExpensesFlow(groupId: String) {
        currentJob?.cancel()

        currentJob = viewModelScope.launch {
            _loading.value = true
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
