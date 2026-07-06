package es.pedrazamiguez.splittrip.features.group.presentation.viewmodel.handler

import es.pedrazamiguez.splittrip.core.common.presentation.UiText
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
import es.pedrazamiguez.splittrip.domain.usecase.group.LeaveGroupUseCase
import es.pedrazamiguez.splittrip.domain.usecase.group.ObserveGroupUseCase
import es.pedrazamiguez.splittrip.domain.usecase.subunit.GetGroupSubunitsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.user.GetMemberProfilesUseCase
import es.pedrazamiguez.splittrip.features.group.R
import es.pedrazamiguez.splittrip.features.group.presentation.mapper.LeaveWizardUiMapper
import es.pedrazamiguez.splittrip.features.group.presentation.model.leave.LeaveWizardStep
import es.pedrazamiguez.splittrip.features.group.presentation.model.leave.LeaveWizardUiState
import es.pedrazamiguez.splittrip.features.group.presentation.viewmodel.action.GroupDetailUiAction
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

@Suppress("LongParameterList", "LongMethod", "CyclomaticComplexMethod", "CognitiveComplexMethod")
class GroupLeaveWizardEventHandlerImpl(
    private val authenticationService: AuthenticationService,
    private val observeGroupUseCase: ObserveGroupUseCase,
    private val getGroupExpensesFlowUseCase: GetGroupExpensesFlowUseCase,
    private val getGroupContributionsFlowUseCase: GetGroupContributionsFlowUseCase,
    private val getCashWithdrawalsFlowUseCase: GetCashWithdrawalsFlowUseCase,
    private val getGroupSubunitsFlowUseCase: GetGroupSubunitsFlowUseCase,
    private val getMemberBalancesFlowUseCase: GetMemberBalancesFlowUseCase,
    private val getSettlementSuggestionsUseCase: GetSettlementSuggestionsUseCase,
    private val areMemberSettlementsResolvedUseCase: AreMemberSettlementsResolvedUseCase,
    private val getMemberProfilesUseCase: GetMemberProfilesUseCase,
    private val confirmSettlementUseCase: ConfirmSettlementUseCase,
    private val leaveGroupUseCase: LeaveGroupUseCase,
    private val leaveWizardUiMapper: LeaveWizardUiMapper
) : GroupLeaveWizardEventHandler {

    private lateinit var localStateFlow: MutableStateFlow<GroupDetailViewModelLocalState>
    private lateinit var actionsChannel: Channel<GroupDetailUiAction>
    private lateinit var coroutineScope: CoroutineScope

    override fun bind(
        localUiStateFlow: MutableStateFlow<GroupDetailViewModelLocalState>,
        actionsChannel: Channel<GroupDetailUiAction>,
        scope: CoroutineScope
    ) {
        this.localStateFlow = localUiStateFlow
        this.actionsChannel = actionsChannel
        this.coroutineScope = scope
    }

    override fun handleLeaveClicked(groupId: String) {
        if (groupId.isBlank()) return

        coroutineScope.launch {
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

                localStateFlow.update { local ->
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
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "Failed to initialize leave wizard")
                actionsChannel.send(
                    GroupDetailUiAction.ShowError(
                        UiText.StringResource(R.string.group_leave_error_general)
                    )
                )
            }
        }
    }

    override fun handleWizardNext(groupId: String) {
        val currentState = localStateFlow.value.leaveWizardState
        val activeSteps = currentState.activeSteps
        val currentIndex = activeSteps.indexOf(currentState.currentStep)
        if (currentIndex in 0 until activeSteps.lastIndex) {
            val nextStep = activeSteps[currentIndex + 1]
            localStateFlow.update { it.copy(leaveWizardState = currentState.copy(currentStep = nextStep)) }
        } else if (currentIndex == activeSteps.lastIndex) {
            handleLeave(groupId)
        }
    }

    override fun handleWizardBack() {
        val currentState = localStateFlow.value.leaveWizardState
        val activeSteps = currentState.activeSteps
        val currentIndex = activeSteps.indexOf(currentState.currentStep)
        if (currentIndex > 0) {
            val prevStep = activeSteps[currentIndex - 1]
            localStateFlow.update { it.copy(leaveWizardState = currentState.copy(currentStep = prevStep)) }
        } else {
            localStateFlow.update { it.copy(leaveWizardState = currentState.copy(showSheet = false)) }
        }
    }

    override fun handleWizardCancelled() {
        localStateFlow.update { local ->
            local.copy(leaveWizardState = local.leaveWizardState.copy(showSheet = false))
        }
    }

    override fun handleConfirmSettlement(groupId: String, settlementId: String) {
        if (groupId.isBlank()) return

        localStateFlow.update { local ->
            local.copy(leaveWizardState = local.leaveWizardState.copy(isConfirmingSettlement = true))
        }

        coroutineScope.launch {
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
                    localStateFlow.update { local ->
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
                    localStateFlow.update { local ->
                        local.copy(
                            leaveWizardState = local.leaveWizardState.copy(isConfirmingSettlement = false)
                        )
                    }
                    actionsChannel.send(
                        GroupDetailUiAction.ShowError(
                            UiText.StringResource(R.string.settlement_overview_error_confirm)
                        )
                    )
                }
            )
        }
    }

    override fun handleLeave(groupId: String) {
        localStateFlow.update { local ->
            local.copy(
                isLeaving = true,
                leaveWizardState = local.leaveWizardState.copy(isLoading = true)
            )
        }
        coroutineScope.launch {
            leaveGroupUseCase(groupId).fold(
                onSuccess = {
                    localStateFlow.update { local ->
                        local.copy(
                            isLeaving = false,
                            leaveWizardState = local.leaveWizardState.copy(showSheet = false, isLoading = false)
                        )
                    }
                    actionsChannel.send(
                        GroupDetailUiAction.LeaveSuccess(UiText.StringResource(R.string.group_leave_success))
                    )
                },
                onFailure = { e -> handleLeaveFailure(groupId, e) }
            )
        }
    }

    private suspend fun handleLeaveFailure(groupId: String, e: Throwable) {
        val isCreator = (e as? CannotLeaveGroupException)?.reason == CannotLeaveGroupException.Reason.IS_CREATOR
        if (isCreator) {
            localStateFlow.update { local ->
                local.copy(
                    isLeaving = false,
                    leaveWizardState = local.leaveWizardState.copy(showSheet = false, isLoading = false)
                )
            }
            actionsChannel.send(
                GroupDetailUiAction.ShowError(UiText.StringResource(R.string.group_leave_error_admin))
            )
        } else if (e is UnresolvedSettlementsException) {
            handleUnresolvedSettlementsOnLeave(groupId, e)
        } else {
            localStateFlow.update { local ->
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
            actionsChannel.send(GroupDetailUiAction.ShowError(message))
        }
    }

    private suspend fun handleUnresolvedSettlementsOnLeave(groupId: String, e: UnresolvedSettlementsException) {
        val currentUserId = authenticationService.requireUserId()
        val group = observeGroupUseCase(groupId).firstOrNull()
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
        localStateFlow.update { local ->
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
        actionsChannel.send(
            GroupDetailUiAction.ShowError(UiText.StringResource(R.string.leave_wizard_unresolved_settlements_error))
        )
    }
}
