package es.pedrazamiguez.splittrip.features.group.presentation.viewmodel.handler

import es.pedrazamiguez.splittrip.core.common.presentation.UiText
import es.pedrazamiguez.splittrip.domain.exception.CannotLeaveGroupException
import es.pedrazamiguez.splittrip.domain.exception.UnresolvedSettlementsException
import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import es.pedrazamiguez.splittrip.domain.usecase.balance.AreMemberSettlementsResolvedUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetCashWithdrawalsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetGroupContributionsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetGroupSettlementsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetMemberBalancesFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetSettlementSuggestionsUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.strategy.StandardContributionAttributionStrategy
import es.pedrazamiguez.splittrip.domain.usecase.balance.support.MemberBalanceCalculationInputs
import es.pedrazamiguez.splittrip.domain.usecase.expense.GetGroupExpensesFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.group.LeaveGroupUseCase
import es.pedrazamiguez.splittrip.domain.usecase.group.ObserveGroupUseCase
import es.pedrazamiguez.splittrip.domain.usecase.subunit.GetGroupSubunitsFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.user.GetMemberProfilesUseCase
import es.pedrazamiguez.splittrip.features.group.R
import es.pedrazamiguez.splittrip.features.group.presentation.mapper.LeaveWizardUiMapper
import es.pedrazamiguez.splittrip.features.group.presentation.model.leave.LeaveWizardStep
import es.pedrazamiguez.splittrip.features.group.presentation.model.leave.LeaveWizardUiState
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    private val leaveGroupUseCase: LeaveGroupUseCase,
    private val leaveWizardUiMapper: LeaveWizardUiMapper,
    private val getGroupSettlementsFlowUseCase: GetGroupSettlementsFlowUseCase
) : GroupLeaveWizardEventHandler {

    private val _wizardState = MutableStateFlow(LeaveWizardUiState())
    override val wizardState: StateFlow<LeaveWizardUiState> = _wizardState

    private lateinit var onLeaveSuccess: suspend (UiText) -> Unit
    private lateinit var onError: suspend (UiText) -> Unit
    private lateinit var coroutineScope: CoroutineScope

    override fun bind(
        scope: CoroutineScope,
        onLeaveSuccess: suspend (UiText) -> Unit,
        onError: suspend (UiText) -> Unit
    ) {
        this.coroutineScope = scope
        this.onLeaveSuccess = onLeaveSuccess
        this.onError = onError
    }

    override fun handleLeaveClicked(groupId: String) {
        if (groupId.isBlank()) return

        _wizardState.update { it.copy(isLoading = true) }

        coroutineScope.launch {
            try {
                val currentUserId = authenticationService.requireUserId()
                val group = observeGroupUseCase(groupId).firstOrNull() ?: return@launch
                val expenses = getGroupExpensesFlowUseCase(groupId).firstOrNull() ?: emptyList()
                val contributions = getGroupContributionsFlowUseCase(groupId).firstOrNull() ?: emptyList()
                val withdrawals = getCashWithdrawalsFlowUseCase(groupId).firstOrNull() ?: emptyList()
                val subunits = getGroupSubunitsFlowUseCase(groupId).firstOrNull() ?: emptyList()
                val settlementRecords = getGroupSettlementsFlowUseCase(groupId).firstOrNull() ?: emptyList()

                val memberBalances = getMemberBalancesFlowUseCase.computeMemberBalances(
                    MemberBalanceCalculationInputs(
                        contributions = contributions,
                        withdrawals = withdrawals,
                        expenses = expenses,
                        subunits = subunits,
                        groupMemberIds = group.members,
                        groupCurrency = group.currency,
                        settlements = settlementRecords,
                        attributionStrategy = StandardContributionAttributionStrategy
                    )
                )

                val myBalance = memberBalances.find { it.userId == currentUserId }
                    ?: MemberBalance(userId = currentUserId)

                getSettlementSuggestionsUseCase.persistForGroup(groupId, currentUserId)
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
                activeSteps.add(LeaveWizardStep.CONFIRMATION)

                val balanceSummary = leaveWizardUiMapper.toBalanceSummaryUiModel(
                    memberBalance = myBalance,
                    memberBalances = memberBalances,
                    currentUserId = currentUserId,
                    memberProfiles = memberProfiles,
                    currency = group.currency
                )
                val hasUnresolvedSettlements = leaveWizardUiMapper.hasUnresolvedSettlements(
                    unresolvedSettlements,
                    currentUserId
                )

                val subunitImpact = leaveWizardUiMapper.toSubunitImpactUiModel(userSubunits)

                val initialStep = activeSteps.first()

                _wizardState.update { state ->
                    state.copy(
                        showSheet = true,
                        currentStep = initialStep,
                        activeSteps = activeSteps.toImmutableList(),
                        balanceSummary = balanceSummary,
                        hasUnresolvedSettlements = hasUnresolvedSettlements,
                        subunitImpact = subunitImpact,
                        isLoading = false
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "Failed to initialize leave wizard")
                onError(UiText.StringResource(R.string.group_leave_error_general))
            }
        }
    }

    override fun handleWizardNext(groupId: String) {
        val currentState = _wizardState.value
        val activeSteps = currentState.activeSteps
        val currentIndex = activeSteps.indexOf(currentState.currentStep)
        if (currentIndex in 0 until activeSteps.lastIndex) {
            val nextStep = activeSteps[currentIndex + 1]
            _wizardState.update { it.copy(currentStep = nextStep) }
        } else if (currentIndex == activeSteps.lastIndex) {
            handleLeave(groupId)
        }
    }

    override fun handleWizardBack() {
        val currentState = _wizardState.value
        val activeSteps = currentState.activeSteps
        val currentIndex = activeSteps.indexOf(currentState.currentStep)
        if (currentIndex > 0) {
            val prevStep = activeSteps[currentIndex - 1]
            _wizardState.update { it.copy(currentStep = prevStep) }
        } else {
            _wizardState.update { it.copy(showSheet = false) }
        }
    }

    override fun handleWizardCancelled() {
        _wizardState.update { it.copy(showSheet = false) }
    }

    override fun handleLeave(groupId: String) {
        _wizardState.update { state ->
            state.copy(
                isLeaving = true,
                isLoading = true
            )
        }
        coroutineScope.launch {
            leaveGroupUseCase(groupId).fold(
                onSuccess = {
                    _wizardState.update { state ->
                        state.copy(
                            isLeaving = false,
                            showSheet = false,
                            isLoading = false
                        )
                    }
                    onLeaveSuccess(UiText.StringResource(R.string.group_leave_success))
                },
                onFailure = { e -> handleLeaveFailure(e) }
            )
        }
    }

    private suspend fun handleLeaveFailure(e: Throwable) {
        val isCreator = (e as? CannotLeaveGroupException)?.reason == CannotLeaveGroupException.Reason.IS_CREATOR
        if (isCreator) {
            _wizardState.update { state ->
                state.copy(
                    isLeaving = false,
                    showSheet = false,
                    isLoading = false
                )
            }
            onError(UiText.StringResource(R.string.group_leave_error_admin))
        } else if (e is UnresolvedSettlementsException) {
            handleUnresolvedSettlementsOnLeave()
        } else {
            _wizardState.update { state ->
                state.copy(
                    isLeaving = false,
                    isLoading = false
                )
            }
            val message = when ((e as? CannotLeaveGroupException)?.reason) {
                CannotLeaveGroupException.Reason.NON_ZERO_POCKET_BALANCE ->
                    UiText.StringResource(R.string.group_leave_error_balance)
                else -> UiText.StringResource(R.string.group_leave_error_general)
            }
            onError(message)
        }
    }

    private suspend fun handleUnresolvedSettlementsOnLeave() {
        _wizardState.update { state ->
            state.copy(
                isLeaving = false,
                hasUnresolvedSettlements = true,
                currentStep = LeaveWizardStep.CONFIRMATION,
                isLoading = false
            )
        }
        onError(UiText.StringResource(R.string.leave_wizard_unresolved_settlements_error))
    }

    override fun handleJumpToStep(step: LeaveWizardStep) {
        if (step in _wizardState.value.activeSteps) {
            _wizardState.update { it.copy(currentStep = step) }
        }
    }
}
