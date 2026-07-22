package es.pedrazamiguez.splittrip.features.settlement.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.splittrip.core.common.constant.AppConstants
import es.pedrazamiguez.splittrip.domain.model.CashWithdrawal
import es.pedrazamiguez.splittrip.domain.model.Contribution
import es.pedrazamiguez.splittrip.domain.model.Expense
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.Subunit
import es.pedrazamiguez.splittrip.domain.service.AppConfigService
import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import es.pedrazamiguez.splittrip.domain.usecase.balance.support.MemberBalanceCalculationInputs
import es.pedrazamiguez.splittrip.features.settlement.presentation.mapper.YourPositionUiMapper
import es.pedrazamiguez.splittrip.features.settlement.presentation.viewmodel.event.YourPositionUiEvent
import es.pedrazamiguez.splittrip.features.settlement.presentation.viewmodel.state.YourPositionUiState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class YourPositionViewModel(
    private val useCases: YourPositionUseCases,
    private val authenticationService: AuthenticationService,
    private val yourPositionUiMapper: YourPositionUiMapper,
    private val appConfigService: AppConfigService,
    private val computationDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    private val _selectedGroupId = MutableStateFlow<String?>(null)
    private val _isCashBreakdownVisible = MutableStateFlow(false)

    val uiState: StateFlow<YourPositionUiState> = _selectedGroupId
        .filterNotNull()
        .flatMapLatest { groupId ->
            val group = useCases.getGroupByIdUseCase(groupId)
            val currency = group?.currency ?: appConfigService.defaultCurrencyCode.value
            val groupMemberIds = group?.members ?: emptyList()
            val currentUserId = authenticationService.currentUserId()

            combine(
                useCases.getGroupContributionsFlowUseCase(groupId),
                useCases.getCashWithdrawalsFlowUseCase(groupId),
                useCases.getGroupExpensesFlowUseCase(groupId),
                useCases.getGroupSubunitsFlowUseCase(groupId),
                useCases.getGroupSettlementsFlowUseCase(groupId)
            ) { contributions, withdrawals, expenses, subunits, settlements ->
                DataSnapshot(
                    contributions = contributions,
                    withdrawals = withdrawals,
                    expenses = expenses,
                    subunits = subunits,
                    settlements = settlements
                )
            }
                .debounce { appConfigService.balanceComputationDebounceMs.value }
                .combine(_isCashBreakdownVisible) { snapshot, isCashBreakdownVisible ->
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

                    YourPositionUiState(
                        isLoading = false,
                        personalPosition = personalPosition,
                        isCashBreakdownVisible = isCashBreakdownVisible
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
        }
    }

    private data class DataSnapshot(
        val contributions: List<Contribution>,
        val withdrawals: List<CashWithdrawal>,
        val expenses: List<Expense>,
        val subunits: List<Subunit>,
        val settlements: List<SettlementRecord>
    )
}
