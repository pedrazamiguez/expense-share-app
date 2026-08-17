package es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.splittrip.core.common.constant.AppConstants
import es.pedrazamiguez.splittrip.domain.model.ExpenseFilterCriteria
import es.pedrazamiguez.splittrip.domain.service.ExpenseFilterService
import es.pedrazamiguez.splittrip.domain.usecase.expense.GetGroupExpensesFlowUseCase
import es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.action.ExpensesFilterUiAction
import es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.event.ExpensesFilterUiEvent
import es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.state.ExpensesFilterUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ExpensesFilterViewModel(
    private val getGroupExpensesFlowUseCase: GetGroupExpensesFlowUseCase,
    private val expenseFilterService: ExpenseFilterService
) : ViewModel() {

    private val _selectedGroupId = MutableStateFlow<String?>(null)
    private val _draftCriteria = MutableStateFlow(ExpenseFilterCriteria())
    private var isInitialized = false

    private val _actions = MutableSharedFlow<ExpensesFilterUiAction>()
    val actions: SharedFlow<ExpensesFilterUiAction> = _actions.asSharedFlow()

    val uiState: StateFlow<ExpensesFilterUiState> = _selectedGroupId
        .filterNotNull()
        .flatMapLatest { groupId ->
            combine(
                getGroupExpensesFlowUseCase(groupId),
                _draftCriteria
            ) { expenses, draft ->
                val totalExpensesCount = expenses.size
                val matchingExpenses = expenseFilterService.filter(expenses, draft)
                ExpensesFilterUiState(
                    draftCriteria = draft,
                    matchingExpensesCount = matchingExpenses.size,
                    totalExpensesCount = totalExpensesCount,
                    isLoading = false,
                    groupId = groupId
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(
                stopTimeoutMillis = AppConstants.FLOW_RETENTION_TIME,
                replayExpirationMillis = AppConstants.FLOW_REPLAY_EXPIRATION
            ),
            initialValue = ExpensesFilterUiState(isLoading = true)
        )

    fun onEvent(event: ExpensesFilterUiEvent) {
        when (event) {
            is ExpensesFilterUiEvent.Initialize -> {
                if (!isInitialized) {
                    _draftCriteria.value = event.initialCriteria
                    isInitialized = true
                }
            }

            is ExpensesFilterUiEvent.UpdateDraft -> {
                _draftCriteria.value = event.criteria
            }

            ExpensesFilterUiEvent.ResetDraft -> {
                _draftCriteria.update { it.clearNonSearchFilters() }
            }

            ExpensesFilterUiEvent.ApplyFilters -> {
                viewModelScope.launch {
                    _actions.emit(
                        ExpensesFilterUiAction.ApplyAndNavigateBack(_draftCriteria.value)
                    )
                }
            }
        }
    }

    fun setSelectedGroup(groupId: String?) {
        if (groupId != _selectedGroupId.value) {
            _selectedGroupId.value = groupId
        }
    }
}
