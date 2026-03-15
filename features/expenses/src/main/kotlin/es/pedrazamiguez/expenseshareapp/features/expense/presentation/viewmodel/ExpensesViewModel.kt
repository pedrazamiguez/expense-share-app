package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.core.common.constant.AppConstants
import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.DeleteExpenseUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.GetGroupExpensesFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.GetGroupByIdUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.user.GetMemberProfilesUseCase
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper.ExpenseUiMapper
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.ExpenseDateGroupUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.action.ExpensesUiAction
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.event.ExpensesUiEvent
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.ExpensesUiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class ExpensesViewModel(
    private val getGroupExpensesFlowUseCase: GetGroupExpensesFlowUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val expenseUiMapper: ExpenseUiMapper,
    private val getGroupByIdUseCase: GetGroupByIdUseCase,
    private val getMemberProfilesUseCase: GetMemberProfilesUseCase
) : ViewModel() {

    private val _scrollState = MutableStateFlow(Pair(0, 0))
    private val _selectedGroupId = MutableStateFlow<String?>(null)
    private val _refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    // Actions for one-shot events like success/error messages
    private val _actions = MutableSharedFlow<ExpensesUiAction>()
    val actions: SharedFlow<ExpensesUiAction> = _actions.asSharedFlow()

    val uiState: StateFlow<ExpensesUiState> = _selectedGroupId
        .filterNotNull()
        .flatMapLatest { groupId ->
            val group = getGroupByIdUseCase(groupId)
            val groupMemberIds = group?.members ?: emptyList()

            // Merge: emit once immediately (Unit), plus on every explicit refresh
            merge(
                flowOf(Unit),
                _refreshTrigger
            ).flatMapLatest {
                getGroupExpensesFlowUseCase(groupId)
                    .map { expenses ->
                        // Collect ALL unique user IDs: group members + expense creators
                        val allUserIds = buildSet {
                            addAll(groupMemberIds)
                            expenses.forEach { add(it.createdBy) }
                        }.toList()
                        val memberProfiles = getMemberProfilesUseCase(allUserIds)
                        expenseUiMapper.mapGroupedByDate(expenses, memberProfiles)
                    }
                    .transformLatest<ImmutableList<ExpenseDateGroupUiModel>, UiStateUpdate> { groups ->
                        if (groups.any { it.expenses.isNotEmpty() }) {
                            emit(UiStateUpdate.Success(groups))
                        } else {
                            // Grace period to avoid empty state flicker
                            emit(UiStateUpdate.LoadingEmpty)
                            delay(EMPTY_STATE_GRACE_PERIOD_MS)
                            emit(UiStateUpdate.Success(groups))
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
                                expenseGroups = update.data,
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
        }
        .combineWithScroll(_scrollState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(AppConstants.FLOW_RETENTION_TIME),
            initialValue = ExpensesUiState(isLoading = true)
        )

    fun onEvent(event: ExpensesUiEvent) {
        when (event) {
            ExpensesUiEvent.LoadExpenses -> {
                viewModelScope.launch { _refreshTrigger.emit(Unit) }
            }

            is ExpensesUiEvent.ScrollPositionChanged -> {
                _scrollState.update { event.index to event.offset }
            }

            is ExpensesUiEvent.DeleteExpense -> handleDeleteExpense(event.expenseId)
        }
    }

    fun setSelectedGroup(groupId: String?) {
        if (groupId != _selectedGroupId.value) {
            _selectedGroupId.value = groupId
        }
    }

    private fun handleDeleteExpense(expenseId: String) {
        val groupId = _selectedGroupId.value ?: return
        viewModelScope.launch {
            try {
                deleteExpenseUseCase(groupId, expenseId)
                _actions.emit(
                    ExpensesUiAction.ShowDeleteSuccess(
                        UiText.StringResource(R.string.expense_deleted_successfully)
                    )
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete expense: $expenseId")
                _actions.emit(
                    ExpensesUiAction.ShowDeleteError(
                        UiText.StringResource(R.string.error_deleting_expense)
                    )
                )
            }
        }
    }

    private sealed interface UiStateUpdate {
        data object Loading : UiStateUpdate
        data object LoadingEmpty : UiStateUpdate
        data class Success(val data: ImmutableList<ExpenseDateGroupUiModel>) : UiStateUpdate
        data class Error(val msg: String) : UiStateUpdate
    }

    private fun Flow<ExpensesUiState>.combineWithScroll(scrollFlow: StateFlow<Pair<Int, Int>>): Flow<ExpensesUiState> =
        combine(this, scrollFlow) { state, scroll ->
            state.copy(scrollPosition = scroll.first, scrollOffset = scroll.second)
        }

    companion object {
        // Grace period before showing the empty state.
        // On cold start, Room emits an empty list instantly while the cloud sync
        // runs in the background. transformLatest will cancel this delay the moment
        // Room emits non-empty data (after the sync upserts), so groups with data
        // are never delayed. Only genuinely empty groups wait the full duration.
        private const val EMPTY_STATE_GRACE_PERIOD_MS = 400L
    }
}
