package es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.core.common.constant.AppConstants
import es.pedrazamiguez.expenseshareapp.domain.model.CashWithdrawal
import es.pedrazamiguez.expenseshareapp.domain.model.Contribution
import es.pedrazamiguez.expenseshareapp.domain.model.GroupPocketBalance
import es.pedrazamiguez.expenseshareapp.domain.model.MemberBalance
import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetCashWithdrawalsFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetGroupContributionsFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetGroupPocketBalanceFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetMemberBalancesFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.GetGroupByIdUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetLastSeenBalanceUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.SetLastSeenBalanceUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.GetGroupSubunitsFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.user.GetMemberProfilesUseCase
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.mapper.BalancesUiMapper
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.event.BalancesUiEvent
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.BalancesUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class)
class BalancesViewModel(
    private val getGroupPocketBalanceFlowUseCase: GetGroupPocketBalanceFlowUseCase,
    private val getGroupContributionsFlowUseCase: GetGroupContributionsFlowUseCase,
    private val getCashWithdrawalsFlowUseCase: GetCashWithdrawalsFlowUseCase,
    private val getMemberBalancesFlowUseCase: GetMemberBalancesFlowUseCase,
    private val getGroupSubunitsFlowUseCase: GetGroupSubunitsFlowUseCase,
    private val getGroupByIdUseCase: GetGroupByIdUseCase,
    private val authenticationService: AuthenticationService,
    private val balancesUiMapper: BalancesUiMapper,
    private val getLastSeenBalanceUseCase: GetLastSeenBalanceUseCase,
    private val setLastSeenBalanceUseCase: SetLastSeenBalanceUseCase,
    private val getMemberProfilesUseCase: GetMemberProfilesUseCase
) : ViewModel() {

    private val _selectedGroupId = MutableStateFlow<String?>(null)
    private val _lastSeenBalance = MutableStateFlow<String?>(null)
    private val _lastSeenBalanceCents = MutableStateFlow<Long?>(null)
    private var _currentBalanceCents: Long = 0L

    val uiState: StateFlow<BalancesUiState> = _selectedGroupId
        .filterNotNull()
        .flatMapLatest { groupId ->
            val group = getGroupByIdUseCase(groupId)
            val currency = group?.currency ?: AppConstants.DEFAULT_CURRENCY_CODE
            val groupName = group?.name ?: ""
            val currentUserId = authenticationService.currentUserId()
            val groupMemberIds = group?.members ?: emptyList()

            // Seed the in-memory cache from DataStore once per group switch
            _lastSeenBalance.value = getLastSeenBalanceUseCase(groupId).first()

            // Nested combine to handle 6 data flows (Kotlin combine supports max 5 typed params)
            combine(
                combine(
                    getGroupPocketBalanceFlowUseCase(groupId, currency),
                    getGroupContributionsFlowUseCase(groupId),
                    getCashWithdrawalsFlowUseCase(groupId),
                    getGroupSubunitsFlowUseCase(groupId),
                    getMemberBalancesFlowUseCase(groupId, groupMemberIds)
                ) { balance, contributions, withdrawals, subunits, memberBalances ->
                    DataSnapshot(balance, contributions, withdrawals, subunits, memberBalances)
                },
                _lastSeenBalance
            ) { snapshot, lastSeen ->
                val (balance, contributions, withdrawals, subunits, memberBalances) = snapshot

                // Build subunit lookup map for mapper use
                val subunitsMap = subunits.associateBy { it.id }

                // Collect ALL unique user IDs from the data being displayed,
                // not just group.members — contributions/withdrawals may reference
                // users not yet in the group members list (e.g. manually-added data).
                val allUserIds = buildSet {
                    addAll(groupMemberIds)
                    contributions.forEach { add(it.userId) }
                    withdrawals.forEach { add(it.withdrawnBy) }
                    memberBalances.forEach { add(it.userId) }
                }.toList()
                val memberProfiles = getMemberProfilesUseCase(allUserIds)

                val mappedBalance = balancesUiMapper.mapBalance(balance, groupName)
                val formattedBalance = mappedBalance.formattedBalance
                val currentCents = balance.virtualBalance
                val previousCents = _lastSeenBalanceCents.value

                // Track current cents so handleBalanceAnimationComplete can snapshot it
                _currentBalanceCents = currentCents

                BalancesUiState(
                    isLoading = false,
                    groupId = groupId,
                    pocketBalance = mappedBalance,
                    contributions = balancesUiMapper.mapContributions(
                        contributions,
                        currentUserId,
                        memberProfiles,
                        subunitsMap
                    ),
                    cashWithdrawals = balancesUiMapper.mapCashWithdrawals(
                        withdrawals,
                        currency,
                        currentUserId,
                        memberProfiles,
                        subunitsMap
                    ),
                    memberBalances = balancesUiMapper.mapMemberBalances(
                        memberBalances,
                        currency,
                        currentUserId,
                        memberProfiles
                    ),
                    activityItems = balancesUiMapper.mapActivity(
                        contributions,
                        withdrawals,
                        currency,
                        currentUserId,
                        memberProfiles,
                        subunitsMap
                    ),
                    shouldAnimateBalance = formattedBalance.isNotBlank() &&
                        formattedBalance != lastSeen,
                    previousBalance = lastSeen ?: "",
                    balanceRollingUp = previousCents == null || currentCents >= previousCents
                )
            }
                .onStart {
                    emit(BalancesUiState(isLoading = true, groupId = groupId))
                }
                .catch { e ->
                    Timber.e(e, "Error loading balances for group $groupId")
                    emit(
                        BalancesUiState(
                            isLoading = false,
                            groupId = groupId,
                            errorMessage = e.localizedMessage ?: "Unknown error"
                        )
                    )
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(AppConstants.FLOW_RETENTION_TIME),
            initialValue = BalancesUiState(isLoading = true)
        )

    fun setSelectedGroup(groupId: String?) {
        if (groupId != _selectedGroupId.value) {
            _selectedGroupId.value = groupId
        }
    }

    fun onEvent(event: BalancesUiEvent) {
        when (event) {
            BalancesUiEvent.BalanceAnimationComplete -> handleBalanceAnimationComplete()
        }
    }

    private fun handleBalanceAnimationComplete() {
        val groupId = _selectedGroupId.value ?: return
        val formattedBalance = uiState.value.pocketBalance.formattedBalance
        if (formattedBalance.isNotBlank()) {
            // Update in-memory immediately → combine re-emits with shouldAnimateBalance = false
            _lastSeenBalance.value = formattedBalance
            _lastSeenBalanceCents.value = _currentBalanceCents
            // Persist to DataStore for next app launch
            viewModelScope.launch {
                setLastSeenBalanceUseCase(groupId, formattedBalance)
            }
        }
    }

    private data class DataSnapshot(
        val balance: GroupPocketBalance,
        val contributions: List<Contribution>,
        val withdrawals: List<CashWithdrawal>,
        val subunits: List<Subunit>,
        val memberBalances: List<MemberBalance>
    )
}
