package es.pedrazamiguez.expenseshareapp.features.withdrawal.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.domain.enums.PayerType
import es.pedrazamiguez.expenseshareapp.features.withdrawal.presentation.viewmodel.action.AddCashWithdrawalUiAction
import es.pedrazamiguez.expenseshareapp.features.withdrawal.presentation.viewmodel.event.AddCashWithdrawalUiEvent
import es.pedrazamiguez.expenseshareapp.features.withdrawal.presentation.viewmodel.handler.WithdrawalConfigHandler
import es.pedrazamiguez.expenseshareapp.features.withdrawal.presentation.viewmodel.handler.WithdrawalCurrencyHandler
import es.pedrazamiguez.expenseshareapp.features.withdrawal.presentation.viewmodel.handler.WithdrawalFeeHandler
import es.pedrazamiguez.expenseshareapp.features.withdrawal.presentation.viewmodel.handler.WithdrawalSubmitHandler
import es.pedrazamiguez.expenseshareapp.features.withdrawal.presentation.viewmodel.state.AddCashWithdrawalUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Thin router ViewModel for the cash withdrawal wizard.
 *
 * All business logic is delegated to co-created handler classes:
 * - [WithdrawalConfigHandler] — group config loading
 * - [WithdrawalCurrencyHandler] — currency selection & exchange rate
 * - [WithdrawalFeeHandler] — ATM fee management
 * - [WithdrawalSubmitHandler] — validation & submission
 *
 * The ViewModel only manages wizard step navigation inline (lightweight state updates).
 */
class AddCashWithdrawalViewModel(
    private val configHandler: WithdrawalConfigHandler,
    private val currencyHandler: WithdrawalCurrencyHandler,
    private val feeHandler: WithdrawalFeeHandler,
    private val submitHandler: WithdrawalSubmitHandler
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddCashWithdrawalUiState())
    val uiState: StateFlow<AddCashWithdrawalUiState> = _uiState.asStateFlow()

    private val _actions = MutableSharedFlow<AddCashWithdrawalUiAction>()
    val actions: SharedFlow<AddCashWithdrawalUiAction> = _actions.asSharedFlow()

    init {
        configHandler.bind(_uiState, _actions, viewModelScope)
        currencyHandler.bind(_uiState, _actions, viewModelScope)
        feeHandler.bind(_uiState, _actions, viewModelScope)
        submitHandler.bind(_uiState, _actions, viewModelScope)
    }

    // Thin router — every branch is a single delegation;
    // complexity is proportional to event count, not logic
    @Suppress("CyclomaticComplexMethod")
    fun onEvent(event: AddCashWithdrawalUiEvent, onSuccess: () -> Unit = {}) {
        when (event) {
            // ── Config ──────────────────────────────────────────────────
            is AddCashWithdrawalUiEvent.LoadGroupConfig ->
                configHandler.loadGroupConfig(event.groupId)

            is AddCashWithdrawalUiEvent.RetryLoadConfig -> {
                _uiState.update { it.copy(configLoadFailed = false, error = null) }
                configHandler.loadGroupConfig(event.groupId, forceRefresh = true)
            }

            // ── Currency & Exchange Rate ─────────────────────────────────
            is AddCashWithdrawalUiEvent.CurrencySelected ->
                currencyHandler.handleCurrencySelected(event.currencyCode)

            is AddCashWithdrawalUiEvent.WithdrawalAmountChanged ->
                currencyHandler.handleWithdrawalAmountChanged(event.amount)

            is AddCashWithdrawalUiEvent.DeductedAmountChanged ->
                currencyHandler.handleDeductedAmountChanged(event.amount)

            is AddCashWithdrawalUiEvent.ExchangeRateChanged ->
                currencyHandler.handleExchangeRateChanged(event.rate)

            // ── Details ──────────────────────────────────────────────────
            is AddCashWithdrawalUiEvent.WithdrawalScopeSelected ->
                _uiState.update {
                    it.copy(
                        withdrawalScope = event.scope,
                        selectedSubunitId = if (event.scope == PayerType.SUBUNIT) {
                            event.subunitId
                        } else {
                            null
                        }
                    )
                }

            is AddCashWithdrawalUiEvent.TitleChanged ->
                _uiState.update { it.copy(title = event.title) }

            is AddCashWithdrawalUiEvent.NotesChanged ->
                _uiState.update { it.copy(notes = event.notes) }

            // ── ATM Fee ──────────────────────────────────────────────────
            is AddCashWithdrawalUiEvent.FeeToggled ->
                feeHandler.handleFeeToggled(event.hasFee)

            is AddCashWithdrawalUiEvent.FeeAmountChanged ->
                feeHandler.handleFeeAmountChanged(event.amount)

            is AddCashWithdrawalUiEvent.FeeCurrencySelected ->
                feeHandler.handleFeeCurrencySelected(event.currencyCode)

            is AddCashWithdrawalUiEvent.FeeExchangeRateChanged ->
                feeHandler.handleFeeExchangeRateChanged(event.rate)

            is AddCashWithdrawalUiEvent.FeeConvertedAmountChanged ->
                feeHandler.handleFeeConvertedAmountChanged(event.amount)

            // ── Submit ───────────────────────────────────────────────────
            is AddCashWithdrawalUiEvent.SubmitWithdrawal ->
                submitHandler.submitWithdrawal(event.groupId, onSuccess)

            // ── Wizard Navigation ────────────────────────────────────────
            AddCashWithdrawalUiEvent.NextStep -> navigateNext()
            AddCashWithdrawalUiEvent.PreviousStep -> navigatePrevious()
        }
    }

    private fun navigateNext() {
        val state = _uiState.value
        val steps = state.applicableSteps
        val currentIndex = state.currentStepIndex
        if (currentIndex < steps.lastIndex) {
            _uiState.update { it.copy(currentStep = steps[currentIndex + 1]) }
        }
    }

    private fun navigatePrevious() {
        val state = _uiState.value
        val steps = state.applicableSteps
        val currentIndex = state.currentStepIndex
        if (currentIndex > 0) {
            _uiState.update { it.copy(currentStep = steps[currentIndex - 1]) }
        } else {
            // On first step — signal the Feature to pop the back stack
            viewModelScope.launch {
                _actions.emit(AddCashWithdrawalUiAction.NavigateBack)
            }
        }
    }
}
