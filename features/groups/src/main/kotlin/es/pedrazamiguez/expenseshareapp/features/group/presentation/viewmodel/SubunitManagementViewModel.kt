package es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.core.common.constant.AppConstants
import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import es.pedrazamiguez.expenseshareapp.domain.model.User
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.GetGroupByIdUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.CreateSubunitUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.DeleteSubunitUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.GetGroupSubunitsFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.UpdateSubunitUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.user.GetMemberProfilesUseCase
import es.pedrazamiguez.expenseshareapp.features.group.R
import es.pedrazamiguez.expenseshareapp.features.group.presentation.mapper.SubunitUiMapper
import es.pedrazamiguez.expenseshareapp.features.group.presentation.model.SubunitFormState
import es.pedrazamiguez.expenseshareapp.features.group.presentation.model.SubunitUiModel
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.action.SubunitManagementUiAction
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.event.SubunitManagementUiEvent
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.state.SubunitManagementUiState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel for the Sub-Unit Management screen.
 *
 * Observes sub-units from the local database (Room) via hot flow.
 * Handles create, update, delete operations and form dialog state.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SubunitManagementViewModel(
    private val getGroupSubunitsFlowUseCase: GetGroupSubunitsFlowUseCase,
    private val createSubunitUseCase: CreateSubunitUseCase,
    private val updateSubunitUseCase: UpdateSubunitUseCase,
    private val deleteSubunitUseCase: DeleteSubunitUseCase,
    private val getGroupByIdUseCase: GetGroupByIdUseCase,
    private val getMemberProfilesUseCase: GetMemberProfilesUseCase,
    private val subunitUiMapper: SubunitUiMapper
) : ViewModel() {

    private val _groupId = MutableStateFlow("")
    private val _dialogState = MutableStateFlow(DialogState())

    private val _actions = MutableSharedFlow<SubunitManagementUiAction>()
    val actions: SharedFlow<SubunitManagementUiAction> = _actions.asSharedFlow()

    // Cache for member profiles and group data to avoid re-fetching on every subunit change
    private var cachedMemberProfiles: Map<String, User> = emptyMap()
    private var cachedMemberIds: List<String> = emptyList()
    private var cachedGroupName: String = ""
    private var cachedSubunits: List<Subunit> = emptyList()

    val uiState: StateFlow<SubunitManagementUiState> = _groupId
        .filter { it.isNotBlank() }
        .flatMapLatest { groupId ->
            val group = getGroupByIdUseCase(groupId)
            val memberIds = group?.members ?: emptyList()
            val memberProfiles = getMemberProfilesUseCase(memberIds)

            cachedMemberIds = memberIds
            cachedMemberProfiles = memberProfiles
            cachedGroupName = group?.name ?: ""

            getGroupSubunitsFlowUseCase(groupId)
                .map { subunits ->
                    cachedSubunits = subunits
                    DataState(
                        groupId = groupId,
                        groupName = cachedGroupName,
                        subunits = subunitUiMapper.toSubunitUiModelList(subunits, memberProfiles),
                        isLoading = false
                    )
                }
        }
        .combine(_dialogState) { dataState, dialogState ->
            SubunitManagementUiState(
                isLoading = dataState.isLoading,
                groupId = dataState.groupId,
                groupName = dataState.groupName,
                subunits = dataState.subunits,
                isDialogVisible = dialogState.isVisible,
                editingSubunit = dialogState.formState,
                nameError = dialogState.nameError,
                membersError = dialogState.membersError
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(AppConstants.FLOW_RETENTION_TIME),
            initialValue = SubunitManagementUiState()
        )

    fun setGroupId(groupId: String) {
        if (groupId != _groupId.value) {
            _groupId.value = groupId
        }
    }

    fun onEvent(event: SubunitManagementUiEvent) {
        when (event) {
            SubunitManagementUiEvent.ShowCreateDialog -> showCreateDialog()
            is SubunitManagementUiEvent.ShowEditDialog -> showEditDialog(event.subunitId)
            SubunitManagementUiEvent.DismissDialog -> dismissDialog()
            is SubunitManagementUiEvent.UpdateName -> updateName(event.name)
            is SubunitManagementUiEvent.ToggleMember -> toggleMember(event.userId)
            is SubunitManagementUiEvent.UpdateMemberShare -> updateMemberShare(event.userId, event.share)
            SubunitManagementUiEvent.SaveSubunit -> saveSubunit()
            is SubunitManagementUiEvent.RequestDeleteSubunit -> { /* Handled by Screen UI state */ }
            is SubunitManagementUiEvent.ConfirmDeleteSubunit -> handleDeleteSubunit(event.subunitId)
        }
    }

    private fun showCreateDialog() {
        val availableMembers = subunitUiMapper.toMemberUiModelList(
            memberIds = cachedMemberIds,
            memberProfiles = cachedMemberProfiles,
            subunits = cachedSubunits,
            excludeSubunitId = null
        )

        _dialogState.update {
            DialogState(
                isVisible = true,
                formState = SubunitFormState(
                    availableMembers = availableMembers
                )
            )
        }
    }

    private fun showEditDialog(subunitId: String) {
        val subunit = cachedSubunits.find { it.id == subunitId } ?: return

        val availableMembers = subunitUiMapper.toMemberUiModelList(
            memberIds = cachedMemberIds,
            memberProfiles = cachedMemberProfiles,
            subunits = cachedSubunits,
            excludeSubunitId = subunitId
        )

        val selectedMemberIds = subunit.memberIds.toImmutableList()
        val memberShares = subunit.memberShares.mapValues { (_, share) ->
            formatShareForInput(share)
        }

        _dialogState.update {
            DialogState(
                isVisible = true,
                formState = SubunitFormState(
                    id = subunit.id,
                    name = subunit.name,
                    selectedMemberIds = selectedMemberIds,
                    memberShares = memberShares,
                    availableMembers = availableMembers
                )
            )
        }
    }

    private fun dismissDialog() {
        _dialogState.update { DialogState() }
    }

    private fun updateName(name: String) {
        _dialogState.update { state ->
            state.copy(
                formState = state.formState?.copy(name = name),
                nameError = null
            )
        }
    }

    private fun toggleMember(userId: String) {
        _dialogState.update { state ->
            val form = state.formState ?: return@update state
            val currentIds = form.selectedMemberIds
            val updatedIds = if (userId in currentIds) {
                currentIds.filter { it != userId }.toImmutableList()
            } else {
                (currentIds + userId).toImmutableList()
            }
            // Remove share entry for deselected members
            val updatedShares = form.memberShares.filterKeys { it in updatedIds }
            state.copy(
                formState = form.copy(
                    selectedMemberIds = updatedIds,
                    memberShares = updatedShares
                ),
                membersError = null
            )
        }
    }

    private fun updateMemberShare(userId: String, share: String) {
        _dialogState.update { state ->
            val form = state.formState ?: return@update state
            val updatedShares = form.memberShares.toMutableMap().apply {
                put(userId, share)
            }
            state.copy(
                formState = form.copy(memberShares = updatedShares)
            )
        }
    }

    private fun saveSubunit() {
        val form = _dialogState.value.formState ?: return
        val groupId = _groupId.value

        // Client-side validation for immediate feedback
        if (form.name.isBlank()) {
            _dialogState.update {
                it.copy(nameError = UiText.StringResource(R.string.subunit_error_name_empty))
            }
            return
        }
        if (form.selectedMemberIds.isEmpty()) {
            _dialogState.update {
                it.copy(membersError = UiText.StringResource(R.string.subunit_error_no_members))
            }
            return
        }

        viewModelScope.launch {
            val memberShares = parseMemberShares(form)
            val subunit = Subunit(
                id = form.id,
                groupId = groupId,
                name = form.name.trim(),
                memberIds = form.selectedMemberIds.toList(),
                memberShares = memberShares
            )

            val result = if (form.isEditing) {
                updateSubunitUseCase(groupId, subunit)
                    .map { UiText.StringResource(R.string.subunit_updated_success) }
            } else {
                createSubunitUseCase(groupId, subunit)
                    .map { UiText.StringResource(R.string.subunit_created_success) }
            }

            result
                .onSuccess { message ->
                    dismissDialog()
                    _actions.emit(SubunitManagementUiAction.ShowSuccess(message))
                }
                .onFailure { error ->
                    Timber.e(error, "Failed to save subunit")
                    _actions.emit(
                        SubunitManagementUiAction.ShowError(
                            UiText.StringResource(R.string.subunit_error_save_failed)
                        )
                    )
                }
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

    /**
     * Parses the user-entered share text values into domain-level doubles.
     * If no custom shares are provided or any entry is unparseable,
     * returns empty map so the domain auto-normalization can apply.
     */
    private fun parseMemberShares(form: SubunitFormState): Map<String, Double> {
        if (form.memberShares.isEmpty()) return emptyMap()

        val allBlank = form.memberShares.values.all { it.isBlank() }
        if (allBlank) return emptyMap()

        val parsed = form.selectedMemberIds.associate { userId ->
            val shareText = form.memberShares[userId] ?: ""
            val shareValue = shareText.toDoubleOrNull()?.div(100.0)
            userId to shareValue
        }

        // If any selected member has an unparseable (non-blank) entry,
        // fall back to auto-normalization rather than silently using 0.
        if (parsed.any { (userId, value) ->
                value == null && form.memberShares[userId]?.isNotBlank() == true
            }
        ) {
            return emptyMap()
        }

        return parsed.mapValues { it.value ?: 0.0 }
    }

    private fun formatShareForInput(share: Double): String {
        val percent = share * 100
        return if (percent == percent.toLong().toDouble()) {
            percent.toLong().toString()
        } else {
            // Cap at 2 decimal places to avoid floating-point noise
            String.format("%.2f", percent).trimEnd('0').trimEnd('.')
        }
    }

    private data class DataState(
        val groupId: String = "",
        val groupName: String = "",
        val subunits: kotlinx.collections.immutable.ImmutableList<SubunitUiModel> = persistentListOf(),
        val isLoading: Boolean = true
    )

    private data class DialogState(
        val isVisible: Boolean = false,
        val formState: SubunitFormState? = null,
        val nameError: UiText? = null,
        val membersError: UiText? = null
    )
}



