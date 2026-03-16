package es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.core.common.constant.AppConstants
import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.domain.model.CashWithdrawal
import es.pedrazamiguez.expenseshareapp.domain.model.Contribution
import es.pedrazamiguez.expenseshareapp.domain.model.GroupPocketBalance
import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.domain.service.ContributionValidationService
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.AddContributionUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetCashWithdrawalsFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetGroupContributionsFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetGroupPocketBalanceFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.GetGroupByIdUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetLastSeenBalanceUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.SetLastSeenBalanceUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.subunit.GetGroupSubunitsFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.user.GetMemberProfilesUseCase
import es.pedrazamiguez.expenseshareapp.features.balance.R
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.mapper.BalancesUiMapper
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.SubunitOptionUiModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.action.BalancesUiAction
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.event.BalancesUiEvent
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.BalancesUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.collections.immutable.toImmutableList
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class)
class BalancesViewModel(
    private val getGroupPocketBalanceFlowUseCase: GetGroupPocketBalanceFlowUseCase,
    private val getGroupContributionsFlowUseCase: GetGroupContributionsFlowUseCase,
    private val getCashWithdrawalsFlowUseCase: GetCashWithdrawalsFlowUseCase,
    private val getGroupSubunitsFlowUseCase: GetGroupSubunitsFlowUseCase,
    private val addContributionUseCase: AddContributionUseCase,
    private val getGroupByIdUseCase: GetGroupByIdUseCase,
    private val authenticationService: AuthenticationService,
    private val contributionValidationService: ContributionValidationService,
    private val balancesUiMapper: BalancesUiMapper,
    private val getLastSeenBalanceUseCase: GetLastSeenBalanceUseCase,
    private val setLastSeenBalanceUseCase: SetLastSeenBalanceUseCase,
    private val getMemberProfilesUseCase: GetMemberProfilesUseCase
) : ViewModel() {

    private val _selectedGroupId = MutableStateFlow<String?>(null)
    private val _dialogState = MutableStateFlow(DialogState())
    private val _lastSeenBalance = MutableStateFlow<String?>(null)
    private val _lastSeenBalanceCents = MutableStateFlow<Long?>(null)
    private var _currentBalanceCents: Long = 0L

    private val _actions = MutableSharedFlow<BalancesUiAction>()
    val actions: SharedFlow<BalancesUiAction> = _actions.asSharedFlow()

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

            combine(
                getGroupPocketBalanceFlowUseCase(groupId, currency),
                getGroupContributionsFlowUseCase(groupId),
                getCashWithdrawalsFlowUseCase(groupId),
                getGroupSubunitsFlowUseCase(groupId),
                _dialogState,
                _lastSeenBalance
            ) { values ->
                @Suppress("UNCHECKED_CAST")
                val balance = values[0] as GroupPocketBalance
                @Suppress("UNCHECKED_CAST")
                val contributions = values[1] as List<Contribution>
                @Suppress("UNCHECKED_CAST")
                val withdrawals = values[2] as List<CashWithdrawal>
                @Suppress("UNCHECKED_CAST")
                val subunits = values[3] as List<Subunit>
                val dialogState = values[4] as DialogState
                val lastSeen = values[5] as String?

                // Build subunit lookup map for mapper use
                val subunitsMap = subunits.associateBy { it.id }

                // Build sub-unit options for the current user
                val userSubunitOptions = subunits
                    .filter { currentUserId != null && currentUserId in it.memberIds }
                    .map { SubunitOptionUiModel(id = it.id, name = it.name) }
                    .toImmutableList()

                // Collect ALL unique user IDs from the data being displayed,
                // not just group.members — contributions/withdrawals may reference
                // users not yet in the group members list (e.g. manually-added data).
                val allUserIds = buildSet {
                    addAll(groupMemberIds)
                    contributions.forEach { add(it.userId) }
                    withdrawals.forEach { add(it.withdrawnBy) }
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
                    isAddMoneyDialogVisible = dialogState.isVisible,
                    contributionAmountInput = dialogState.amountInput,
                    contributionAmountError = dialogState.amountError,
                    contributionSubunitOptions = userSubunitOptions,
                    contributionSelectedSubunitId = dialogState.selectedSubunitId,
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
            BalancesUiEvent.ShowAddMoneyDialog -> {
                _dialogState.value = DialogState(isVisible = true)
            }

            BalancesUiEvent.DismissAddMoneyDialog -> {
                _dialogState.value = DialogState()
            }

            is BalancesUiEvent.UpdateContributionAmount -> {
                _dialogState.value = _dialogState.value.copy(
                    amountInput = event.amount,
                    amountError = false
                )
            }

            is BalancesUiEvent.SelectContributionSubunit -> {
                _dialogState.value = _dialogState.value.copy(
                    selectedSubunitId = event.subunitId
                )
            }

            BalancesUiEvent.SubmitContribution -> handleSubmitContribution()

            BalancesUiEvent.BalanceAnimationComplete -> handleBalanceAnimationComplete()
        }
    }

    private fun handleSubmitContribution() {
        val groupId = _selectedGroupId.value ?: return
        val amountText = _dialogState.value.amountInput
        val currencyCode = uiState.value.pocketBalance.currency
        val selectedSubunitId = _dialogState.value.selectedSubunitId

        val amountInSmallestUnit = balancesUiMapper.parseAmountToSmallestUnit(amountText, currencyCode)

        val validationResult = contributionValidationService.validateAmount(amountInSmallestUnit)
        if (validationResult is ContributionValidationService.ValidationResult.Invalid) {
            _dialogState.value = _dialogState.value.copy(amountError = true)
            return
        }

        viewModelScope.launch {
            try {
                val contribution = Contribution(
                    groupId = groupId,
                    subunitId = selectedSubunitId,
                    amount = amountInSmallestUnit,
                    currency = uiState.value.pocketBalance.currency
                )
                addContributionUseCase(groupId, contribution)
                _dialogState.value = DialogState()
                _actions.emit(
                    BalancesUiAction.ShowContributionSuccess(
                        UiText.StringResource(R.string.balances_add_money_success)
                    )
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to add contribution")
                _actions.emit(
                    BalancesUiAction.ShowContributionError(
                        UiText.StringResource(R.string.balances_add_money_error)
                    )
                )
            }
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

    private data class DialogState(
        val isVisible: Boolean = false,
        val amountInput: String = "",
        val amountError: Boolean = false,
        val selectedSubunitId: String? = null
    )
}
