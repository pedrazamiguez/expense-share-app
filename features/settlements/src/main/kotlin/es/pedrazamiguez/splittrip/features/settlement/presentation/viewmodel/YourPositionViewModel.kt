package es.pedrazamiguez.splittrip.features.settlement.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.splittrip.core.common.constant.AppConstants
import es.pedrazamiguez.splittrip.core.common.network.NetworkMonitor
import es.pedrazamiguez.splittrip.domain.model.CashWithdrawal
import es.pedrazamiguez.splittrip.domain.model.Contribution
import es.pedrazamiguez.splittrip.domain.model.Expense
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.Subunit
import es.pedrazamiguez.splittrip.domain.service.AppConfigService
import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import es.pedrazamiguez.splittrip.domain.usecase.balance.support.MemberBalanceCalculationInputs
import es.pedrazamiguez.splittrip.features.settlement.presentation.mapper.MemberSpendingChartUiMapper
import es.pedrazamiguez.splittrip.features.settlement.presentation.mapper.SettlementConsensusUiMapper
import es.pedrazamiguez.splittrip.features.settlement.presentation.mapper.YourPositionUiMapper
import es.pedrazamiguez.splittrip.features.settlement.presentation.viewmodel.action.YourPositionUiAction
import es.pedrazamiguez.splittrip.features.settlement.presentation.viewmodel.delegate.YourPositionActionDelegate
import es.pedrazamiguez.splittrip.features.settlement.presentation.viewmodel.event.YourPositionUiEvent
import es.pedrazamiguez.splittrip.features.settlement.presentation.viewmodel.state.YourPositionUiState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class YourPositionViewModel(
    private val useCases: YourPositionUseCases,
    private val actionDelegate: YourPositionActionDelegate,
    private val authenticationService: AuthenticationService,
    private val yourPositionUiMapper: YourPositionUiMapper,
    private val settlementConsensusUiMapper: SettlementConsensusUiMapper,
    private val memberSpendingChartUiMapper: MemberSpendingChartUiMapper,
    private val appConfigService: AppConfigService,
    private val networkMonitor: NetworkMonitor,
    private val computationDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    private val _selectedGroupId = MutableStateFlow<String?>(null)
    private val _isCashBreakdownVisible = MutableStateFlow(false)
    private val _isChartCashOnly = MutableStateFlow<Boolean?>(null)

    private val _actions = Channel<YourPositionUiAction>(Channel.BUFFERED)
    val actions = _actions.receiveAsFlow()

    val uiState: StateFlow<YourPositionUiState> = _selectedGroupId
        .filterNotNull()
        .flatMapLatest { groupId ->
            val group = useCases.getGroupByIdUseCase(groupId)
            val currency = group?.currency ?: appConfigService.defaultCurrencyCode.value
            val groupMemberIds = group?.members ?: emptyList()
            val currentUserId = authenticationService.currentUserId() ?: ""
            var lastTransactionSignature: String? = null

            val transactionsFlow = combine(
                useCases.getGroupContributionsFlowUseCase(groupId),
                useCases.getCashWithdrawalsFlowUseCase(groupId),
                useCases.getGroupExpensesFlowUseCase(groupId),
                useCases.getGroupSubunitsFlowUseCase(groupId),
                useCases.getGroupSettlementsFlowUseCase(groupId)
            ) { contributions, withdrawals, expenses, subunits, settlements ->
                TransactionData(
                    contributions = contributions,
                    withdrawals = withdrawals,
                    expenses = expenses,
                    subunits = subunits,
                    settlements = settlements
                )
            }

            val baseStateFlow = combine(
                transactionsFlow,
                useCases.getNudgeTimestampsFlowUseCase(),
                appConfigService.settlementNudgeRateLimitHours
            ) { txData, nudgeTimestamps, rateLimitHours ->
                DataSnapshot(
                    contributions = txData.contributions,
                    withdrawals = txData.withdrawals,
                    expenses = txData.expenses,
                    subunits = txData.subunits,
                    settlements = txData.settlements,
                    nudgeTimestamps = nudgeTimestamps,
                    rateLimitHours = rateLimitHours
                )
            }
                .onEach { snapshot ->
                    val totalExpensesAmount = snapshot.expenses.sumOf { it.sourceAmount }
                    val signature = "${snapshot.contributions.size}_${snapshot.withdrawals.size}_" +
                        "${snapshot.expenses.size}_${snapshot.subunits.size}_$totalExpensesAmount"
                    if (signature != lastTransactionSignature) {
                        lastTransactionSignature = signature
                        viewModelScope.launch {
                            try {
                                useCases.getSettlementSuggestionsUseCase.persistForGroup(groupId)
                            } catch (e: Exception) {
                                Timber.e(e, "Failed to persist settlement suggestions for group $groupId")
                            }
                        }
                    }
                }
                .debounce { appConfigService.balanceComputationDebounceMs.value }
                .combine(_isCashBreakdownVisible) { snapshot, isCashBreakdownVisible ->
                    Pair(snapshot, isCashBreakdownVisible)
                }
                .combine(_isChartCashOnly) { (snapshot, isCashBreakdownVisible), isChartCashOnly ->
                    val hasCash = snapshot.withdrawals.isNotEmpty()
                    val actualIsChartCashOnly = isChartCashOnly ?: hasCash

                    val memberBalances = useCases.getMemberBalancesFlowUseCase.computeMemberBalances(
                        MemberBalanceCalculationInputs(
                            contributions = snapshot.contributions,
                            withdrawals = snapshot.withdrawals,
                            expenses = snapshot.expenses,
                            subunits = snapshot.subunits,
                            groupMemberIds = groupMemberIds,
                            groupCurrency = currency,
                            settlements = snapshot.settlements
                        )
                    )

                    val currentMemberBalance = memberBalances.firstOrNull {
                        it.userId == currentUserId
                    }

                    val subunitsMap = snapshot.subunits.associateBy { it.id }

                    val personalPosition = currentMemberBalance?.let { balance ->
                        yourPositionUiMapper.toPersonalPosition(
                            memberBalance = balance,
                            groupCurrencyCode = currency,
                            withdrawals = snapshot.withdrawals,
                            subunitsMap = subunitsMap,
                            groupMemberIds = groupMemberIds
                        )
                    }

                    val memberProfiles = if (groupMemberIds.isNotEmpty()) {
                        try {
                            useCases.getMemberProfilesUseCase(groupMemberIds)
                        } catch (e: Exception) {
                            Timber.w(e, "Failed to fetch member profiles for group $groupId")
                            emptyMap()
                        }
                    } else {
                        emptyMap()
                    }

                    val settlementConsensus = settlementConsensusUiMapper.toConsensusItems(
                        settlements = snapshot.settlements,
                        currentUserId = currentUserId,
                        groupCreatorId = group?.createdBy ?: "",
                        memberProfiles = memberProfiles,
                        nudgeTimestamps = snapshot.nudgeTimestamps,
                        rateLimitHours = snapshot.rateLimitHours
                    )

                    val spendingChart = memberSpendingChartUiMapper.toChartUiModel(
                        memberBalances = memberBalances,
                        cashOnly = actualIsChartCashOnly,
                        currentUserId = currentUserId,
                        memberProfiles = memberProfiles,
                        groupCurrencyCode = currency
                    )

                    YourPositionUiState(
                        isLoading = false,
                        personalPosition = personalPosition,
                        isCashBreakdownVisible = isCashBreakdownVisible,
                        settlementConsensus = settlementConsensus,
                        spendingChart = spendingChart,
                        isChartCashOnly = actualIsChartCashOnly
                    )
                }

            combine(baseStateFlow, actionDelegate.localState, networkMonitor.isOnline) {
                    baseState,
                    localState,
                    isOnline
                ->
                baseState.copy(
                    isOffline = !isOnline,
                    activeDisputeSettlementId = localState.activeDisputeSettlementId,
                    disputeReasonInput = localState.disputeReasonInput
                )
            }
                .catch { e ->
                    Timber.e(e, "Error loading personal position for group $groupId")
                    emit(YourPositionUiState(isLoading = false))
                }
                .flowOn(computationDispatcher)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(
                stopTimeoutMillis = AppConstants.FLOW_RETENTION_TIME,
                replayExpirationMillis = AppConstants.FLOW_REPLAY_EXPIRATION
            ),
            initialValue = YourPositionUiState(isLoading = true)
        )

    fun setSelectedGroup(groupId: String?) {
        if (groupId != _selectedGroupId.value) {
            _selectedGroupId.value = groupId
            groupId?.let { gid ->
                viewModelScope.launch {
                    try {
                        useCases.getSettlementSuggestionsUseCase.persistForGroup(gid)
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to persist settlement suggestions for group $gid")
                    }
                }
            }
        }
    }

    fun onEvent(event: YourPositionUiEvent) {
        when (event) {
            YourPositionUiEvent.Refresh -> { /* no-op */ }
            YourPositionUiEvent.ShowCashBreakdown -> {
                _isCashBreakdownVisible.value = true
            }
            YourPositionUiEvent.DismissCashBreakdown -> {
                _isCashBreakdownVisible.value = false
            }
            is YourPositionUiEvent.ConfirmSettlement -> viewModelScope.launch {
                actionDelegate.handleConfirm(
                    event.settlementId,
                    _selectedGroupId.value,
                    uiState.value.isOffline,
                    _actions
                )
            }
            is YourPositionUiEvent.DisputeSettlement -> viewModelScope.launch {
                actionDelegate.handleOpenDispute(event.settlementId, uiState.value.isOffline, _actions)
            }
            is YourPositionUiEvent.DisputeReasonChanged -> actionDelegate.updateDisputeReason(event.reason)
            YourPositionUiEvent.DisputeSubmitted -> viewModelScope.launch {
                actionDelegate.handleSubmitDispute(_selectedGroupId.value, uiState.value.isOffline, _actions)
            }
            YourPositionUiEvent.DisputeCancelled -> actionDelegate.handleCancelDispute()
            is YourPositionUiEvent.NudgeDebtor -> viewModelScope.launch {
                actionDelegate.handleNudgeDebtor(
                    event.settlementId,
                    _selectedGroupId.value,
                    uiState.value.isOffline,
                    _actions
                )
            }
            is YourPositionUiEvent.ChartModeToggled -> _isChartCashOnly.value = event.cashOnly
        }
    }

    private data class TransactionData(
        val contributions: List<Contribution>,
        val withdrawals: List<CashWithdrawal>,
        val expenses: List<Expense>,
        val subunits: List<Subunit>,
        val settlements: List<SettlementRecord>
    )

    private data class DataSnapshot(
        val contributions: List<Contribution>,
        val withdrawals: List<CashWithdrawal>,
        val expenses: List<Expense>,
        val subunits: List<Subunit>,
        val settlements: List<SettlementRecord>,
        val nudgeTimestamps: Map<String, Long>,
        val rateLimitHours: Long
    )
}
