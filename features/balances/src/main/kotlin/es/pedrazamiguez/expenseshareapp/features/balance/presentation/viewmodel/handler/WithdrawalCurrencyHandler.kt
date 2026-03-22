package es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.handler

import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseCalculatorService
import es.pedrazamiguez.expenseshareapp.domain.usecase.currency.GetExchangeRateUseCase
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.mapper.AddCashWithdrawalUiMapper
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.action.AddCashWithdrawalUiAction
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.AddCashWithdrawalUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Handles currency selection and exchange rate events:
 * [CurrencySelected], [WithdrawalAmountChanged], [ExchangeRateChanged], [DeductedAmountChanged].
 *
 * Also exposes [recalculateDeducted] for cross-handler calls.
 */
class WithdrawalCurrencyHandler(
    private val getExchangeRateUseCase: GetExchangeRateUseCase,
    private val expenseCalculatorService: ExpenseCalculatorService,
    private val mapper: AddCashWithdrawalUiMapper
) : AddCashWithdrawalEventHandler {

    private lateinit var _uiState: MutableStateFlow<AddCashWithdrawalUiState>
    private lateinit var _actions: MutableSharedFlow<AddCashWithdrawalUiAction>
    private lateinit var scope: CoroutineScope

    override fun bind(
        stateFlow: MutableStateFlow<AddCashWithdrawalUiState>,
        actionsFlow: MutableSharedFlow<AddCashWithdrawalUiAction>,
        scope: CoroutineScope
    ) {
        _uiState = stateFlow
        _actions = actionsFlow
        this.scope = scope
    }

    fun handleCurrencySelected(currencyCode: String) {
        val currentState = _uiState.value
        val selectedUiModel = currentState.availableCurrencies
            .find { it.code == currencyCode } ?: return
        val isForeign = selectedUiModel.code != currentState.groupCurrency?.code

        val exchangeRateLabel = if (isForeign && currentState.groupCurrency != null) {
            mapper.buildExchangeRateLabel(currentState.groupCurrency, selectedUiModel)
        } else {
            ""
        }

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

    fun handleWithdrawalAmountChanged(amount: String) {
        _uiState.update {
            it.copy(withdrawalAmount = amount, isAmountValid = true, error = null)
        }
        recalculateDeducted()
    }

    fun handleExchangeRateChanged(rate: String) {
        _uiState.update { it.copy(displayExchangeRate = rate) }
        recalculateDeducted()
    }

    fun handleDeductedAmountChanged(amount: String) {
        _uiState.update { it.copy(deductedAmount = amount) }
        recalculateRateFromDeducted()
    }

    /**
     * Forward calculation: from withdrawal amount + exchange rate → deducted amount.
     */
    fun recalculateDeducted() {
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

        if (groupCurrency == null ||
            selectedCurrency == null ||
            groupCurrency.code == selectedCurrency.code
        ) {
            return
        }

        scope.launch {
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
}
