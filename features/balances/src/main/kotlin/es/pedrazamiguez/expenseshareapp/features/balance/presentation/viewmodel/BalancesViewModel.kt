package es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.core.common.constant.AppConstants
import es.pedrazamiguez.expenseshareapp.domain.model.CashWithdrawal
import es.pedrazamiguez.expenseshareapp.domain.model.Contribution
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.domain.model.GroupPocketBalance
import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class)
class BalancesViewModel(
    private val useCases: BalancesUseCases,
    private val authenticationService: AuthenticationService,
    private val balancesUiMapper: BalancesUiMapper
) : ViewModel() {

    private val _selectedGroupId = MutableStateFlow<String?>(null)
    private val _lastSeenBalance = MutableStateFlow<String?>(null)
    private val _lastSeenBalanceCents = MutableStateFlow<Long?>(null)
    private var _currentBalanceCents: Long = 0L

    val uiState: StateFlow<BalancesUiState> = _selectedGroupId
        .filterNotNull()
        .flatMapLatest { groupId ->
            val group = useCases.getGroupByIdUseCase(groupId)
            val currency = group?.currency ?: AppConstants.DEFAULT_CURRENCY_CODE
            val groupName = group?.name ?: ""
            val currentUserId = authenticationService.currentUserId()
            val groupMemberIds = group?.members ?: emptyList()

            // Seed the in-memory cache from DataStore once per group switch
            _lastSeenBalance.value = useCases.getLastSeenBalanceUseCase(groupId).first()

            // Nested combine: inner combines 6 data flows into DataSnapshot,
            // outer pairs with lastSeenBalance for animation logic.
            // Expenses are collected here and passed to computeMemberBalances()
            // to avoid duplicate Firestore snapshot listeners.
            combine(
                combine(
                    useCases.getGroupPocketBalanceFlowUseCase(groupId, currency),
                    useCases.getGroupContributionsFlowUseCase(groupId),
                    useCases.getCashWithdrawalsFlowUseCase(groupId),
                    useCases.getGroupSubunitsFlowUseCase(groupId),
                    useCases.getGroupExpensesFlowUseCase(groupId)
                ) { balance, contributions, withdrawals, subunits, expenses ->
                    DataSnapshot(balance, contributions, withdrawals, subunits, expenses)
                },
                _lastSeenBalance
            ) { snapshot, lastSeen ->
                val balance = snapshot.balance
                val contributions = snapshot.contributions
                val withdrawals = snapshot.withdrawals
                val subunits = snapshot.subunits
                val expenses = snapshot.expenses

                // Compute member balances from already-loaded data (pure computation)
                val memberBalances = useCases.getMemberBalancesFlowUseCase.computeMemberBalances(
                    contributions = contributions,
                    withdrawals = withdrawals,
                    expenses = expenses,
                    subunits = subunits,
                    groupMemberIds = groupMemberIds,
                    groupCurrency = currency
                )

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
                val memberProfiles = useCases.getMemberProfilesUseCase(allUserIds)

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
                        memberProfiles,
                        groupCurrency = currency
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
            started = SharingStarted.WhileSubscribed(
                stopTimeoutMillis = AppConstants.FLOW_RETENTION_TIME,
                replayExpirationMillis = AppConstants.FLOW_REPLAY_EXPIRATION
            ),
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
                useCases.setLastSeenBalanceUseCase(groupId, formattedBalance)
            }
        }
    }

    private data class DataSnapshot(
        val balance: GroupPocketBalance,
        val contributions: List<Contribution>,
        val withdrawals: List<CashWithdrawal>,
        val subunits: List<Subunit>,
        val expenses: List<Expense>
    )
}
