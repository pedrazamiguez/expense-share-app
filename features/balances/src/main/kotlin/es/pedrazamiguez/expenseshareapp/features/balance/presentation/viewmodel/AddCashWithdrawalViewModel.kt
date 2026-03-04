package es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.domain.model.CashWithdrawal
import es.pedrazamiguez.expenseshareapp.domain.service.CashWithdrawalValidationService
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseCalculatorService
import es.pedrazamiguez.expenseshareapp.domain.usecase.balance.AddCashWithdrawalUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.currency.GetExchangeRateUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.GetGroupExpenseConfigUseCase
import es.pedrazamiguez.expenseshareapp.features.balance.R
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.mapper.AddCashWithdrawalUiMapper
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.action.AddCashWithdrawalUiAction
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.event.AddCashWithdrawalUiEvent
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.AddCashWithdrawalUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class AddCashWithdrawalViewModel(
    private val addCashWithdrawalUseCase: AddCashWithdrawalUseCase,
    private val getGroupExpenseConfigUseCase: GetGroupExpenseConfigUseCase,
    private val getExchangeRateUseCase: GetExchangeRateUseCase,
    private val expenseCalculatorService: ExpenseCalculatorService,
    private val cashWithdrawalValidationService: CashWithdrawalValidationService,
    private val mapper: AddCashWithdrawalUiMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddCashWithdrawalUiState())
    val uiState: StateFlow<AddCashWithdrawalUiState> = _uiState.asStateFlow()

    private val _actions = MutableSharedFlow<AddCashWithdrawalUiAction>()
    val actions: SharedFlow<AddCashWithdrawalUiAction> = _actions.asSharedFlow()

    fun onEvent(event: AddCashWithdrawalUiEvent, onSuccess: () -> Unit = {}) {
        when (event) {
            is AddCashWithdrawalUiEvent.LoadGroupConfig -> loadGroupConfig(event.groupId)

            is AddCashWithdrawalUiEvent.RetryLoadConfig -> {
                _uiState.update { it.copy(configLoadFailed = false, error = null) }
                loadGroupConfig(event.groupId, forceRefresh = true)
            }

            is AddCashWithdrawalUiEvent.CurrencySelected -> handleCurrencySelected(event.currencyCode)

            is AddCashWithdrawalUiEvent.WithdrawalAmountChanged -> {
                _uiState.update {
                    it.copy(withdrawalAmount = event.amount, isAmountValid = true, error = null)
                }
                recalculateDeducted()
            }

            is AddCashWithdrawalUiEvent.DeductedAmountChanged -> {
                _uiState.update { it.copy(deductedAmount = event.amount) }
                recalculateRateFromDeducted()
            }

            is AddCashWithdrawalUiEvent.ExchangeRateChanged -> {
                _uiState.update { it.copy(displayExchangeRate = event.rate) }
                recalculateDeducted()
            }

            is AddCashWithdrawalUiEvent.SubmitWithdrawal -> submitWithdrawal(
                event.groupId,
                onSuccess
            )
        }
    }

    private fun loadGroupConfig(groupId: String?, forceRefresh: Boolean = false) {
        if (groupId == null) return

        val currentState = _uiState.value
        val isGroupChanged = currentState.loadedGroupId != groupId

        if (!forceRefresh && !isGroupChanged && currentState.isConfigLoaded) return

        viewModelScope.launch {
            if (isGroupChanged) {
                _uiState.update {
                    AddCashWithdrawalUiState(isLoading = true, configLoadFailed = false)
                }
            } else {
                _uiState.update { it.copy(isLoading = true, configLoadFailed = false) }
            }

            getGroupExpenseConfigUseCase(groupId, forceRefresh)
                .onSuccess { config ->
                    val mappedCurrencies = mapper.mapCurrencies(config.availableCurrencies)
                    val mappedGroupCurrency = mapper.mapCurrency(config.groupCurrency)
                    val deductedAmountLabel = mapper.buildDeductedAmountLabel(mappedGroupCurrency)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isConfigLoaded = true,
                            configLoadFailed = false,
                            loadedGroupId = groupId,
                            groupName = config.group.name,
                            groupCurrency = mappedGroupCurrency,
                            availableCurrencies = mappedCurrencies,
                            selectedCurrency = mappedGroupCurrency,
                            showExchangeRateSection = false,
                            deductedAmountLabel = deductedAmountLabel,
                            error = null
                        )
                    }
                }
                .onFailure { e ->
                    Timber.e(e, "Failed to load group config for withdrawal")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isConfigLoaded = false,
                            configLoadFailed = true,
                            error = UiText.StringResource(R.string.withdrawal_error_load_config)
                        )
                    }
                }
        }
    }

    private fun handleCurrencySelected(currencyCode: String) {
        val currentState = _uiState.value
        val selectedUiModel = currentState.availableCurrencies
            .find { it.code == currencyCode } ?: return
        val isForeign = selectedUiModel.code != currentState.groupCurrency?.code

        val exchangeRateLabel = if (isForeign && currentState.groupCurrency != null) {
            mapper.buildExchangeRateLabel(currentState.groupCurrency, selectedUiModel)
        } else ""

        _uiState.update {
            it.copy(
                selectedCurrency = selectedUiModel,
                showExchangeRateSection = isForeign,
                exchangeRateLabel = exchangeRateLabel
            )
        }

        if (isForeign) {
            fetchRate()
        } else {
            _uiState.update { it.copy(displayExchangeRate = "1.0", deductedAmount = "") }
        }
        recalculateDeducted()
    }

    /**
     * Forward calculation: from withdrawal amount + exchange rate → deducted amount.
     * Uses: sourceAmount / displayRate = groupAmount (deducted from pocket).
     */
    private fun recalculateDeducted() {
        val state = _uiState.value
        if (!state.showExchangeRateSection) return

        val sourceDecimalPlaces = state.selectedCurrency?.decimalDigits ?: 2
        val targetDecimalPlaces = state.groupCurrency?.decimalDigits ?: 2
        val calculatedDeducted = expenseCalculatorService.calculateGroupAmountFromDisplayRate(
            sourceAmountString = state.withdrawalAmount,
            displayRateString = state.displayExchangeRate,
            sourceDecimalPlaces = sourceDecimalPlaces,
            targetDecimalPlaces = targetDecimalPlaces
        )
        val formatted = mapper.formatForDisplay(
            internalValue = calculatedDeducted,
            maxDecimalPlaces = targetDecimalPlaces,
            minDecimalPlaces = targetDecimalPlaces
        )
        _uiState.update { it.copy(deductedAmount = formatted) }
    }

    /**
     * Reverse calculation: from withdrawal amount + deducted amount → implied exchange rate.
     */
    private fun recalculateRateFromDeducted() {
        val state = _uiState.value
        if (!state.showExchangeRateSection) return

        val sourceDecimalPlaces = state.selectedCurrency?.decimalDigits ?: 2
        val impliedRate = expenseCalculatorService.calculateImpliedDisplayRateFromStrings(
            sourceAmountString = state.withdrawalAmount,
            groupAmountString = state.deductedAmount,
            sourceDecimalPlaces = sourceDecimalPlaces
        )
        val formatted = mapper.formatRateForDisplay(impliedRate)
        _uiState.update { it.copy(displayExchangeRate = formatted) }
    }

    private fun fetchRate() {
        val state = _uiState.value
        val groupCurrency = state.groupCurrency
        val selectedCurrency = state.selectedCurrency

        if (groupCurrency == null || selectedCurrency == null ||
            groupCurrency.code == selectedCurrency.code
        ) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingRate = true) }
            try {
                val rate = getExchangeRateUseCase(
                    baseCurrencyCode = groupCurrency.code,
                    targetCurrencyCode = selectedCurrency.code
                )
                _uiState.update {
                    it.copy(
                        isLoadingRate = false,
                        displayExchangeRate = rate?.let { r ->
                            mapper.formatRateForDisplay(r.toPlainString())
                        } ?: it.displayExchangeRate
                    )
                }
                if (rate != null) recalculateDeducted()
            } catch (e: Exception) {
                Timber.w(e, "Failed to fetch exchange rate")
                _uiState.update { it.copy(isLoadingRate = false) }
            }
        }
    }

    private fun submitWithdrawal(groupId: String?, onSuccess: () -> Unit) {
        if (groupId == null) return
        val state = _uiState.value
        val selectedCurrency = state.selectedCurrency ?: return
        val groupCurrency = state.groupCurrency ?: return

        val amountWithdrawn = mapper.parseAmountToSmallestUnit(
            state.withdrawalAmount, selectedCurrency.code
        )

        // Validate
        val amountValidation = cashWithdrawalValidationService.validateAmountWithdrawn(amountWithdrawn)
        if (amountValidation is CashWithdrawalValidationService.ValidationResult.Invalid) {
            _uiState.update { it.copy(isAmountValid = false) }
            return
        }

        // For same currency, deducted = withdrawn. For foreign, parse user input.
        val deductedBaseAmount = if (state.showExchangeRateSection) {
            mapper.parseAmountToSmallestUnit(state.deductedAmount, groupCurrency.code)
        } else {
            amountWithdrawn
        }

        val exchangeRate = if (deductedBaseAmount > 0 && state.showExchangeRateSection) {
            amountWithdrawn.toDouble() / deductedBaseAmount.toDouble()
        } else {
            1.0
        }

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val withdrawal = CashWithdrawal(
                    groupId = groupId,
                    amountWithdrawn = amountWithdrawn,
                    remainingAmount = amountWithdrawn,
                    currency = selectedCurrency.code,
                    deductedBaseAmount = deductedBaseAmount,
                    exchangeRate = exchangeRate
                )
                val result = addCashWithdrawalUseCase(groupId, withdrawal)
                result.getOrThrow()
                onSuccess()
            } catch (e: Exception) {
                Timber.e(e, "Failed to add cash withdrawal")
                _uiState.update { it.copy(isLoading = false) }
                _actions.emit(
                    AddCashWithdrawalUiAction.ShowError(
                        UiText.StringResource(R.string.balances_withdraw_cash_error)
                    )
                )
            }
        }
    }
}


