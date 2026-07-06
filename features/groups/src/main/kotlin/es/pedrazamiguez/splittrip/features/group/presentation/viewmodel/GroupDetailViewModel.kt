package es.pedrazamiguez.splittrip.features.group.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.splittrip.core.common.constant.AppConstants
import es.pedrazamiguez.splittrip.core.common.presentation.UiText
import es.pedrazamiguez.splittrip.core.designsystem.R as DesignSystemR
import es.pedrazamiguez.splittrip.domain.exception.CannotLeaveGroupException
import es.pedrazamiguez.splittrip.domain.exception.UnresolvedSettlementsException
import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import es.pedrazamiguez.splittrip.domain.usecase.balance.AreMemberSettlementsResolvedUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.ConfirmSettlementUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetCashWithdrawalsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetGroupContributionsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetMemberBalancesFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetSettlementSuggestionsUseCase
import es.pedrazamiguez.splittrip.domain.usecase.expense.GetGroupExpensesFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.group.ArchiveGroupUseCase
import es.pedrazamiguez.splittrip.domain.usecase.group.DeleteGroupUseCase
import es.pedrazamiguez.splittrip.domain.usecase.group.GetUserGroupsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.group.LeaveGroupUseCase
import es.pedrazamiguez.splittrip.domain.usecase.group.ObserveGroupUseCase
import es.pedrazamiguez.splittrip.domain.usecase.subunit.GetGroupSubunitsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.user.GetMemberProfilesUseCase
import es.pedrazamiguez.splittrip.features.group.R
import es.pedrazamiguez.splittrip.features.group.presentation.mapper.GroupUiMapper
import es.pedrazamiguez.splittrip.features.group.presentation.mapper.LeaveWizardUiMapper
import es.pedrazamiguez.splittrip.features.group.presentation.model.leave.LeaveWizardStep
import es.pedrazamiguez.splittrip.features.group.presentation.model.leave.LeaveWizardUiState
import es.pedrazamiguez.splittrip.features.group.presentation.viewmodel.action.GroupDetailUiAction
import es.pedrazamiguez.splittrip.features.group.presentation.viewmodel.event.GroupDetailUiEvent
import es.pedrazamiguez.splittrip.features.group.presentation.viewmodel.state.GroupDetailUiState
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel for the Group Detail screen.
 *
 * Reactively observes group info via [ObserveGroupUseCase] and subunit count via
 * [GetGroupSubunitsFlowUseCase]. Follows the same `_groupId`-gated `flatMapLatest` +
 * `stateIn` pattern.
 *
 * Group selection is handled in [GroupDetailFeature] via [SharedViewModel].
 */
@Suppress("LongParameterList", "LongMethod", "CyclomaticComplexMethod")
@OptIn(ExperimentalCoroutinesApi::class)
class GroupDetailViewModel(
    private val observeGroupUseCase: ObserveGroupUseCase,
    private val getGroupSubunitsFlowUseCase: GetGroupSubunitsFlowUseCase,
    private val getUserGroupsFlowUseCase: GetUserGroupsFlowUseCase,
    private val getMemberProfilesUseCase: GetMemberProfilesUseCase,
    private val groupUiMapper: GroupUiMapper,
    private val authenticationService: AuthenticationService,
    private val archiveGroupUseCase: ArchiveGroupUseCase,
    private val deleteGroupUseCase: DeleteGroupUseCase,
    private val leaveGroupUseCase: LeaveGroupUseCase,
    private val getMemberBalancesFlowUseCase: GetMemberBalancesFlowUseCase,
    private val areMemberSettlementsResolvedUseCase: AreMemberSettlementsResolvedUseCase,
    private val getSettlementSuggestionsUseCase: GetSettlementSuggestionsUseCase,
    private val confirmSettlementUseCase: ConfirmSettlementUseCase,
    private val getGroupExpensesFlowUseCase: GetGroupExpensesFlowUseCase,
    private val getGroupContributionsFlowUseCase: GetGroupContributionsFlowUseCase,
    private val getCashWithdrawalsFlowUseCase: GetCashWithdrawalsFlowUseCase,
    private val leaveWizardUiMapper: LeaveWizardUiMapper
) : ViewModel() {

    private val _groupId = MutableStateFlow("")

    private val _localUiState = MutableStateFlow(LocalUiState())

    private val _actions = Channel<GroupDetailUiAction>(Channel.BUFFERED)
    val actions = _actions.receiveAsFlow()

    val uiState: StateFlow<GroupDetailUiState> = _groupId
        .filter { it.isNotBlank() }
        .flatMapLatest { groupId ->
            observeGroupUseCase(groupId)
                .distinctUntilChanged()
                .flatMapLatest { group ->
                    if (group == null) {
                        return@flatMapLatest flowOf(GroupDetailUiState(isLoading = false, hasError = true))
                    }

                    val memberProfiles = if (group.members.isNotEmpty()) {
                        try {
                            getMemberProfilesUseCase(group.members)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            Timber.w(e, "Failed to fetch member profiles for group $groupId")
                            emptyMap()
                        }
                    } else {
                        emptyMap()
                    }

                    val groupUiModel = groupUiMapper.toGroupUiModel(group, memberProfiles)

                    combine(
                        getGroupSubunitsFlowUseCase(groupId).distinctUntilChanged(),
                        getUserGroupsFlowUseCase().distinctUntilChanged(),
                        _localUiState
                    ) { subunits, userGroups, localState ->
                        val currentUserId = authenticationService.requireUserId()
                        GroupDetailUiState(
                            group = groupUiModel,
                            isLoading = false,
                            subunitsCount = subunits.size,
                            isOnlyGroup = userGroups.size == 1,
                            showArchiveConfirmation = localState.showArchiveConfirmation,
                            isUserAdmin = group.createdBy == currentUserId,
                            isArchiving = localState.isArchiving,
                            showDeleteConfirmation = localState.showDeleteConfirmation,
                            isDeleting = localState.isDeleting,
                            showLeaveConfirmation = localState.showLeaveConfirmation,
                            isLeaving = localState.isLeaving,
                            leaveWizardState = localState.leaveWizardState
                        )
                    }
                        .catch { e ->
                            Timber.e(e, "Error loading subunits or groups for group $groupId")
                            emit(GroupDetailUiState(group = groupUiModel, isLoading = false))
                        }
                }
        }
        .catch { e ->
            Timber.e(e, "Fatal error in GroupDetailViewModel flow")
            _actions.send(
                GroupDetailUiAction.ShowError(
                    UiText.StringResource(R.string.group_detail_error_loading)
                )
            )
            emit(GroupDetailUiState(isLoading = false, hasError = true))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(
                stopTimeoutMillis = AppConstants.FLOW_RETENTION_TIME,
                replayExpirationMillis = AppConstants.FLOW_REPLAY_EXPIRATION
            ),
            initialValue = GroupDetailUiState()
        )

    fun setGroupId(groupId: String) {
        if (groupId != _groupId.value) {
            _groupId.value = groupId
        }
    }

    fun onEvent(event: GroupDetailUiEvent) {
        when (event) {
            GroupDetailUiEvent.ArchiveClicked -> _localUiState.update { it.copy(showArchiveConfirmation = true) }
            GroupDetailUiEvent.ArchiveCancelled -> _localUiState.update { it.copy(showArchiveConfirmation = false) }
            GroupDetailUiEvent.ArchiveConfirmed -> handleArchive()
            GroupDetailUiEvent.DeleteClicked -> _localUiState.update { it.copy(showDeleteConfirmation = true) }
            GroupDetailUiEvent.DeleteCancelled -> _localUiState.update { it.copy(showDeleteConfirmation = false) }
            GroupDetailUiEvent.DeleteConfirmed -> handleDelete()
            GroupDetailUiEvent.LeaveClicked -> handleLeaveClicked()
            GroupDetailUiEvent.LeaveCancelled -> _localUiState.update {
                it.copy(leaveWizardState = it.leaveWizardState.copy(showSheet = false))
            }
            GroupDetailUiEvent.LeaveConfirmed -> handleLeave()
            GroupDetailUiEvent.WizardNextClicked -> handleWizardNext()
            GroupDetailUiEvent.WizardBackClicked -> handleWizardBack()
            GroupDetailUiEvent.WizardCancelled -> _localUiState.update {
                it.copy(leaveWizardState = it.leaveWizardState.copy(showSheet = false))
            }
            is GroupDetailUiEvent.ConfirmSettlementClicked -> handleConfirmSettlement(event.settlementId)
        }
    }

    private fun handleLeaveClicked() {
        val groupId = _groupId.value
        if (groupId.isBlank()) return

        viewModelScope.launch {
            try {
                val currentUserId = authenticationService.requireUserId()
                val group = observeGroupUseCase(groupId).firstOrNull() ?: return@launch
                val expenses = getGroupExpensesFlowUseCase(groupId).firstOrNull() ?: emptyList()
                val contributions = getGroupContributionsFlowUseCase(groupId).firstOrNull() ?: emptyList()
                val withdrawals = getCashWithdrawalsFlowUseCase(groupId).firstOrNull() ?: emptyList()
                val subunits = getGroupSubunitsFlowUseCase(groupId).firstOrNull() ?: emptyList()

                val memberBalances = getMemberBalancesFlowUseCase.computeMemberBalances(
                    contributions = contributions,
                    withdrawals = withdrawals,
                    expenses = expenses,
                    subunits = subunits,
                    groupMemberIds = group.members,
                    groupCurrency = group.currency
                )

                val myBalance = memberBalances.find { it.userId == currentUserId }
                    ?: MemberBalance(userId = currentUserId)

                getSettlementSuggestionsUseCase.persistForGroup(groupId)
                val unresolvedSettlements = areMemberSettlementsResolvedUseCase(groupId, currentUserId)
                val memberProfiles = if (group.members.isNotEmpty()) {
                    getMemberProfilesUseCase(group.members)
                } else {
                    emptyMap()
                }

                val userSubunits = subunits.filter { currentUserId in it.memberShares.keys }

                val activeSteps = mutableListOf<LeaveWizardStep>()
                if (myBalance.pocketBalance != 0L || myBalance.cashInHand != 0L || myBalance.totalBalance != 0L) {
                    activeSteps.add(LeaveWizardStep.BALANCE_SUMMARY)
                }
                if (unresolvedSettlements.isNotEmpty()) {
                    activeSteps.add(LeaveWizardStep.SETTLEMENTS)
                }
                if (myBalance.cashInHand != 0L) {
                    activeSteps.add(LeaveWizardStep.CASH_RESOLUTION)
                }
                activeSteps.add(LeaveWizardStep.CONFIRMATION)

                val balanceSummary = leaveWizardUiMapper.toBalanceSummaryUiModel(myBalance, group.currency)
                val settlements = leaveWizardUiMapper.toSettlementUiModels(
                    unresolvedSettlements,
                    memberProfiles,
                    currentUserId
                )
                val cashResolution = leaveWizardUiMapper.toCashResolutionUiModel(myBalance, group.currency)
                val subunitImpact = leaveWizardUiMapper.toSubunitImpactUiModel(userSubunits)

                val initialStep = activeSteps.first()

                _localUiState.update { local ->
                    local.copy(
                        leaveWizardState = LeaveWizardUiState(
                            showSheet = true,
                            currentStep = initialStep,
                            activeSteps = activeSteps.toImmutableList(),
                            balanceSummary = balanceSummary,
                            settlements = settlements.toImmutableList(),
                            cashResolution = cashResolution,
                            subunitImpact = subunitImpact,
                            isLoading = false
                        )
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to initialize leave wizard")
                _actions.send(
                    GroupDetailUiAction.ShowError(
                        UiText.StringResource(R.string.group_leave_error_general)
                    )
                )
            }
        }
    }

    private fun handleWizardNext() {
        val currentState = _localUiState.value.leaveWizardState
        val activeSteps = currentState.activeSteps
        val currentIndex = activeSteps.indexOf(currentState.currentStep)
        if (currentIndex in 0 until activeSteps.lastIndex) {
            val nextStep = activeSteps[currentIndex + 1]
            _localUiState.update { it.copy(leaveWizardState = currentState.copy(currentStep = nextStep)) }
        } else if (currentIndex == activeSteps.lastIndex) {
            handleLeave()
        }
    }

    private fun handleWizardBack() {
        val currentState = _localUiState.value.leaveWizardState
        val activeSteps = currentState.activeSteps
        val currentIndex = activeSteps.indexOf(currentState.currentStep)
        if (currentIndex > 0) {
            val prevStep = activeSteps[currentIndex - 1]
            _localUiState.update { it.copy(leaveWizardState = currentState.copy(currentStep = prevStep)) }
        } else {
            _localUiState.update { it.copy(leaveWizardState = currentState.copy(showSheet = false)) }
        }
    }

    private fun handleConfirmSettlement(settlementId: String) {
        val groupId = _groupId.value
        if (groupId.isBlank()) return

        _localUiState.update { local ->
            local.copy(leaveWizardState = local.leaveWizardState.copy(isConfirmingSettlement = true))
        }

        viewModelScope.launch {
            confirmSettlementUseCase(groupId, settlementId).fold(
                onSuccess = {
                    val currentUserId = authenticationService.requireUserId()
                    val group = observeGroupUseCase(groupId).firstOrNull()
                    val unresolvedSettlements = areMemberSettlementsResolvedUseCase(groupId, currentUserId)
                    val memberProfiles = if (group != null && group.members.isNotEmpty()) {
                        getMemberProfilesUseCase(group.members)
                    } else {
                        emptyMap()
                    }
                    val updatedSettlements = leaveWizardUiMapper.toSettlementUiModels(
                        unresolvedSettlements,
                        memberProfiles,
                        currentUserId
                    )
                    _localUiState.update { local ->
                        local.copy(
                            leaveWizardState = local.leaveWizardState.copy(
                                settlements = updatedSettlements.toImmutableList(),
                                isConfirmingSettlement = false
                            )
                        )
                    }
                },
                onFailure = { e ->
                    Timber.e(e, "Failed to confirm settlement $settlementId")
                    _localUiState.update { local ->
                        local.copy(
                            leaveWizardState = local.leaveWizardState.copy(isConfirmingSettlement = false)
                        )
                    }
                    _actions.send(
                        GroupDetailUiAction.ShowError(
                            UiText.StringResource(R.string.settlement_overview_error_confirm)
                        )
                    )
                }
            )
        }
    }

    private fun handleArchive() {
        _localUiState.update { it.copy(showArchiveConfirmation = false, isArchiving = true) }
        viewModelScope.launch {
            archiveGroupUseCase(_groupId.value).fold(
                onSuccess = {
                    _localUiState.update { it.copy(isArchiving = false) }
                    _actions.send(
                        GroupDetailUiAction.ArchiveSuccess(
                            UiText.StringResource(R.string.group_archived_successfully)
                        )
                    )
                },
                onFailure = { e ->
                    _localUiState.update { it.copy(isArchiving = false) }
                    when (e) {
                        is UnresolvedSettlementsException ->
                            _actions.send(GroupDetailUiAction.NavigateToSettlementOverview(_groupId.value))
                        else ->
                            _actions.send(
                                GroupDetailUiAction.ShowError(
                                    UiText.StringResource(DesignSystemR.string.group_error_archiving_failed)
                                )
                            )
                    }
                }
            )
        }
    }

    private fun handleDelete() {
        _localUiState.update { it.copy(showDeleteConfirmation = false, isDeleting = true) }
        viewModelScope.launch {
            try {
                deleteGroupUseCase(_groupId.value)
                _localUiState.update { it.copy(isDeleting = false) }
                _actions.send(
                    GroupDetailUiAction.DeleteSuccess(UiText.StringResource(R.string.group_deleted_successfully))
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete group: ${_groupId.value}")
                _localUiState.update { it.copy(isDeleting = false) }
                _actions.send(GroupDetailUiAction.ShowError(UiText.StringResource(R.string.error_deleting_group)))
            }
        }
    }

    private fun handleLeave() {
        _localUiState.update { local ->
            local.copy(
                showLeaveConfirmation = false,
                isLeaving = true,
                leaveWizardState = local.leaveWizardState.copy(isLoading = true)
            )
        }
        viewModelScope.launch {
            leaveGroupUseCase(_groupId.value).fold(
                onSuccess = {
                    _localUiState.update { local ->
                        local.copy(
                            isLeaving = false,
                            leaveWizardState = local.leaveWizardState.copy(showSheet = false, isLoading = false)
                        )
                    }
                    _actions.send(GroupDetailUiAction.LeaveSuccess(UiText.StringResource(R.string.group_leave_success)))
                },
                onFailure = { e -> handleLeaveFailure(e) }
            )
        }
    }

    private suspend fun handleLeaveFailure(e: Throwable) {
        val isCreator = (e as? CannotLeaveGroupException)?.reason == CannotLeaveGroupException.Reason.IS_CREATOR
        if (isCreator) {
            _localUiState.update { local ->
                local.copy(
                    isLeaving = false,
                    leaveWizardState = local.leaveWizardState.copy(showSheet = false, isLoading = false)
                )
            }
            _actions.send(GroupDetailUiAction.ShowError(UiText.StringResource(R.string.group_leave_error_admin)))
        } else if (e is UnresolvedSettlementsException) {
            handleUnresolvedSettlementsOnLeave(e)
        } else {
            _localUiState.update { local ->
                local.copy(
                    isLeaving = false,
                    leaveWizardState = local.leaveWizardState.copy(isLoading = false)
                )
            }
            val message = when ((e as? CannotLeaveGroupException)?.reason) {
                CannotLeaveGroupException.Reason.NON_ZERO_POCKET_BALANCE ->
                    UiText.StringResource(R.string.group_leave_error_balance)
                else -> UiText.StringResource(R.string.group_leave_error_general)
            }
            _actions.send(GroupDetailUiAction.ShowError(message))
        }
    }

    private suspend fun handleUnresolvedSettlementsOnLeave(e: UnresolvedSettlementsException) {
        val currentUserId = authenticationService.requireUserId()
        val group = observeGroupUseCase(_groupId.value).firstOrNull()
        val memberProfiles = if (group != null && group.members.isNotEmpty()) {
            getMemberProfilesUseCase(group.members)
        } else {
            emptyMap()
        }
        val updatedSettlements = leaveWizardUiMapper.toSettlementUiModels(
            e.pendingSettlements,
            memberProfiles,
            currentUserId
        )
        _localUiState.update { local ->
            val currentActive = local.leaveWizardState.activeSteps.toMutableList()
            if (LeaveWizardStep.SETTLEMENTS !in currentActive) {
                val insertIndex = currentActive.indexOf(LeaveWizardStep.CONFIRMATION).coerceAtLeast(0)
                currentActive.add(insertIndex, LeaveWizardStep.SETTLEMENTS)
            }
            local.copy(
                isLeaving = false,
                leaveWizardState = local.leaveWizardState.copy(
                    activeSteps = currentActive.toImmutableList(),
                    settlements = updatedSettlements.toImmutableList(),
                    currentStep = LeaveWizardStep.SETTLEMENTS,
                    isLoading = false
                )
            )
        }
        _actions.send(
            GroupDetailUiAction.ShowError(UiText.StringResource(R.string.leave_wizard_unresolved_settlements_error))
        )
    }

    private data class LocalUiState(
        val showArchiveConfirmation: Boolean = false,
        val isArchiving: Boolean = false,
        val showDeleteConfirmation: Boolean = false,
        val isDeleting: Boolean = false,
        val showLeaveConfirmation: Boolean = false,
        val isLeaving: Boolean = false,
        val leaveWizardState: LeaveWizardUiState = LeaveWizardUiState()
    )
}
