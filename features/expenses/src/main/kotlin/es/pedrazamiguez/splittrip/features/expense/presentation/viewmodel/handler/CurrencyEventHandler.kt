package es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.handler

import es.pedrazamiguez.splittrip.core.common.presentation.UiText
import es.pedrazamiguez.splittrip.core.designsystem.presentation.formatter.FormattingHelper
import es.pedrazamiguez.splittrip.domain.model.CashRatePreviewResult
import es.pedrazamiguez.splittrip.domain.service.ExchangeRateCalculationService
import es.pedrazamiguez.splittrip.domain.service.ExpenseCalculatorService
import es.pedrazamiguez.splittrip.domain.service.split.SplitPreviewService
import es.pedrazamiguez.splittrip.domain.usecase.currency.GetExchangeRateUseCase
import es.pedrazamiguez.splittrip.domain.usecase.expense.PreviewCashExchangeRateUseCase
import es.pedrazamiguez.splittrip.features.expense.R
import es.pedrazamiguez.splittrip.features.expense.presentation.mapper.AddExpenseOptionsUiMapper
import es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.action.AddExpenseUiAction
import es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.state.AddExpenseUiState
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
// Function count driven by event/action categories (CASH, non-CASH, funding source);
// extracting further would require a Delegate sub-pattern
@Suppress("TooManyFunctions")
class CurrencyEventHandler(
    private val getExchangeRateUseCase: GetExchangeRateUseCase,
    private val previewCashExchangeRateUseCase: PreviewCashExchangeRateUseCase,
    private val exchangeRateCalculationService: ExchangeRateCalculationService,
    private val expenseCalculatorService: ExpenseCalculatorService,
    private val splitPreviewService: SplitPreviewService,
    private val formattingHelper: FormattingHelper,
    private val addExpenseOptionsMapper: AddExpenseOptionsUiMapper
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

        /**
         * Placeholder shown in locked exchange-rate fields when no value is available
         * (e.g. insufficient cash, no withdrawals). Keeps the OutlinedTextField label
         * floating above the field instead of collapsing into the field body.
         */
        private const val EMPTY_FIELD_PLACEHOLDER = "—"
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
            addExpenseOptionsMapper.buildExchangeRateLabel(currentState.groupCurrency, selectedUiModel)
        } else {
            ""
        }

        _uiState.update {
            it.copy(
                selectedCurrency = selectedUiModel,
                showExchangeRateSection = isForeign,
                exchangeRateLabel = exchangeRateLabel,
                // Clear the saved pre-CASH rate — it belongs to the previous currency pair
                preCashExchangeRate = null
            ).withStepClamped()
        }
        // If switching to foreign, fetch the appropriate rate; otherwise default to 1.0
        if (isForeign) {
            val isCash = isCashPaymentMethod()
            val isGroupPocket = isGroupFundingSource()
            if (isCash && isGroupPocket) {
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
        val calculatedAmount = exchangeRateCalculationService.calculateGroupAmountFromDisplayRate(
            sourceAmountString = state.sourceAmount,
            displayRateString = state.displayExchangeRate,
            sourceDecimalPlaces = sourceDecimalPlaces,
            targetDecimalPlaces = targetDecimalPlaces
        )
        // Format the amount for display using locale-aware formatting
        // Use currency's decimal digits as minimum to ensure proper display (e.g., "1,10" for EUR instead of "1,1")
        val formattedAmount = formattingHelper.formatForDisplay(
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
        val impliedDisplayRate = exchangeRateCalculationService.calculateImpliedDisplayRateFromStrings(
            sourceAmountString = state.sourceAmount,
            groupAmountString = state.calculatedGroupAmount,
            sourceDecimalPlaces = sourceDecimalPlaces
        )
        // Format the rate for display using locale-aware formatting
        val formattedRate = formattingHelper.formatRateForDisplay(impliedDisplayRate)
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
                val rateResult = getExchangeRateUseCase(
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
                            displayExchangeRate = rateResult?.rate?.let { exchangeRate ->
                                formattingHelper.formatRateForDisplay(exchangeRate.toPlainString())
                            } ?: current.displayExchangeRate,
                            isExchangeRateStale = rateResult?.isStale
                                ?: current.isExchangeRateStale
                        )
                    }
                }

                if (rateResult != null) {
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
     * When switching TO CASH + foreign currency + GROUP pocket:
     * - Saves the current display exchange rate so it can be restored later
     * - Locks the exchange rate fields (not user-editable)
     * - Shows a hint explaining the rate source
     * - Computes a preview blended rate from available ATM withdrawals
     *
     * When switching FROM CASH to non-CASH + foreign currency:
     * - Unlocks the exchange rate fields
     * - Restores the previously saved exchange rate (if available)
     * - Falls back to fetching the API rate only when no saved rate exists
     *   (e.g. currency was changed while on CASH)
     *
     * When switching between non-CASH methods + foreign currency:
     * - Does nothing with the exchange rate — the user's custom rate is preserved
     *
     * @param isCash true when the selected payment method is CASH
     * @param isGroupPocket true when the funding source is GROUP (default)
     */
    fun handlePaymentMethodChanged(isCash: Boolean, isGroupPocket: Boolean = true) {
        val state = _uiState.value
        val isForeign = state.selectedCurrency?.code != state.groupCurrency?.code
        val wasCashLocked = state.isExchangeRateLocked

        if (isCash && isForeign && isGroupPocket) {
            // Save the current rate before locking so it can be restored later
            _uiState.update {
                it.copy(
                    preCashExchangeRate = it.displayExchangeRate,
                    isExchangeRateLocked = true,
                    isInsufficientCash = false,
                    exchangeRateLockedHint = UiText.StringResource(
                        R.string.add_expense_cash_rate_locked_hint
                    )
                )
            }
            fetchCashRate()
        } else {
            // Cancel any in-flight or debounced CASH rate jobs so a stale result
            // cannot re-lock the exchange rate after the user has switched away.
            cancelPendingCashJobs()

            _uiState.update {
                it.copy(
                    isExchangeRateLocked = false,
                    isInsufficientCash = false,
                    exchangeRateLockedHint = null
                )
            }
            if (isForeign && wasCashLocked) {
                // Transitioning OUT of locked CASH rate — either because the user
                // switched payment method away from CASH, or changed funding source
                // from GROUP to USER while staying on CASH.  Restore the rate the
                // user had before the lock was applied.
                val savedRate = state.preCashExchangeRate
                if (savedRate != null) {
                    _uiState.update {
                        it.copy(
                            displayExchangeRate = savedRate,
                            preCashExchangeRate = null
                        )
                    }
                    recalculateForward()
                } else {
                    // No saved rate (e.g. currency changed while on CASH) — fetch fresh
                    fetchRate()
                }
            }
            // Switching between non-CASH methods or same-currency: do nothing with the rate
        }
    }

    /**
     * Reacts to the funding source changing between GROUP and USER.
     *
     * When CASH + foreign currency:
     * - GROUP → USER: unlock rate (user pays from own money, manual rate entry)
     * - USER → GROUP: lock rate (fetch blended rate from ATM withdrawals)
     *
     * When non-CASH or same currency:
     * - No effect on exchange rate.
     */
    fun handleFundingSourceChanged(isGroupPocket: Boolean) {
        val isCash = isCashPaymentMethod()
        handlePaymentMethodChanged(isCash, isGroupPocket)
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
        val sourceAmountCents = splitPreviewService.parseAmountToCents(
            state.sourceAmount,
            sourceDecimalDigits
        )

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

                    mapCashRateResult(current, result, targetDecimalDigits)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to preview cash exchange rate")
                _uiState.update { it.copy(isLoadingRate = false) }
            }
        }
    }

    /**
     * Maps a [CashRatePreviewResult] into an updated [AddExpenseUiState].
     *
     * Extracted from [fetchCashRate] to reduce method length and complexity.
     */
    // Sealed when-expression maps each result variant to a state copy;
    // length is driven by the number of fields per branch, not logic
    @Suppress("LongMethod")
    internal fun mapCashRateResult(
        current: AddExpenseUiState,
        result: CashRatePreviewResult,
        targetDecimalDigits: Int
    ): AddExpenseUiState = when (result) {
        is CashRatePreviewResult.Available -> {
            val preview = result.preview
            val formattedRate = formattingHelper.formatRateForDisplay(
                preview.displayRate.toPlainString()
            )

            if (preview.groupAmountCents > 0) {
                // FIFO-simulated: update both rate and group amount
                val groupAmountStr = expenseCalculatorService.centsToBigDecimalString(
                    preview.groupAmountCents,
                    targetDecimalDigits
                )
                val formattedAmount = formattingHelper.formatForDisplay(
                    internalValue = groupAmountStr,
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
                // Weighted-average preview (no amount entered yet).
                current.copy(
                    isLoadingRate = false,
                    displayExchangeRate = formattedRate,
                    calculatedGroupAmount = "",
                    isExchangeRateLocked = true,
                    isInsufficientCash = false,
                    exchangeRateLockedHint = UiText.StringResource(
                        R.string.add_expense_cash_rate_locked_hint
                    )
                )
            }
        }

        is CashRatePreviewResult.InsufficientCash -> {
            current.copy(
                isLoadingRate = false,
                displayExchangeRate = EMPTY_FIELD_PLACEHOLDER,
                calculatedGroupAmount = EMPTY_FIELD_PLACEHOLDER,
                isExchangeRateLocked = true,
                isInsufficientCash = true,
                exchangeRateLockedHint = UiText.StringResource(
                    R.string.add_expense_cash_insufficient_hint
                )
            )
        }

        is CashRatePreviewResult.NoWithdrawals -> {
            current.copy(
                isLoadingRate = false,
                displayExchangeRate = EMPTY_FIELD_PLACEHOLDER,
                calculatedGroupAmount = EMPTY_FIELD_PLACEHOLDER,
                isExchangeRateLocked = true,
                isInsufficientCash = false,
                exchangeRateLockedHint = UiText.StringResource(
                    R.string.add_expense_cash_rate_locked_hint
                )
            )
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
     * Cancels any in-flight or debounced CASH rate jobs.
     * Called when leaving CASH payment to prevent a stale result from
     * re-locking the exchange rate after the user has switched away.
     */
    private fun cancelPendingCashJobs() {
        cashRateJob?.cancel()
        cashRateJob = null
        cashPreviewJob?.cancel()
        cashPreviewJob = null
    }

    /**
     * Returns true if the currently selected payment method is CASH.
     */
    private fun isCashPaymentMethod(): Boolean {
        val methodId = _uiState.value.selectedPaymentMethod?.id ?: return false
        return try {
            es.pedrazamiguez.splittrip.domain.enums.PaymentMethod.fromString(methodId) ==
                es.pedrazamiguez.splittrip.domain.enums.PaymentMethod.CASH
        } catch (_: IllegalArgumentException) {
            false
        }
    }

    /**
     * Returns true if the currently selected funding source is GROUP (Group Pocket).
     * Defaults to true when no funding source is selected (GROUP is the default).
     */
    private fun isGroupFundingSource(): Boolean {
        val sourceId = _uiState.value.selectedFundingSource?.id ?: return true
        return try {
            es.pedrazamiguez.splittrip.domain.enums.PayerType.fromString(sourceId) ==
                es.pedrazamiguez.splittrip.domain.enums.PayerType.GROUP
        } catch (_: IllegalArgumentException) {
            true
        }
    }
}
