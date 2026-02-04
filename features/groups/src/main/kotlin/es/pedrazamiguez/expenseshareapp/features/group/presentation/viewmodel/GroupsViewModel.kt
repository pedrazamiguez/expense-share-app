package es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.DeleteGroupUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.GetUserGroupsFlowUseCase
import es.pedrazamiguez.expenseshareapp.features.group.R
import es.pedrazamiguez.expenseshareapp.features.group.presentation.mapper.GroupUiMapper
import es.pedrazamiguez.expenseshareapp.features.group.presentation.model.GroupUiModel
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.action.GroupsUiAction
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.event.GroupsUiEvent
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.state.GroupsUiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel for the Groups screen.
 *
 * Uses Offline-First pattern:
 * - Groups are loaded automatically from local database (instant, no shimmer)
 * - Background sync with cloud happens automatically
 * - UI state is derived from the groups Flow using stateIn
 */
class GroupsViewModel(
    getUserGroupsFlowUseCase: GetUserGroupsFlowUseCase,
    private val deleteGroupUseCase: DeleteGroupUseCase,
    private val groupUiMapper: GroupUiMapper
) : ViewModel() {

    // Scroll state is managed separately as it's UI-only state
    private val _scrollState = MutableStateFlow(ScrollState())

    // Actions for one-shot events like success/error messages
    private val _actions = MutableSharedFlow<GroupsUiAction>()
    val actions: SharedFlow<GroupsUiAction> = _actions.asSharedFlow()

    /**
     * UI state derived from the groups Flow.
     * - Room emits instantly → UI shows data immediately
     * - No more isLoading shimmer on every tab switch
     * - Only shows loading on first app install (when local DB is empty)
     */
    val uiState: StateFlow<GroupsUiState> = combine(
        getUserGroupsFlowUseCase.invoke()
            .map { groups ->
                GroupsDataState(
                    isLoading = false,
                    groups = groupUiMapper.toGroupUiModelList(groups),
                    errorMessage = null
                )
            }
            .catch { e ->
                emit(
                    GroupsDataState(
                        isLoading = false,
                        groups = persistentListOf(),
                        errorMessage = e.localizedMessage ?: "Unknown error"
                    )
                )
            },
        _scrollState
    ) { dataState, scrollState ->
        GroupsUiState(
            isLoading = dataState.isLoading,
            groups = dataState.groups,
            errorMessage = dataState.errorMessage,
            scrollPosition = scrollState.position,
            scrollOffset = scrollState.offset
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GroupsUiState(isLoading = true)
    )

    fun onEvent(event: GroupsUiEvent) {
        when (event) {
            // LoadGroups is now a no-op - data loads automatically via stateIn
            GroupsUiEvent.LoadGroups -> { /* No action needed */
            }

            is GroupsUiEvent.ScrollPositionChanged -> saveScrollPosition(
                event.index,
                event.offset
            )

            is GroupsUiEvent.DeleteGroup -> handleDeleteGroup(event.groupId)
        }
    }

    private fun handleDeleteGroup(groupId: String) {
        viewModelScope.launch {
            try {
                deleteGroupUseCase(groupId)
                _actions.emit(
                    GroupsUiAction.ShowDeleteSuccess(
                        UiText.StringResource(R.string.group_deleted_successfully)
                    )
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete group: $groupId")
                _actions.emit(
                    GroupsUiAction.ShowDeleteError(
                        UiText.StringResource(R.string.error_deleting_group)
                    )
                )
            }
        }
    }

    private fun saveScrollPosition(firstVisibleItemIndex: Int, firstVisibleItemScrollOffset: Int) {
        _scrollState.update {
            it.copy(
                position = firstVisibleItemIndex,
                offset = firstVisibleItemScrollOffset
            )
        }
    }

    // Internal data classes for state combination
    private data class GroupsDataState(
        val isLoading: Boolean,
        val groups: ImmutableList<GroupUiModel>,
        val errorMessage: String?
    )

    private data class ScrollState(
        val position: Int = 0,
        val offset: Int = 0
    )
}
