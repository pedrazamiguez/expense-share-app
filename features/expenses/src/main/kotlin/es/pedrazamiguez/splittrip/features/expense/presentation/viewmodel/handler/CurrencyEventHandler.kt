package es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.handler

import es.pedrazamiguez.splittrip.core.common.presentation.UiText
import es.pedrazamiguez.splittrip.core.designsystem.presentation.formatter.FormattingHelper
import es.pedrazamiguez.splittrip.domain.enums.PayerType
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
import kotlinx.collections.immutable.persistentListOf
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
        val isCash = isCashPaymentMethod()
        if (isForeign) {
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
            // Same currency + CASH: still fetch tranche preview (shown on AmountStep)
            if (isCash) fetchCashRate()
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
     * - CASH + foreign + GROUP pocket: saves current rate, locks fields, fetches ATM preview.
     * - CASH + same currency + GROUP pocket: no lock needed, fetches tranche preview only.
     * - CASH + foreign + USER/SUBUNIT pocket: rate stays unlocked (user enters manually).
     * - non-CASH or leaving CASH: cancels jobs, unlocks rate, restores pre-CASH rate if available.
     *
     * @param isCash true when the selected payment method is CASH.
     */
    fun handlePaymentMethodChanged(isCash: Boolean, isGroupPocket: Boolean = true) {
        val state = _uiState.value
        val isForeign = state.selectedCurrency?.code != state.groupCurrency?.code
        val wasCashLocked = state.isExchangeRateLocked

        if (isCash && isForeign) {
            if (isGroupPocket) {
                // GROUP pocket CASH: save current rate, lock, fetch ATM preview
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
            }
            // else USER/SUBUNIT cash: rate stays unlocked, user enters manually, no fetch
        } else if (isCash && !isForeign && isGroupPocket) {
            // Same-currency CASH: no rate to lock, but still fetch tranche preview
            fetchCashRate()
        } else {
            // Cancel any in-flight or debounced CASH rate jobs so a stale result
            // cannot re-lock the exchange rate after the user has switched away.
            cancelPendingCashJobs()

            _uiState.update {
                it.copy(
                    isExchangeRateLocked = false,
                    isInsufficientCash = false,
                    exchangeRateLockedHint = null,
                    cashTranchePreviews = persistentListOf()
                )
            }
            if (isForeign && wasCashLocked) {
                // Transitioning OUT of locked CASH rate — because the user switched payment
                // method away from CASH.  Restore the rate the user had before the lock
                // was applied.
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
     * Reacts to the funding source changing between GROUP, USER, and SUBUNIT.
     * CASH + foreign + GROUP: locks the rate and fetches from the ATM pool.
     * CASH + foreign + USER/SUBUNIT: cancels the fetch, unlocks, restores pre-CASH rate.
     * Non-CASH or same currency: no effect on the exchange rate.
     */
    fun handleFundingSourceChanged(isGroupPocket: Boolean) {
        val state = _uiState.value
        val isCash = isCashPaymentMethod()
        val isForeign = state.selectedCurrency?.code != state.groupCurrency?.code

        if (!isCash || !isForeign) return // no rate effect for non-CASH or same currency

        if (isGroupPocket) {
            // Switching to GROUP: lock rate and fetch from ATM pool
            if (!state.isExchangeRateLocked) {
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
            }
            fetchCashRate()
        } else {
            // Switching to USER/SUBUNIT: cancel ATM jobs, unlock rate, restore saved rate
            cancelPendingCashJobs()
            val savedRate = state.preCashExchangeRate
            _uiState.update {
                it.copy(
                    isExchangeRateLocked = false,
                    isInsufficientCash = false,
                    exchangeRateLockedHint = null,
                    displayExchangeRate = savedRate ?: it.displayExchangeRate,
                    preCashExchangeRate = null,
                    cashTranchePreviews = persistentListOf()
                )
            }
            if (savedRate != null) {
                recalculateForward()
            }
        }
    }

    /**
     * Fetches the blended ATM exchange rate preview for the current source currency and amount.
     * Shows a weighted-average when no amount is entered yet.
     * Cancels any previous in-flight request and applies a stale-result guard.
     */
    fun fetchCashRate() {
        val state = _uiState.value
        val groupId = state.loadedGroupId ?: return
        val sourceCurrency = state.selectedCurrency?.code ?: return
        val groupCurrency = state.groupCurrency?.code ?: return

        val isSameCurrency = sourceCurrency == groupCurrency
        val sourceDecimalDigits = state.selectedCurrency.decimalDigits
        val targetDecimalDigits = state.groupCurrency.decimalDigits

        // Parse current source amount to cents (0 if blank/invalid)
        val sourceAmountCents = splitPreviewService.parseAmountToCents(
            state.sourceAmount,
            sourceDecimalDigits
        )

        // Resolve scope context from the current funding source selection
        val payerType = currentPayerType()
        val payerId = currentPayerId()

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
                    sourceAmountCents = sourceAmountCents,
                    payerType = payerType,
                    payerId = payerId
                )

                _uiState.update { current ->
                    // Stale-result check: ignore if the user changed group or currency
                    // while the request was in-flight.
                    if (current.loadedGroupId != requestedGroupId ||
                        current.selectedCurrency?.code != requestedSourceCurrency
                    ) {
                        return@update current.copy(isLoadingRate = false)
                    }

                    mapCashRateResult(current, result, targetDecimalDigits, requestedSourceCurrency, !isSameCurrency)
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
        targetDecimalDigits: Int,
        sourceCurrencyCode: String = "",
        updateRateFields: Boolean = true
    ): AddExpenseUiState = when (result) {
        is CashRatePreviewResult.Available -> {
            val preview = result.preview
            val tranchePreviews = if (preview.tranches.isNotEmpty() && sourceCurrencyCode.isNotBlank()) {
                addExpenseOptionsMapper.mapCashTranchePreviews(preview.tranches, sourceCurrencyCode)
            } else {
                persistentListOf()
            }

            // Same-currency CASH: only update tranche preview and clear insufficient flag.
            if (!updateRateFields) {
                return current.copy(
                    isLoadingRate = false,
                    isInsufficientCash = false,
                    cashTranchePreviews = tranchePreviews
                )
            }

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
                    ),
                    cashTranchePreviews = tranchePreviews
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
                    ),
                    cashTranchePreviews = persistentListOf()
                )
            }
        }

        is CashRatePreviewResult.InsufficientCash -> {
            current.copy(
                isLoadingRate = false,
                displayExchangeRate = if (updateRateFields) {
                    EMPTY_FIELD_PLACEHOLDER
                } else {
                    current.displayExchangeRate
                },
                calculatedGroupAmount = if (updateRateFields) {
                    EMPTY_FIELD_PLACEHOLDER
                } else {
                    current.calculatedGroupAmount
                },
                isExchangeRateLocked = updateRateFields || current.isExchangeRateLocked,
                isInsufficientCash = true,
                exchangeRateLockedHint = if (updateRateFields) {
                    UiText.StringResource(R.string.add_expense_cash_insufficient_hint)
                } else {
                    current.exchangeRateLockedHint
                },
                cashTranchePreviews = persistentListOf()
            )
        }

        is CashRatePreviewResult.NoWithdrawals -> {
            current.copy(
                isLoadingRate = false,
                displayExchangeRate = if (updateRateFields) {
                    EMPTY_FIELD_PLACEHOLDER
                } else {
                    current.displayExchangeRate
                },
                calculatedGroupAmount = if (updateRateFields) {
                    EMPTY_FIELD_PLACEHOLDER
                } else {
                    current.calculatedGroupAmount
                },
                isExchangeRateLocked = updateRateFields,
                isInsufficientCash = false,
                exchangeRateLockedHint = if (updateRateFields) {
                    UiText.StringResource(R.string.add_expense_cash_rate_locked_hint)
                } else {
                    current.exchangeRateLockedHint
                },
                cashTranchePreviews = persistentListOf()
            )
        }
    }

    /** Debounced CASH rate recalculation — avoids hitting Room on every keystroke. */
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
     * Returns the [PayerType] derived from the currently selected funding source.
     * Defaults to [PayerType.GROUP] when no funding source is selected.
     */
    internal fun currentPayerType(): PayerType {
        val sourceId = _uiState.value.selectedFundingSource?.id ?: return PayerType.GROUP
        return try {
            PayerType.fromString(sourceId)
        } catch (_: IllegalArgumentException) {
            PayerType.GROUP
        }
    }

    /**
     * Returns the payer ID relevant to the current funding source scope:
     * - **USER:** the current user's ID ([AddExpenseUiState.currentUserId]).
     * - **SUBUNIT:** not yet available from state (returns null; SUBUNIT pool support
     *   is tracked in the companion issue for SUBUNIT funding-source selection).
     * - **GROUP:** always null (GROUP pool needs no owner filter).
     */
    internal fun currentPayerId(): String? = when (currentPayerType()) {
        PayerType.USER -> _uiState.value.currentUserId
        PayerType.GROUP, PayerType.SUBUNIT -> null
    }
}
