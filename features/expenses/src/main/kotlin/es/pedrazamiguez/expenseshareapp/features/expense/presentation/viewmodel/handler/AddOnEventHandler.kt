package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler

import es.pedrazamiguez.expenseshareapp.domain.converter.CurrencyConverter
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnMode
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnType
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnValueType
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseCalculatorService
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper.AddExpenseUiMapper
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.AddOnUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.action.AddExpenseUiAction
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

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
    private val addExpenseUiMapper: AddExpenseUiMapper
) : AddExpenseEventHandler {

    private lateinit var _uiState: MutableStateFlow<AddExpenseUiState>
    private lateinit var _actions: MutableSharedFlow<AddExpenseUiAction>
    private lateinit var scope: CoroutineScope

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
        val newAddOn = AddOnUiModel(
            id = UUID.randomUUID().toString(),
            type = type,
            mode = if (type == AddOnType.DISCOUNT) AddOnMode.ON_TOP else AddOnMode.ON_TOP,
            currency = state.selectedCurrency,
            paymentMethod = state.selectedPaymentMethod
        )
        _uiState.update {
            it.copy(
                addOns = (it.addOns + newAddOn).toImmutableList(),
                isAddOnsSectionExpanded = true,
                addOnError = null
            )
        }
    }

    fun handleAddOnRemoved(addOnId: String) {
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
    }

    fun handleCurrencySelected(addOnId: String, currencyCode: String) {
        val currency = _uiState.value.availableCurrencies
            .find { it.code == currencyCode } ?: return
        updateAddOn(addOnId) { it.copy(currency = currency) }
        val state = _uiState.value
        state.addOns.find { it.id == addOnId }?.let { addOn ->
            updateAddOn(addOnId) { resolveAddOnAmounts(addOn, state) }
        }
        recalculateEffectiveTotal()
    }

    fun handlePaymentMethodSelected(addOnId: String, methodId: String) {
        val method = _uiState.value.paymentMethods
            .find { it.id == methodId } ?: return
        updateAddOn(addOnId) { it.copy(paymentMethod = method) }
    }

    fun handleDescriptionChanged(addOnId: String, description: String) {
        updateAddOn(addOnId) { it.copy(description = description) }
    }

    fun handleSectionToggled() {
        _uiState.update {
            it.copy(isAddOnsSectionExpanded = !it.isAddOnsSectionExpanded)
        }
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
     * Then converts to group currency using the add-on's exchange rate.
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
        val inputBd = normalizedInput.toBigDecimalOrNull() ?: return addOn

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

        // Convert to group currency
        val groupAmountCents = convertToGroupCurrency(
            resolvedCents,
            addOn,
            state
        )

        return addOn.copy(
            resolvedAmountCents = resolvedCents,
            groupAmountCents = groupAmountCents
        )
    }

    companion object {

        /**
         * Converts add-on cents to group currency cents.
         * If the add-on currency matches the group currency, no conversion is needed.
         * Otherwise, uses the expense's display exchange rate as an approximation.
         */
        fun convertToGroupCurrency(
            amountCents: Long,
            addOn: AddOnUiModel,
            state: AddExpenseUiState
        ): Long {
            val addOnCurrencyCode = addOn.currency?.code ?: return amountCents
            val groupCurrencyCode = state.groupCurrency?.code ?: return amountCents

            if (addOnCurrencyCode == groupCurrencyCode) return amountCents

            val normalizedRate = CurrencyConverter.normalizeAmountString(
                state.displayExchangeRate.trim()
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
