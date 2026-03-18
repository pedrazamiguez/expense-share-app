package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler

import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.domain.model.CashRatePreviewResult
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseCalculatorService
import es.pedrazamiguez.expenseshareapp.domain.usecase.currency.GetExchangeRateUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.PreviewCashExchangeRateUseCase
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper.AddExpenseUiMapper
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.action.AddExpenseUiAction
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Handles currency selection and exchange rate events:
 * [CurrencySelected], [ExchangeRateChanged], [GroupAmountChanged].
 *
 * Also exposes [fetchRate] and [recalculateForward] for cross-handler calls
 * (e.g., from [ConfigEventHandler] after initial config load).
 */
class CurrencyEventHandler(
    private val getExchangeRateUseCase: GetExchangeRateUseCase,
    private val previewCashExchangeRateUseCase: PreviewCashExchangeRateUseCase,
    private val expenseCalculatorService: ExpenseCalculatorService,
    private val addExpenseUiMapper: AddExpenseUiMapper
) : AddExpenseEventHandler {

    private lateinit var _uiState: MutableStateFlow<AddExpenseUiState>
    private lateinit var _actions: MutableSharedFlow<AddExpenseUiAction>
    private lateinit var scope: CoroutineScope

    /** Debounce job for cash rate preview recalculations on amount changes. */
    private var cashPreviewJob: Job? = null

    /** Tracked coroutine for in-flight cash rate fetch (prevents stale/duplicate results). */
    private var cashRateJob: Job? = null

    companion object {
        private const val CASH_PREVIEW_DEBOUNCE_MS = 300L
    }

    override fun bind(
        stateFlow: MutableStateFlow<AddExpenseUiState>,
        actionsFlow: MutableSharedFlow<AddExpenseUiAction>,
        scope: CoroutineScope
    ) {
        _uiState = stateFlow
        _actions = actionsFlow
        this.scope = scope
    }

    fun handleCurrencySelected(currencyCode: String, onRecalculate: () -> Unit) {
        val currentState = _uiState.value
        val selectedUiModel = currentState.availableCurrencies
            .find { it.code == currencyCode } ?: return
        val isForeign = selectedUiModel.code != currentState.groupCurrency?.code

        val exchangeRateLabel = if (isForeign && currentState.groupCurrency != null) {
            addExpenseUiMapper.buildExchangeRateLabel(currentState.groupCurrency, selectedUiModel)
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
        // If switching to foreign, fetch the appropriate rate; otherwise default to 1.0
        if (isForeign) {
            val isCash = isCashPaymentMethod()
            if (isCash) {
                // Lock immediately so the fields are non-editable from the start,
                // before the async fetchCashRate completes.
                _uiState.update {
                    it.copy(
                        isExchangeRateLocked = true,
                        isInsufficientCash = false,
                        exchangeRateLockedHint = UiText.StringResource(
                            R.string.add_expense_cash_rate_locked_hint
                        )
                    )
                }
                fetchCashRate()
            } else {
                fetchRate()
            }
        } else {
            _uiState.update {
                it.copy(
                    displayExchangeRate = "1.0",
                    isExchangeRateLocked = false,
                    isInsufficientCash = false,
                    exchangeRateLockedHint = null
                )
            }
        }
        recalculateForward()
        onRecalculate()
    }

    fun handleExchangeRateChanged(rate: String) {
        _uiState.update { it.copy(displayExchangeRate = rate) }
        recalculateForward()
    }

    fun handleGroupAmountChanged(amount: String) {
        _uiState.update { it.copy(calculatedGroupAmount = amount) }
        recalculateReverse()
    }

    /**
     * Calculates the group amount from source amount and display exchange rate.
     * Uses the user-friendly display rate (1 GroupCurrency = X SourceCurrency).
     * All BigDecimal operations are delegated to ExpenseCalculatorService.
     */
    fun recalculateForward() {
        val state = _uiState.value
        val sourceDecimalPlaces = state.selectedCurrency?.decimalDigits ?: 2
        val targetDecimalPlaces = state.groupCurrency?.decimalDigits ?: 2
        val calculatedAmount = expenseCalculatorService.calculateGroupAmountFromDisplayRate(
            sourceAmountString = state.sourceAmount,
            displayRateString = state.displayExchangeRate,
            sourceDecimalPlaces = sourceDecimalPlaces,
            targetDecimalPlaces = targetDecimalPlaces
        )
        // Format the amount for display using locale-aware formatting
        // Use currency's decimal digits as minimum to ensure proper display (e.g., "1,10" for EUR instead of "1,1")
        val formattedAmount = addExpenseUiMapper.formatForDisplay(
            internalValue = calculatedAmount,
            maxDecimalPlaces = targetDecimalPlaces,
            minDecimalPlaces = targetDecimalPlaces
        )
        _uiState.update { it.copy(calculatedGroupAmount = formattedAmount) }
    }

    /**
     * Calculates the implied display exchange rate from source and group amounts.
     * Returns the rate in user-friendly format (1 GroupCurrency = X SourceCurrency).
     * All BigDecimal operations are delegated to ExpenseCalculatorService.
     */
    private fun recalculateReverse() {
        val state = _uiState.value
        val sourceDecimalPlaces = state.selectedCurrency?.decimalDigits ?: 2
        val impliedDisplayRate = expenseCalculatorService.calculateImpliedDisplayRateFromStrings(
            sourceAmountString = state.sourceAmount,
            groupAmountString = state.calculatedGroupAmount,
            sourceDecimalPlaces = sourceDecimalPlaces
        )
        // Format the rate for display using locale-aware formatting
        val formattedRate = addExpenseUiMapper.formatRateForDisplay(impliedDisplayRate)
        _uiState.update { it.copy(displayExchangeRate = formattedRate) }
    }

    fun fetchRate() {
        val state = _uiState.value
        val groupCurrency = state.groupCurrency
        val selectedCurrency = state.selectedCurrency

        if (groupCurrency == null || selectedCurrency == null || groupCurrency.code == selectedCurrency.code) {
            return
        }

        // Capture the requested pair so we can verify it before applying the result
        val requestedBaseCode = groupCurrency.code
        val requestedTargetCode = selectedCurrency.code

        scope.launch {
            _uiState.update { it.copy(isLoadingRate = true) }

            try {
                val rate = getExchangeRateUseCase(
                    baseCurrencyCode = requestedBaseCode,
                    targetCurrencyCode = requestedTargetCode
                )

                _uiState.update { current ->
                    // If the user changed currencies while the request was in-flight,
                    // don't overwrite state for the new selection with a stale result.
                    if (current.groupCurrency?.code != requestedBaseCode ||
                        current.selectedCurrency?.code != requestedTargetCode
                    ) {
                        current.copy(isLoadingRate = false)
                    } else {
                        current.copy(
                            isLoadingRate = false,
                            // If rate found, update display; otherwise keep existing/default
                            displayExchangeRate = rate?.let { exchangeRate ->
                                addExpenseUiMapper.formatRateForDisplay(exchangeRate.toPlainString())
                            } ?: current.displayExchangeRate
                        )
                    }
                }

                if (rate != null) {
                    recalculateForward()
                }
            } catch (e: Exception) {
                Timber.e(
                    e,
                    "Failed to fetch exchange rate for $requestedBaseCode -> $requestedTargetCode"
                )
                _uiState.update { it.copy(isLoadingRate = false) }
            }
        }
    }

    /**
     * Reacts to the payment method changing between CASH and non-CASH.
     *
     * When switching TO CASH + foreign currency:
     * - Locks the exchange rate fields (not user-editable)
     * - Shows a hint explaining the rate source
     * - Computes a preview blended rate from available ATM withdrawals
     *
     * When switching FROM CASH to non-CASH + foreign currency:
     * - Unlocks the exchange rate fields
     * - Fetches the API rate as usual
     */
    fun handlePaymentMethodChanged(isCash: Boolean) {
        val state = _uiState.value
        val isForeign = state.selectedCurrency?.code != state.groupCurrency?.code

        if (isCash && isForeign) {
            _uiState.update {
                it.copy(
                    isExchangeRateLocked = true,
                    isInsufficientCash = false,
                    exchangeRateLockedHint = UiText.StringResource(
                        R.string.add_expense_cash_rate_locked_hint
                    )
                )
            }
            fetchCashRate()
        } else {
            _uiState.update {
                it.copy(
                    isExchangeRateLocked = false,
                    isInsufficientCash = false,
                    exchangeRateLockedHint = null
                )
            }
            if (!isCash && isForeign) {
                fetchRate()
            }
        }
    }

    /**
     * Fetches the blended exchange rate from ATM withdrawals for the current
     * source currency and amount. Updates the display rate and group amount.
     *
     * If no amount is entered yet, a weighted-average preview rate is shown.
     *
     * Cancels any previous in-flight cash rate request and verifies the result
     * is still relevant (same groupId + currency) before applying state changes,
     * analogous to [fetchRate]'s stale-result protection.
     */
    fun fetchCashRate() {
        val state = _uiState.value
        val groupId = state.loadedGroupId ?: return
        val sourceCurrency = state.selectedCurrency?.code ?: return
        val groupCurrency = state.groupCurrency?.code ?: return
        if (sourceCurrency == groupCurrency) return

        val sourceDecimalDigits = state.selectedCurrency.decimalDigits
        val targetDecimalDigits = state.groupCurrency.decimalDigits

        // Parse current source amount to cents (0 if blank/invalid)
        val sourceAmountCents = parseSourceAmountToCents(state.sourceAmount, sourceDecimalDigits)

        // Capture request context for stale-result check
        val requestedGroupId = groupId
        val requestedSourceCurrency = sourceCurrency

        // Cancel any previous in-flight cash rate request
        cashRateJob?.cancel()
        cashRateJob = scope.launch {
            _uiState.update { it.copy(isLoadingRate = true) }
            try {
                val result = previewCashExchangeRateUseCase(
                    groupId = requestedGroupId,
                    sourceCurrency = requestedSourceCurrency,
                    sourceAmountCents = sourceAmountCents
                )

                _uiState.update { current ->
                    // Stale-result check: ignore if the user changed group or currency
                    // while the request was in-flight.
                    if (current.loadedGroupId != requestedGroupId ||
                        current.selectedCurrency?.code != requestedSourceCurrency
                    ) {
                        return@update current.copy(isLoadingRate = false)
                    }

                    when (result) {
                        is CashRatePreviewResult.Available -> {
                            val preview = result.preview
                            val formattedRate = addExpenseUiMapper.formatRateForDisplay(
                                preview.displayRate.toPlainString()
                            )

                            if (preview.groupAmountCents > 0) {
                                // FIFO-simulated: update both rate and group amount
                                val groupAmountBd = expenseCalculatorService.centsToBigDecimal(
                                    preview.groupAmountCents,
                                    targetDecimalDigits
                                )
                                val formattedAmount = addExpenseUiMapper.formatForDisplay(
                                    internalValue = groupAmountBd.toPlainString(),
                                    maxDecimalPlaces = targetDecimalDigits,
                                    minDecimalPlaces = targetDecimalDigits
                                )
                                current.copy(
                                    isLoadingRate = false,
                                    displayExchangeRate = formattedRate,
                                    calculatedGroupAmount = formattedAmount,
                                    isExchangeRateLocked = true,
                                    isInsufficientCash = false,
                                    exchangeRateLockedHint = UiText.StringResource(
                                        R.string.add_expense_cash_rate_locked_hint
                                    )
                                )
                            } else {
                                // Weighted-average preview (no amount entered yet)
                                current.copy(
                                    isLoadingRate = false,
                                    displayExchangeRate = formattedRate,
                                    isExchangeRateLocked = true,
                                    isInsufficientCash = false,
                                    exchangeRateLockedHint = UiText.StringResource(
                                        R.string.add_expense_cash_rate_locked_hint
                                    )
                                )
                            }
                        }

                        is CashRatePreviewResult.InsufficientCash -> {
                            // Amount exceeds available cash — show warning hint
                            current.copy(
                                isLoadingRate = false,
                                displayExchangeRate = "",
                                calculatedGroupAmount = "",
                                isExchangeRateLocked = true,
                                isInsufficientCash = true,
                                exchangeRateLockedHint = UiText.StringResource(
                                    R.string.add_expense_cash_insufficient_hint
                                )
                            )
                        }

                        is CashRatePreviewResult.NoWithdrawals -> {
                            // No withdrawals — clear rate/amount with generic hint
                            current.copy(
                                isLoadingRate = false,
                                displayExchangeRate = "",
                                calculatedGroupAmount = "",
                                isExchangeRateLocked = true,
                                isInsufficientCash = false,
                                exchangeRateLockedHint = UiText.StringResource(
                                    R.string.add_expense_cash_rate_locked_hint
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to preview cash exchange rate")
                _uiState.update { it.copy(isLoadingRate = false) }
            }
        }
    }

    /**
     * Debounced recalculation for CASH expenses when the source amount changes.
     * Calls [fetchCashRate] after a short delay to avoid hitting Room on every keystroke.
     */
    fun recalculateCashForward() {
        cashPreviewJob?.cancel()
        cashPreviewJob = scope.launch {
            delay(CASH_PREVIEW_DEBOUNCE_MS)
            fetchCashRate()
        }
    }

    /**
     * Returns true if the currently selected payment method is CASH.
     */
    private fun isCashPaymentMethod(): Boolean {
        val methodId = _uiState.value.selectedPaymentMethod?.id ?: return false
        return try {
            es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod.fromString(methodId) ==
                es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod.CASH
        } catch (_: IllegalArgumentException) {
            false
        }
    }

    /**
     * Parses a locale-aware amount string to cents using the currency's decimal places.
     * Returns 0 if the input is blank or unparseable.
     */
    private fun parseSourceAmountToCents(amountString: String, decimalPlaces: Int): Long {
        val trimmed = amountString.trim()
        if (trimmed.isBlank()) return 0L
        val normalized = es.pedrazamiguez.expenseshareapp.domain.converter.CurrencyConverter
            .normalizeAmountString(trimmed)
        val bd = normalized.toBigDecimalOrNull() ?: return 0L
        val multiplier = java.math.BigDecimal.TEN.pow(decimalPlaces)
        return bd.multiply(multiplier).setScale(0, java.math.RoundingMode.HALF_UP).toLong()
    }
}
