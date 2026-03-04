package es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.core.common.constant.AppConstants
import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.domain.model.Contribution
import es.pedrazamiguez.expenseshareapp.domain.service.ContributionValidationService
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.AddContributionUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetCashWithdrawalsFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetGroupContributionsFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.GetGroupPocketBalanceFlowUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.group.GetGroupByIdUseCase
import es.pedrazamiguez.expenseshareapp.features.balance.R
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.mapper.BalancesUiMapper
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
    private val addContributionUseCase: AddContributionUseCase,
    private val getGroupByIdUseCase: GetGroupByIdUseCase,
    private val contributionValidationService: ContributionValidationService,
    private val balancesUiMapper: BalancesUiMapper
) : ViewModel() {

    private val _selectedGroupId = MutableStateFlow<String?>(null)
    private val _dialogState = MutableStateFlow(DialogState())

    private val _actions = MutableSharedFlow<BalancesUiAction>()
    val actions: SharedFlow<BalancesUiAction> = _actions.asSharedFlow()

    val uiState: StateFlow<BalancesUiState> = _selectedGroupId
        .filterNotNull()
        .flatMapLatest { groupId ->
            val group = getGroupByIdUseCase(groupId)
            val currency = group?.currency ?: AppConstants.DEFAULT_CURRENCY_CODE
            val groupName = group?.name ?: ""

            combine(
                getGroupPocketBalanceFlowUseCase(groupId, currency),
                getGroupContributionsFlowUseCase(groupId),
                getCashWithdrawalsFlowUseCase(groupId),
                _dialogState
            ) { balance, contributions, withdrawals, dialogState ->
                BalancesUiState(
                    isLoading = false,
                    groupId = groupId,
                    pocketBalance = balancesUiMapper.mapBalance(balance, groupName),
                    contributions = balancesUiMapper.mapContributions(contributions),
                    cashWithdrawals = balancesUiMapper.mapCashWithdrawals(withdrawals, currency),
                    isAddMoneyDialogVisible = dialogState.isVisible,
                    contributionAmountInput = dialogState.amountInput,
                    contributionAmountError = dialogState.amountError
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

            BalancesUiEvent.SubmitContribution -> handleSubmitContribution()
        }
    }

    private fun handleSubmitContribution() {
        val groupId = _selectedGroupId.value ?: return
        val amountText = _dialogState.value.amountInput
        val currencyCode = uiState.value.pocketBalance.currency

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

    private data class DialogState(
        val isVisible: Boolean = false,
        val amountInput: String = "",
        val amountError: Boolean = false
    )
}
