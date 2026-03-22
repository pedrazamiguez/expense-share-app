package es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.core.common.constant.AppConstants
import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.GetGroupByIdUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.DeleteSubunitUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.GetGroupSubunitsFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.user.GetMemberProfilesUseCase
import es.pedrazamiguez.expenseshareapp.features.group.R
import es.pedrazamiguez.expenseshareapp.features.group.presentation.mapper.SubunitUiMapper
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.action.SubunitManagementUiAction
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.event.SubunitManagementUiEvent
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.state.SubunitManagementUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel for the Sub-Unit Management screen (list view).
 *
 * Observes sub-units from the local database (Room) via hot flow.
 * Handles delete operations and navigates to create/edit screen.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SubunitManagementViewModel(
    private val getGroupSubunitsFlowUseCase: GetGroupSubunitsFlowUseCase,
    private val deleteSubunitUseCase: DeleteSubunitUseCase,
    private val getGroupByIdUseCase: GetGroupByIdUseCase,
    private val getMemberProfilesUseCase: GetMemberProfilesUseCase,
    private val subunitUiMapper: SubunitUiMapper
) : ViewModel() {

    private val _groupId = MutableStateFlow("")

    private val _actions = MutableSharedFlow<SubunitManagementUiAction>()
    val actions: SharedFlow<SubunitManagementUiAction> = _actions.asSharedFlow()

    val uiState: StateFlow<SubunitManagementUiState> = _groupId
        .filter { it.isNotBlank() }
        .flatMapLatest { groupId ->
            val group = getGroupByIdUseCase(groupId)
            val memberIds = group?.members ?: emptyList()
            val memberProfiles = getMemberProfilesUseCase(memberIds)
            val groupName = group?.name ?: ""

            getGroupSubunitsFlowUseCase(groupId)
                .map { subunits ->
                    SubunitManagementUiState(
                        isLoading = false,
                        groupId = groupId,
                        groupName = groupName,
                        subunits = subunitUiMapper.toSubunitUiModelList(subunits, memberProfiles)
                    )
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(
                stopTimeoutMillis = AppConstants.FLOW_RETENTION_TIME,
                replayExpirationMillis = AppConstants.FLOW_REPLAY_EXPIRATION
            ),
            initialValue = SubunitManagementUiState()
        )

    fun setGroupId(groupId: String) {
        if (groupId != _groupId.value) {
            _groupId.value = groupId
        }
    }

    fun onEvent(event: SubunitManagementUiEvent) {
        when (event) {
            SubunitManagementUiEvent.CreateSubunit -> navigateToCreate()
            is SubunitManagementUiEvent.EditSubunit -> navigateToEdit(event.subunitId)
            is SubunitManagementUiEvent.ConfirmDeleteSubunit -> handleDeleteSubunit(event.subunitId)
        }
    }

    private fun navigateToCreate() {
        viewModelScope.launch {
            _actions.emit(SubunitManagementUiAction.NavigateToCreateSubunit(_groupId.value))
        }
    }

    private fun navigateToEdit(subunitId: String) {
        viewModelScope.launch {
            _actions.emit(SubunitManagementUiAction.NavigateToEditSubunit(_groupId.value, subunitId))
        }
    }

    private fun handleDeleteSubunit(subunitId: String) {
        val groupId = _groupId.value
        viewModelScope.launch {
            try {
                deleteSubunitUseCase(groupId, subunitId)
                _actions.emit(
                    SubunitManagementUiAction.ShowSuccess(
                        UiText.StringResource(R.string.subunit_deleted_success)
                    )
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete subunit: $subunitId")
                _actions.emit(
                    SubunitManagementUiAction.ShowError(
                        UiText.StringResource(R.string.subunit_error_delete_failed)
                    )
                )
            }
        }
    }
}
