package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler

import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.domain.converter.CurrencyConverter
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnMode
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnType
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnValueType
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentMethod
import es.pedrazamiguez.expenseshareapp.domain.model.CashRatePreviewResult
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseCalculatorService
import es.pedrazamiguez.expenseshareapp.domain.usecase.currency.GetExchangeRateUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.PreviewCashExchangeRateUseCase
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper.AddExpenseUiMapper
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.AddOnUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.action.AddExpenseUiAction
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Handles add-on CRUD and recalculation events within the Add Expense form.
 *
 * Manages the `addOns` list in [AddExpenseUiState], resolves amounts
 * (including percentage → cents), and recalculates the effective total.
 *
 * Exposes [recalculateEffectiveTotal] for cross-handler calls
 * (e.g., when source amount or currency changes).
 */
class AddOnEventHandler(
    private val expenseCalculatorService: ExpenseCalculatorService,
    private val addExpenseUiMapper: AddExpenseUiMapper,
    private val getExchangeRateUseCase: GetExchangeRateUseCase,
    private val previewCashExchangeRateUseCase: PreviewCashExchangeRateUseCase
) : AddExpenseEventHandler {

    private lateinit var _uiState: MutableStateFlow<AddExpenseUiState>
    private lateinit var _actions: MutableSharedFlow<AddExpenseUiAction>
    private lateinit var scope: CoroutineScope

    /** Tracked rate-fetch jobs per add-on to prevent stale results. */
    private val rateFetchJobs = ConcurrentHashMap<String, Job>()

    /** Tracked cash rate-fetch jobs per add-on to prevent stale/duplicate results. */
    private val cashRateFetchJobs = ConcurrentHashMap<String, Job>()

    /** Tracked cash rate debounce jobs per add-on (amount changes while CASH). */
    private val cashPreviewJobs = ConcurrentHashMap<String, Job>()

    override fun bind(
        stateFlow: MutableStateFlow<AddExpenseUiState>,
        actionsFlow: MutableSharedFlow<AddExpenseUiAction>,
        scope: CoroutineScope
    ) {
        _uiState = stateFlow
        _actions = actionsFlow
        this.scope = scope
    }

    // ── CRUD Operations ─────────────────────────────────────────────────

    fun handleAddOnAdded(type: AddOnType) {
        val state = _uiState.value
        val groupCurrency = state.groupCurrency
        val addOnCurrency = state.selectedCurrency
        val isForeign = addOnCurrency != null &&
            groupCurrency != null &&
            addOnCurrency.code != groupCurrency.code

        val exchangeRateLabel = if (isForeign) {
            addExpenseUiMapper.buildExchangeRateLabel(groupCurrency, addOnCurrency)
        } else {
            ""
        }

        val groupAmountLabel = if (isForeign) {
            addExpenseUiMapper.buildGroupAmountLabel(groupCurrency)
        } else {
            ""
        }

        val isCash = isCashMethod(state.selectedPaymentMethod?.id)
        val shouldLockRate = isForeign && isCash

        val newAddOn = AddOnUiModel(
            id = UUID.randomUUID().toString(),
            type = type,
            mode = if (type == AddOnType.DISCOUNT) AddOnMode.ON_TOP else AddOnMode.ON_TOP,
            currency = addOnCurrency,
            paymentMethod = state.selectedPaymentMethod,
            showExchangeRateSection = isForeign,
            exchangeRateLabel = exchangeRateLabel,
            groupAmountLabel = groupAmountLabel,
            // When foreign and same currency as expense, inherit the expense-level rate
            displayExchangeRate = if (isForeign) {
                state.displayExchangeRate
            } else {
                "1.0"
            },
            isExchangeRateLocked = shouldLockRate,
            isInsufficientCash = false,
            exchangeRateLockedHint = if (shouldLockRate) {
                UiText.StringResource(R.string.add_expense_cash_rate_locked_hint)
            } else {
                null
            }
        )
        _uiState.update {
            it.copy(
                addOns = (it.addOns + newAddOn).toImmutableList(),
                isAddOnsSectionExpanded = true,
                addOnError = null
            )
        }

        // Auto-fetch rate if foreign currency
        if (isForeign) {
            if (isCash) {
                fetchAddOnCashRate(newAddOn.id)
            } else {
                fetchAddOnRate(newAddOn.id, groupCurrency.code, addOnCurrency.code)
            }
        }
    }

    fun handleAddOnRemoved(addOnId: String) {
        rateFetchJobs.remove(addOnId)?.cancel()
        cancelPendingCashJobs(addOnId)
        _uiState.update {
            it.copy(
                addOns = it.addOns.filter { a -> a.id != addOnId }.toImmutableList(),
                addOnError = null
            )
        }
        recalculateEffectiveTotal()
    }

    fun handleTypeChanged(addOnId: String, type: AddOnType) {
        updateAddOn(addOnId) { it.copy(type = type) }
    }

    fun handleModeChanged(addOnId: String, mode: AddOnMode) {
        updateAddOn(addOnId) { it.copy(mode = mode) }
        recalculateEffectiveTotal()
    }

    fun handleValueTypeChanged(addOnId: String, valueType: AddOnValueType) {
        updateAddOn(addOnId) { it.copy(valueType = valueType, amountInput = "") }
        val state = _uiState.value
        state.addOns.find { it.id == addOnId }?.let { addOn ->
            updateAddOn(addOnId) { resolveAddOnAmounts(addOn, state) }
        }
        recalculateEffectiveTotal()
    }

    fun handleAmountChanged(addOnId: String, amount: String) {
        updateAddOn(addOnId) {
            it.copy(amountInput = amount, isAmountValid = true)
        }
        _uiState.update { it.copy(addOnError = null) }
        val state = _uiState.value
        state.addOns.find { it.id == addOnId }?.let { addOn ->
            updateAddOn(addOnId) { resolveAddOnAmounts(addOn, state) }
        }
        recalculateEffectiveTotal()

        // Re-fetch CASH rate when amount changes (FIFO may use different tranches)
        val updatedAddOn = _uiState.value.addOns.find { it.id == addOnId }
        if (updatedAddOn?.isExchangeRateLocked == true) {
            recalculateCashForward(addOnId)
        }
    }

    fun handleCurrencySelected(addOnId: String, currencyCode: String) {
        val state = _uiState.value
        val currency = state.availableCurrencies
            .find { it.code == currencyCode } ?: return
        val groupCurrency = state.groupCurrency ?: return
        val isForeign = currency.code != groupCurrency.code
        val currentAddOn = state.addOns.find { it.id == addOnId } ?: return
        val isCash = isCashMethod(currentAddOn.paymentMethod?.id)

        val exchangeRateLabel = if (isForeign) {
            addExpenseUiMapper.buildExchangeRateLabel(groupCurrency, currency)
        } else {
            ""
        }

        val groupAmountLabel = if (isForeign) {
            addExpenseUiMapper.buildGroupAmountLabel(groupCurrency)
        } else {
            ""
        }

        val shouldLockRate = isForeign && isCash

        updateAddOn(addOnId) {
            it.copy(
                currency = currency,
                showExchangeRateSection = isForeign,
                exchangeRateLabel = exchangeRateLabel,
                groupAmountLabel = groupAmountLabel,
                displayExchangeRate = if (isForeign) it.displayExchangeRate else "1.0",
                calculatedGroupAmount = if (isForeign) it.calculatedGroupAmount else "",
                isExchangeRateLocked = shouldLockRate,
                isInsufficientCash = false,
                exchangeRateLockedHint = if (shouldLockRate) {
                    UiText.StringResource(R.string.add_expense_cash_rate_locked_hint)
                } else {
                    null
                },
                // Clear pre-cash rate when currency changes while on CASH
                preCashExchangeRate = if (isCash) null else it.preCashExchangeRate
            )
        }

        // Re-resolve amounts with the new currency
        val updatedState = _uiState.value
        updatedState.addOns.find { it.id == addOnId }?.let { addOn ->
            updateAddOn(addOnId) { resolveAddOnAmounts(addOn, updatedState) }
        }
        recalculateEffectiveTotal()

        // Auto-fetch rate if foreign
        if (isForeign) {
            if (isCash) {
                fetchAddOnCashRate(addOnId)
            } else {
                fetchAddOnRate(addOnId, groupCurrency.code, currency.code)
            }
        }
    }

    /**
     * Reacts to the payment method changing on a specific add-on.
     *
     * When switching TO CASH + foreign currency:
     * - Saves the current display exchange rate so it can be restored later
     * - Locks the exchange rate fields (not user-editable)
     * - Shows a hint explaining the rate source
     * - Computes a preview blended rate from available ATM withdrawals
     *
     * When switching FROM CASH to non-CASH + foreign currency:
     * - Unlocks the exchange rate fields
     * - Restores the previously saved exchange rate (if available)
     * - Falls back to fetching the API rate only when no saved rate exists
     *
     * When switching between non-CASH methods + foreign currency:
     * - Does nothing with the exchange rate — the user's custom rate is preserved
     */
    fun handlePaymentMethodSelected(addOnId: String, methodId: String) {
        val method = _uiState.value.paymentMethods
            .find { it.id == methodId } ?: return

        val state = _uiState.value
        val addOn = state.addOns.find { it.id == addOnId } ?: return
        val groupCurrency = state.groupCurrency
        val isForeign = addOn.currency != null &&
            groupCurrency != null &&
            addOn.currency.code != groupCurrency.code
        val isCash = isCashMethod(method.id)
        val wasCashLocked = addOn.isExchangeRateLocked

        updateAddOn(addOnId) { it.copy(paymentMethod = method) }

        if (isCash && isForeign) {
            // Save the current rate before locking so it can be restored later
            updateAddOn(addOnId) {
                it.copy(
                    preCashExchangeRate = it.displayExchangeRate,
                    isExchangeRateLocked = true,
                    isInsufficientCash = false,
                    exchangeRateLockedHint = UiText.StringResource(
                        R.string.add_expense_cash_rate_locked_hint
                    )
                )
            }
            fetchAddOnCashRate(addOnId)
        } else if (!isCash && isForeign && wasCashLocked) {
            // Switching FROM CASH: cancel pending jobs and unlock
            cancelPendingCashJobs(addOnId)
            updateAddOn(addOnId) {
                it.copy(
                    isExchangeRateLocked = false,
                    isInsufficientCash = false,
                    exchangeRateLockedHint = null
                )
            }

            // Restore the rate the user had before CASH
            val savedRate = addOn.preCashExchangeRate
            if (savedRate != null) {
                updateAddOn(addOnId) {
                    it.copy(
                        displayExchangeRate = savedRate,
                        preCashExchangeRate = null
                    )
                }
                recalculateAddOnForward(addOnId)
                recalculateEffectiveTotal()
            } else {
                // No saved rate (e.g. currency changed while on CASH) — fetch fresh
                val groupCode = groupCurrency.code
                val addOnCode = addOn.currency.code
                fetchAddOnRate(addOnId, groupCode, addOnCode)
            }
        } else if (!isCash) {
            // Switching between non-CASH methods while locked (shouldn't happen, safety)
            if (wasCashLocked) {
                cancelPendingCashJobs(addOnId)
                updateAddOn(addOnId) {
                    it.copy(
                        isExchangeRateLocked = false,
                        isInsufficientCash = false,
                        exchangeRateLockedHint = null
                    )
                }
            }
            // Do nothing with the rate for non-CASH → non-CASH
        }
    }

    fun handleDescriptionChanged(addOnId: String, description: String) {
        updateAddOn(addOnId) { it.copy(description = description) }
    }

    fun handleSectionToggled() {
        _uiState.update {
            it.copy(isAddOnsSectionExpanded = !it.isAddOnsSectionExpanded)
        }
    }

    // ── Per-add-on Exchange Rate Events ──────────────────────────────────

    fun handleExchangeRateChanged(addOnId: String, rate: String) {
        updateAddOn(addOnId) { it.copy(displayExchangeRate = rate) }
        recalculateAddOnForward(addOnId)
        recalculateEffectiveTotal()
    }

    fun handleGroupAmountChanged(addOnId: String, amount: String) {
        updateAddOn(addOnId) { it.copy(calculatedGroupAmount = amount) }
        recalculateAddOnReverse(addOnId)
        recalculateEffectiveTotal()
    }

    // ── Cross-handler: Recalculate ──────────────────────────────────────

    /**
     * Recalculates the effective total and updates the display.
     * Called by other handlers when source amount or currency changes.
     */
    fun recalculateEffectiveTotal() {
        val state = _uiState.value
        val groupCurrency = state.groupCurrency ?: return

        // Re-resolve all add-ons with current source amount
        val resolvedAddOns = state.addOns.map { addOn ->
            resolveAddOnAmounts(addOn, state)
        }.toImmutableList()

        // Parse source amount to group amount cents for effective total
        val groupAmountCents = parseGroupAmountCents(state)

        // Build domain-level add-on list for calculation
        val domainAddOns = resolvedAddOns.map { uiModel ->
            es.pedrazamiguez.expenseshareapp.domain.model.AddOn(
                groupAmountCents = uiModel.groupAmountCents
                    .takeIf { uiModel.mode != AddOnMode.INCLUDED } ?: 0L,
                mode = uiModel.mode,
                type = uiModel.type
            )
        }

        val effectiveCents = expenseCalculatorService.calculateEffectiveGroupAmount(
            groupAmountCents,
            domainAddOns
        )

        val effectiveDisplay = if (effectiveCents != groupAmountCents && effectiveCents > 0) {
            addExpenseUiMapper.formatCentsForDisplay(effectiveCents, groupCurrency)
        } else {
            ""
        }

        _uiState.update {
            it.copy(
                addOns = resolvedAddOns,
                effectiveTotal = effectiveDisplay
            )
        }
    }

    // ── Private Helpers ─────────────────────────────────────────────────

    private fun updateAddOn(
        addOnId: String,
        transform: (AddOnUiModel) -> AddOnUiModel
    ) {
        _uiState.update { state ->
            state.copy(
                addOns = state.addOns.map { addOn ->
                    if (addOn.id == addOnId) transform(addOn) else addOn
                }.toImmutableList()
            )
        }
    }

    /**
     * Resolves an add-on's amount input into [AddOnUiModel.resolvedAmountCents]
     * and [AddOnUiModel.groupAmountCents].
     *
     * For PERCENTAGE value type, computes the percentage of the source amount.
     * For EXACT, parses the input directly.
     * Then converts to group currency using the add-on's own exchange rate.
     *
     * Also called from [recalculateEffectiveTotal] for batch re-resolution.
     */
    private fun resolveAddOnAmounts(addOn: AddOnUiModel, state: AddExpenseUiState): AddOnUiModel {
        if (addOn.amountInput.isBlank()) {
            return addOn.copy(resolvedAmountCents = 0L, groupAmountCents = 0L)
        }

        val decimalDigits = addOn.currency?.decimalDigits ?: 2
        val normalizedInput = CurrencyConverter.normalizeAmountString(
            addOn.amountInput.trim()
        )
        val inputBd = normalizedInput.toBigDecimalOrNull()
            ?: return addOn.copy(
                resolvedAmountCents = 0L,
                groupAmountCents = 0L,
                isAmountValid = false
            )

        val resolvedCents: Long = when (addOn.valueType) {
            AddOnValueType.EXACT -> {
                val multiplier = BigDecimal.TEN.pow(decimalDigits)
                inputBd.multiply(multiplier)
                    .setScale(0, RoundingMode.HALF_UP)
                    .toLong()
            }
            AddOnValueType.PERCENTAGE -> {
                // Percentage of the source amount
                val sourceDecimalDigits = state.selectedCurrency?.decimalDigits ?: 2
                val sourceAmountCents = parseSourceAmountCents(state, sourceDecimalDigits)
                if (sourceAmountCents <= 0) return addOn
                BigDecimal(sourceAmountCents)
                    .multiply(inputBd)
                    .divide(BigDecimal(100), 0, RoundingMode.HALF_UP)
                    .toLong()
            }
        }

        // Convert to group currency using the add-on's own rate
        val groupAmountCents = convertToGroupCurrency(resolvedCents, addOn)

        // Update the display string for the exchange rate card
        val calculatedGroupAmount = if (addOn.showExchangeRateSection && groupAmountCents > 0) {
            val targetDecimalPlaces = state.groupCurrency?.decimalDigits ?: 2
            val divisor = BigDecimal.TEN.pow(targetDecimalPlaces)
            val displayValue = BigDecimal(groupAmountCents)
                .divide(divisor, targetDecimalPlaces, RoundingMode.HALF_UP)
                .toPlainString()
            addExpenseUiMapper.formatForDisplay(
                internalValue = displayValue,
                maxDecimalPlaces = targetDecimalPlaces,
                minDecimalPlaces = targetDecimalPlaces
            )
        } else {
            addOn.calculatedGroupAmount
        }

        return addOn.copy(
            resolvedAmountCents = resolvedCents,
            groupAmountCents = groupAmountCents,
            calculatedGroupAmount = calculatedGroupAmount
        )
    }

    /**
     * Fetches the exchange rate for an add-on's currency pair and updates
     * the add-on's [AddOnUiModel.displayExchangeRate] when the result arrives.
     *
     * Cancels any in-flight fetch for the same add-on to prevent stale results.
     */
    private fun fetchAddOnRate(addOnId: String, baseCurrencyCode: String, targetCurrencyCode: String) {
        rateFetchJobs[addOnId]?.cancel()
        rateFetchJobs[addOnId] = scope.launch {
            updateAddOn(addOnId) { it.copy(isLoadingRate = true) }
            try {
                val rate = getExchangeRateUseCase(
                    baseCurrencyCode = baseCurrencyCode,
                    targetCurrencyCode = targetCurrencyCode
                )

                updateAddOn(addOnId) { current ->
                    // Verify the add-on still has the same currency pair
                    val state = _uiState.value
                    val addOn = state.addOns.find { it.id == addOnId }
                    if (addOn?.currency?.code != targetCurrencyCode ||
                        state.groupCurrency?.code != baseCurrencyCode
                    ) {
                        current.copy(isLoadingRate = false)
                    } else {
                        val formattedRate = rate?.let {
                            addExpenseUiMapper.formatRateForDisplay(it.toPlainString())
                        } ?: current.displayExchangeRate
                        current.copy(
                            isLoadingRate = false,
                            displayExchangeRate = formattedRate
                        )
                    }
                }

                if (rate != null) {
                    recalculateAddOnForward(addOnId)
                    recalculateEffectiveTotal()
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to fetch add-on rate for $baseCurrencyCode -> $targetCurrencyCode")
                updateAddOn(addOnId) { it.copy(isLoadingRate = false) }
            }
        }
    }

    /**
     * Forward calculation for a single add-on: source amount + display rate → group amount.
     */
    private fun recalculateAddOnForward(addOnId: String) {
        val state = _uiState.value
        val addOn = state.addOns.find { it.id == addOnId } ?: return
        if (!addOn.showExchangeRateSection) return

        val sourceDecimalPlaces = addOn.currency?.decimalDigits ?: 2
        val targetDecimalPlaces = state.groupCurrency?.decimalDigits ?: 2

        val sourceAmountStr = if (addOn.resolvedAmountCents > 0) {
            // Convert cents back to major unit string for calculation
            val divisor = BigDecimal.TEN.pow(sourceDecimalPlaces)
            BigDecimal(addOn.resolvedAmountCents)
                .divide(divisor, sourceDecimalPlaces, RoundingMode.HALF_UP)
                .toPlainString()
        } else {
            addOn.amountInput
        }

        val calculatedAmount = expenseCalculatorService.calculateGroupAmountFromDisplayRate(
            sourceAmountString = sourceAmountStr,
            displayRateString = addOn.displayExchangeRate,
            sourceDecimalPlaces = sourceDecimalPlaces,
            targetDecimalPlaces = targetDecimalPlaces
        )

        val formattedAmount = addExpenseUiMapper.formatForDisplay(
            internalValue = calculatedAmount,
            maxDecimalPlaces = targetDecimalPlaces,
            minDecimalPlaces = targetDecimalPlaces
        )

        updateAddOn(addOnId) { it.copy(calculatedGroupAmount = formattedAmount) }
    }

    /**
     * Reverse calculation for a single add-on: group amount → implied display rate.
     */
    private fun recalculateAddOnReverse(addOnId: String) {
        val state = _uiState.value
        val addOn = state.addOns.find { it.id == addOnId } ?: return
        if (!addOn.showExchangeRateSection) return

        val sourceDecimalPlaces = addOn.currency?.decimalDigits ?: 2

        val sourceAmountStr = if (addOn.resolvedAmountCents > 0) {
            val divisor = BigDecimal.TEN.pow(sourceDecimalPlaces)
            BigDecimal(addOn.resolvedAmountCents)
                .divide(divisor, sourceDecimalPlaces, RoundingMode.HALF_UP)
                .toPlainString()
        } else {
            addOn.amountInput
        }

        val impliedDisplayRate = expenseCalculatorService.calculateImpliedDisplayRateFromStrings(
            sourceAmountString = sourceAmountStr,
            groupAmountString = addOn.calculatedGroupAmount,
            sourceDecimalPlaces = sourceDecimalPlaces
        )

        val formattedRate = addExpenseUiMapper.formatRateForDisplay(impliedDisplayRate)
        updateAddOn(addOnId) { it.copy(displayExchangeRate = formattedRate) }
    }

    // ── CASH Rate Helpers ─────────────────────────────────────────────────

    /**
     * Fetches the blended exchange rate from ATM withdrawals for a specific add-on.
     * Updates the add-on's display rate and group amount.
     *
     * Cancels any previous in-flight cash rate request for this add-on and verifies
     * the result is still relevant before applying state changes.
     */
    private fun fetchAddOnCashRate(addOnId: String) {
        val state = _uiState.value
        val addOn = state.addOns.find { it.id == addOnId } ?: return
        val groupId = state.loadedGroupId ?: return
        val sourceCurrency = addOn.currency?.code ?: return
        val groupCurrency = state.groupCurrency?.code ?: return
        if (sourceCurrency == groupCurrency) return

        val targetDecimalDigits = state.groupCurrency.decimalDigits
        val sourceDecimalDigits = addOn.currency.decimalDigits

        // Parse add-on amount to cents (0 if blank/invalid)
        val sourceAmountCents = parseAddOnAmountToCents(addOn, sourceDecimalDigits)

        // Capture request context for stale-result check
        val requestedGroupId = groupId
        val requestedSourceCurrency = sourceCurrency

        // Cancel any previous in-flight cash rate request for this add-on
        cashRateFetchJobs[addOnId]?.cancel()
        cashRateFetchJobs[addOnId] = scope.launch {
            updateAddOn(addOnId) { it.copy(isLoadingRate = true) }
            try {
                val result = previewCashExchangeRateUseCase(
                    groupId = requestedGroupId,
                    sourceCurrency = requestedSourceCurrency,
                    sourceAmountCents = sourceAmountCents
                )

                updateAddOn(addOnId) { current ->
                    // Stale-result check: ignore if the user changed group or currency
                    val currentState = _uiState.value
                    val currentAddOn = currentState.addOns.find { it.id == addOnId }
                    if (currentState.loadedGroupId != requestedGroupId ||
                        currentAddOn?.currency?.code != requestedSourceCurrency
                    ) {
                        return@updateAddOn current.copy(isLoadingRate = false)
                    }

                    when (result) {
                        is CashRatePreviewResult.Available -> {
                            val preview = result.preview
                            val formattedRate = addExpenseUiMapper.formatRateForDisplay(
                                preview.displayRate.toPlainString()
                            )

                            if (preview.groupAmountCents > 0) {
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
                }
                recalculateEffectiveTotal()
            } catch (e: Exception) {
                Timber.e(e, "Failed to preview cash exchange rate for add-on $addOnId")
                updateAddOn(addOnId) { it.copy(isLoadingRate = false) }
            }
        }
    }

    /**
     * Debounced recalculation for CASH add-ons when the amount changes.
     * Calls [fetchAddOnCashRate] after a short delay to avoid hitting Room on every keystroke.
     */
    private fun recalculateCashForward(addOnId: String) {
        cashPreviewJobs[addOnId]?.cancel()
        cashPreviewJobs[addOnId] = scope.launch {
            delay(CASH_PREVIEW_DEBOUNCE_MS)
            fetchAddOnCashRate(addOnId)
        }
    }

    /**
     * Cancels any in-flight or debounced CASH rate jobs for a specific add-on.
     */
    private fun cancelPendingCashJobs(addOnId: String) {
        cashRateFetchJobs.remove(addOnId)?.cancel()
        cashPreviewJobs.remove(addOnId)?.cancel()
    }

    /**
     * Returns true if the given payment method ID corresponds to CASH.
     */
    private fun isCashMethod(methodId: String?): Boolean {
        if (methodId == null) return false
        return try {
            PaymentMethod.fromString(methodId) == PaymentMethod.CASH
        } catch (_: IllegalArgumentException) {
            false
        }
    }

    /**
     * Parses the add-on's resolved amount or raw input to cents for the cash rate preview.
     * Returns 0 if no valid amount exists.
     */
    private fun parseAddOnAmountToCents(addOn: AddOnUiModel, decimalPlaces: Int): Long {
        if (addOn.resolvedAmountCents > 0) return addOn.resolvedAmountCents
        val input = addOn.amountInput.trim()
        if (input.isBlank()) return 0L
        val normalized = CurrencyConverter.normalizeAmountString(input)
        val bd = normalized.toBigDecimalOrNull() ?: return 0L
        val multiplier = BigDecimal.TEN.pow(decimalPlaces)
        return bd.multiply(multiplier).setScale(0, RoundingMode.HALF_UP).toLong()
    }

    companion object {
        private const val CASH_PREVIEW_DEBOUNCE_MS = 300L

        /**
         * Placeholder shown in locked exchange-rate fields when no value is available
         * (e.g. insufficient cash, no withdrawals). Keeps the OutlinedTextField label
         * floating above the field instead of collapsing into the field body.
         */
        private const val EMPTY_FIELD_PLACEHOLDER = "—"

        /**
         * Converts add-on cents to group currency cents.
         * If the add-on currency matches the group currency, no conversion is needed.
         * Otherwise, uses the add-on's own display exchange rate.
         */
        fun convertToGroupCurrency(
            amountCents: Long,
            addOn: AddOnUiModel
        ): Long {
            if (addOn.currency == null) return amountCents

            // If the add-on has no exchange rate section, it's in group currency already
            if (!addOn.showExchangeRateSection) return amountCents

            val normalizedRate = CurrencyConverter.normalizeAmountString(
                addOn.displayExchangeRate.trim()
            )
            val displayRate = normalizedRate.toBigDecimalOrNull() ?: BigDecimal.ONE
            if (displayRate.compareTo(BigDecimal.ZERO) == 0) return amountCents

            val internalRate = BigDecimal.ONE.divide(displayRate, 6, RoundingMode.HALF_UP)

            return BigDecimal(amountCents)
                .multiply(internalRate)
                .setScale(0, RoundingMode.HALF_UP)
                .toLong()
        }

        fun parseSourceAmountCents(state: AddExpenseUiState, decimalDigits: Int): Long {
            if (state.sourceAmount.isBlank()) return 0L
            val normalized = CurrencyConverter.normalizeAmountString(state.sourceAmount.trim())
            val amount = normalized.toBigDecimalOrNull() ?: return 0L
            val multiplier = BigDecimal.TEN.pow(decimalDigits)
            return amount.multiply(multiplier).setScale(0, RoundingMode.HALF_UP).toLong()
        }

        fun parseGroupAmountCents(state: AddExpenseUiState): Long {
            val groupDecimalDigits = state.groupCurrency?.decimalDigits ?: 2
            val groupAmountStr = state.calculatedGroupAmount.ifBlank { state.sourceAmount }
            if (groupAmountStr.isBlank()) return 0L
            val normalized = CurrencyConverter.normalizeAmountString(groupAmountStr.trim())
            val amount = normalized.toBigDecimalOrNull() ?: return 0L
            val multiplier = BigDecimal.TEN.pow(groupDecimalDigits)
            return amount.multiply(multiplier).setScale(0, RoundingMode.HALF_UP).toLong()
        }
    }
}
