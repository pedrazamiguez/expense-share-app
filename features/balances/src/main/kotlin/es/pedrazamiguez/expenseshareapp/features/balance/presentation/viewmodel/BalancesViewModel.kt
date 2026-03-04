package es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.core.common.constant.AppConstants
import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.domain.model.CashWithdrawal
import es.pedrazamiguez.expenseshareapp.domain.model.Contribution
import es.pedrazamiguez.expenseshareapp.domain.service.CashWithdrawalValidationService
import es.pedrazamiguez.expenseshareapp.domain.service.ContributionValidationService
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.AddCashWithdrawalUseCase
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
    private val addCashWithdrawalUseCase: AddCashWithdrawalUseCase,
    private val getGroupByIdUseCase: GetGroupByIdUseCase,
    private val contributionValidationService: ContributionValidationService,
    private val cashWithdrawalValidationService: CashWithdrawalValidationService,
    private val balancesUiMapper: BalancesUiMapper
) : ViewModel() {

    private val _selectedGroupId = MutableStateFlow<String?>(null)
    private val _dialogState = MutableStateFlow(DialogState())
    private val _withdrawalDialogState = MutableStateFlow(WithdrawalDialogState())

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
                _dialogState,
                _withdrawalDialogState
            ) { balance, contributions, withdrawals, dialogState, withdrawalState ->
                BalancesUiState(
                    isLoading = false,
                    groupId = groupId,
                    pocketBalance = balancesUiMapper.mapBalance(balance, groupName),
                    contributions = balancesUiMapper.mapContributions(contributions),
                    cashWithdrawals = balancesUiMapper.mapCashWithdrawals(withdrawals),
                    isAddMoneyDialogVisible = dialogState.isVisible,
                    contributionAmountInput = dialogState.amountInput,
                    contributionAmountError = dialogState.amountError,
                    isWithdrawCashSheetVisible = withdrawalState.isVisible,
                    withdrawalAmountInput = withdrawalState.amountInput,
                    withdrawalCurrencyInput = withdrawalState.currencyInput,
                    withdrawalDeductedInput = withdrawalState.deductedInput,
                    withdrawalExchangeRateInput = withdrawalState.exchangeRateInput,
                    withdrawalAmountError = withdrawalState.amountError,
                    withdrawalDeductedError = withdrawalState.deductedError
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
            // Contribution events
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

            // Cash withdrawal events
            BalancesUiEvent.ShowWithdrawCashSheet -> {
                _withdrawalDialogState.value = WithdrawalDialogState(isVisible = true)
            }

            BalancesUiEvent.DismissWithdrawCashSheet -> {
                _withdrawalDialogState.value = WithdrawalDialogState()
            }

            is BalancesUiEvent.UpdateWithdrawalAmount -> {
                _withdrawalDialogState.value = _withdrawalDialogState.value.copy(
                    amountInput = event.amount,
                    amountError = false
                )
            }

            is BalancesUiEvent.UpdateWithdrawalCurrency -> {
                _withdrawalDialogState.value = _withdrawalDialogState.value.copy(
                    currencyInput = event.currency
                )
            }

            is BalancesUiEvent.UpdateWithdrawalDeducted -> {
                _withdrawalDialogState.value = _withdrawalDialogState.value.copy(
                    deductedInput = event.deducted,
                    deductedError = false
                )
            }

            is BalancesUiEvent.UpdateWithdrawalExchangeRate -> {
                _withdrawalDialogState.value = _withdrawalDialogState.value.copy(
                    exchangeRateInput = event.rate
                )
            }

            BalancesUiEvent.SubmitWithdrawal -> handleSubmitWithdrawal()
        }
    }

    private fun handleSubmitContribution() {
        val groupId = _selectedGroupId.value ?: return
        val amountText = _dialogState.value.amountInput
        val currencyCode = uiState.value.pocketBalance.currency

        // Delegate parsing to the mapper: respects currency-specific decimal places,
        // uses locale-safe normalization, and rounds with HALF_UP (no silent truncation).
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

    private fun handleSubmitWithdrawal() {
        val groupId = _selectedGroupId.value ?: return
        val state = _withdrawalDialogState.value
        val groupCurrency = uiState.value.pocketBalance.currency

        val withdrawalCurrency = state.currencyInput.ifBlank { groupCurrency }

        // Parse amounts
        val amountWithdrawn = balancesUiMapper.parseAmountToSmallestUnit(
            state.amountInput, withdrawalCurrency
        )
        val deductedBaseAmount = balancesUiMapper.parseAmountToSmallestUnit(
            state.deductedInput, groupCurrency
        )

        // Validate
        val amountValidation = cashWithdrawalValidationService.validateAmountWithdrawn(amountWithdrawn)
        if (amountValidation is CashWithdrawalValidationService.ValidationResult.Invalid) {
            _withdrawalDialogState.value = state.copy(amountError = true)
            return
        }

        val deductedValidation = cashWithdrawalValidationService.validateDeductedBaseAmount(deductedBaseAmount)
        if (deductedValidation is CashWithdrawalValidationService.ValidationResult.Invalid) {
            _withdrawalDialogState.value = state.copy(deductedError = true)
            return
        }

        // Calculate exchange rate: amountWithdrawn / deductedBaseAmount
        val exchangeRate = if (deductedBaseAmount > 0) {
            amountWithdrawn.toDouble() / deductedBaseAmount.toDouble()
        } else {
            1.0
        }

        viewModelScope.launch {
            try {
                val withdrawal = CashWithdrawal(
                    groupId = groupId,
                    amountWithdrawn = amountWithdrawn,
                    remainingAmount = amountWithdrawn,
                    currency = withdrawalCurrency,
                    deductedBaseAmount = deductedBaseAmount,
                    exchangeRate = exchangeRate
                )
                val result = addCashWithdrawalUseCase(groupId, withdrawal)
                result.getOrThrow()

                _withdrawalDialogState.value = WithdrawalDialogState()
                _actions.emit(
                    BalancesUiAction.ShowWithdrawalSuccess(
                        UiText.StringResource(R.string.balances_withdraw_cash_success)
                    )
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to add cash withdrawal")
                _actions.emit(
                    BalancesUiAction.ShowWithdrawalError(
                        UiText.StringResource(R.string.balances_withdraw_cash_error)
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

    private data class WithdrawalDialogState(
        val isVisible: Boolean = false,
        val amountInput: String = "",
        val currencyInput: String = "",
        val deductedInput: String = "",
        val exchangeRateInput: String = "",
        val amountError: Boolean = false,
        val deductedError: Boolean = false
    )
}
