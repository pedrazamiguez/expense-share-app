package es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.core.common.constant.AppConstants
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
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
import kotlinx.coroutines.flow.transformLatest
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
@OptIn(ExperimentalCoroutinesApi::class)
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
     * - When the list is empty, a grace period keeps isLoading=true for
     *   [EMPTY_STATE_GRACE_PERIOD_MS] ms so the UI shows shimmer instead of
     *   the empty state. If data arrives during the grace period,
     *   transformLatest cancels the delay and emits data immediately.
     *   This covers cold start (Room empty before cloud sync) and also
     *   any transient empty emissions (e.g. after the last group is deleted
     *   and cloud reconciliation re-populates the list).
     */
    val uiState: StateFlow<GroupsUiState> = combine(
        getUserGroupsFlowUseCase.invoke()
            .map { groups -> groupUiMapper.toGroupUiModelList(groups) }
            .transformLatest { groups ->
                if (groups.isNotEmpty()) {
                    emit(
                        GroupsDataState(
                            isLoading = false,
                            groups = groups,
                            errorMessage = null
                        )
                    )
                } else {
                    // Grace period: on cold start Room emits an empty list instantly
                    // while the cloud sync runs in the background. Stay in loading state
                    // so the UI shows shimmer instead of the empty state.
                    // transformLatest cancels this coroutine if Room re-emits with data
                    // before the delay completes, so groups with data are never delayed.
                    emit(
                        GroupsDataState(
                            isLoading = true,
                            groups = groups,
                            errorMessage = null
                        )
                    )
                    delay(EMPTY_STATE_GRACE_PERIOD_MS)
                    emit(
                        GroupsDataState(
                            isLoading = false,
                            groups = groups,
                            errorMessage = null
                        )
                    )
                }
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
        started = SharingStarted.WhileSubscribed(
            stopTimeoutMillis = AppConstants.FLOW_RETENTION_TIME,
            replayExpirationMillis = AppConstants.FLOW_REPLAY_EXPIRATION
        ),
        initialValue = GroupsUiState(isLoading = true)
    )

    fun onEvent(event: GroupsUiEvent) {
        when (event) {
            // LoadGroups is now a no-op - data loads automatically via stateIn
            GroupsUiEvent.LoadGroups -> {
                /* No action needed */
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

    private data class ScrollState(val position: Int = 0, val offset: Int = 0)

    companion object {
        // Grace period before showing the empty state.
        // On cold start, Room emits an empty list instantly while the cloud sync
        // runs in the background. transformLatest will cancel this delay the moment
        // Room emits non-empty data (after the sync upserts), so groups with data
        // are never delayed. Only genuinely empty collections wait the full duration.
        private const val EMPTY_STATE_GRACE_PERIOD_MS = 400L
    }
}
