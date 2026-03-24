package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler

import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.domain.converter.CurrencyConverter
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnMode
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnType
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnValueType
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentStatus
import es.pedrazamiguez.expenseshareapp.domain.exception.InsufficientCashException
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.domain.model.ExpenseSplit
import es.pedrazamiguez.expenseshareapp.domain.model.ValidationResult
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseCalculatorService
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseValidationService
import es.pedrazamiguez.expenseshareapp.domain.usecase.expense.AddExpenseUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.SetGroupLastUsedCategoryUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.SetGroupLastUsedCurrencyUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.SetGroupLastUsedPaymentMethodUseCase
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper.AddExpenseUiMapper
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.AddOnUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.action.AddExpenseUiAction
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState
import java.math.BigDecimal
import java.math.RoundingMode
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Handles expense form submission.
 *
 * Validates the form, maps to a domain object, applies INCLUDED add-on base-cost
 * decomposition via [ExpenseCalculatorService], and delegates to the use case.
 */
class SubmitEventHandler(
    private val addExpenseUseCase: AddExpenseUseCase,
    private val expenseValidationService: ExpenseValidationService,
    private val expenseCalculatorService: ExpenseCalculatorService,
    private val setGroupLastUsedCurrencyUseCase: SetGroupLastUsedCurrencyUseCase,
    private val setGroupLastUsedPaymentMethodUseCase: SetGroupLastUsedPaymentMethodUseCase,
    private val setGroupLastUsedCategoryUseCase: SetGroupLastUsedCategoryUseCase,
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

    fun submitExpense(groupId: String?, onSuccess: () -> Unit) {
        if (groupId == null) return

        val currentState = _uiState.value

        // Validate title using domain service
        val titleValidation = expenseValidationService.validateTitle(currentState.expenseTitle)
        if (titleValidation is ValidationResult.Invalid) {
            _uiState.update {
                it.copy(
                    isTitleValid = false,
                    error = UiText.StringResource(R.string.expense_error_title_empty)
                )
            }
            return
        }

        // Validate amount using domain service
        val amountValidation = expenseValidationService.validateAmount(currentState.sourceAmount)
        if (amountValidation is ValidationResult.Invalid) {
            _uiState.update {
                it.copy(
                    isAmountValid = false,
                    error = UiText.DynamicString(amountValidation.message)
                )
            }
            return
        }

        // Validate due date when payment status is SCHEDULED
        if (currentState.selectedPaymentStatus?.id == PaymentStatus.SCHEDULED.name &&
            currentState.dueDateMillis == null
        ) {
            _uiState.update {
                it.copy(
                    isDueDateValid = false,
                    error = UiText.StringResource(R.string.expense_error_due_date_required)
                )
            }
            return
        }

        // Validate add-ons (only those with non-empty input)
        val addOnsWithInput = currentState.addOns.filter { it.amountInput.isNotBlank() }
        if (addOnsWithInput.any { it.resolvedAmountCents <= 0 }) {
            _uiState.update {
                it.copy(
                    addOnError = UiText.StringResource(
                        R.string.add_expense_add_on_error_amount
                    )
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        addExpenseUiMapper.mapToDomain(_uiState.value, groupId).onSuccess { expense ->
            // Apply INCLUDED add-on base-cost decomposition: when the user enters a total
            // that already includes a tip/fee, extract the base cost so we store the
            // pre-tip amount in groupAmount (and source amount) and keep the tip in addOns.
            val adjustedExpense = adjustForIncludedAddOns(expense, _uiState.value.addOns)
            scope.launch {
                addExpenseUseCase(groupId, adjustedExpense).onSuccess {
                    // Save the user's selections specific to this group
                    _uiState.value.selectedCurrency?.code?.let { code ->
                        runCatching {
                            setGroupLastUsedCurrencyUseCase(groupId, code)
                        }
                    }
                    _uiState.value.selectedPaymentMethod?.id?.let { id ->
                        runCatching {
                            setGroupLastUsedPaymentMethodUseCase(groupId, id)
                        }
                    }
                    _uiState.value.selectedCategory?.id?.let { id ->
                        runCatching {
                            setGroupLastUsedCategoryUseCase(groupId, id)
                        }
                    }
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }.onFailure { e ->
                    // Clear loading and ensure no stale inline error is visible;
                    // the snackbar is the correct surface for submission errors.
                    _uiState.update { it.copy(isLoading = false, error = null) }

                    when (e) {
                        is InsufficientCashException -> {
                            // Use the cash currency (the currency the user actually paid in),
                            // NOT the group currency — the cent values come from the source amount.
                            val cashCurrency = currentState.selectedCurrency
                            if (cashCurrency != null) {
                                val required = addExpenseUiMapper.formatCentsForDisplay(
                                    e.requiredCents,
                                    cashCurrency
                                )
                                val available = addExpenseUiMapper.formatCentsForDisplay(
                                    e.availableCents,
                                    cashCurrency
                                )
                                _actions.emit(
                                    AddExpenseUiAction.ShowError(
                                        UiText.StringResource(
                                            R.string.expense_error_insufficient_cash,
                                            required,
                                            available
                                        )
                                    )
                                )
                            } else {
                                _actions.emit(
                                    AddExpenseUiAction.ShowError(
                                        UiText.StringResource(R.string.expense_error_addition_failed)
                                    )
                                )
                            }
                        }

                        else -> {
                            _actions.emit(
                                AddExpenseUiAction.ShowError(
                                    UiText.StringResource(R.string.expense_error_addition_failed)
                                )
                            )
                        }
                    }
                }
            }
        }.onFailure { e ->
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = UiText.DynamicString(e.message ?: "Unknown error")
                )
            }
        }
    }

    // ── INCLUDED Add-On Base Cost Extraction ──────────────────────────────

    /**
     * When INCLUDED non-discount add-ons are present, adjusts the mapped [expense]
     * to store the extracted **base cost** instead of the full user-entered total.
     *
     * - Computes the base cost for group and source amounts using
     *   [ExpenseCalculatorService.calculateIncludedBaseCost].
     * - Recomputes PERCENTAGE INCLUDED add-on amounts against the base cost.
     * - Rescales splits proportionally to the new base source amount.
     *
     * When no INCLUDED add-ons exist, returns the expense unchanged.
     *
     * This belongs here (not in the mapper) because it applies business-rule
     * transformation that depends on [ExpenseCalculatorService] — a domain service —
     * which must not be injected into a Mapper.
     */
    internal fun adjustForIncludedAddOns(
        expense: Expense,
        uiAddOns: ImmutableList<AddOnUiModel>
    ): Expense {
        val includedNonDiscount = expense.addOns.filter {
            it.mode == AddOnMode.INCLUDED && it.type != AddOnType.DISCOUNT
        }
        if (includedNonDiscount.isEmpty()) return expense

        val fullSourceAmount = expense.sourceAmount
        val fullGroupAmount = expense.groupAmount

        // Separate EXACT and PERCENTAGE INCLUDED add-ons for base cost calculation
        val includedExactGroupCents = includedNonDiscount
            .filter { it.valueType == AddOnValueType.EXACT }
            .sumOf { it.groupAmountCents }

        val totalIncludedPercentage = uiAddOns
            .filter {
                it.mode == AddOnMode.INCLUDED &&
                    it.type != AddOnType.DISCOUNT &&
                    it.valueType == AddOnValueType.PERCENTAGE &&
                    it.resolvedAmountCents > 0
            }
            .fold(BigDecimal.ZERO) { acc, uiModel ->
                val normalized = CurrencyConverter.normalizeAmountString(uiModel.amountInput.trim())
                acc.add(normalized.toBigDecimalOrNull() ?: BigDecimal.ZERO)
            }

        // Compute base cost in group currency
        val baseCostGroup = expenseCalculatorService.calculateIncludedBaseCost(
            totalAmountCents = fullGroupAmount,
            includedExactCents = includedExactGroupCents,
            totalIncludedPercentage = totalIncludedPercentage
        )

        // Compute base cost in source currency proportionally
        val baseCostSource = if (fullSourceAmount == fullGroupAmount || fullGroupAmount == 0L) {
            baseCostGroup
        } else {
            BigDecimal(fullSourceAmount)
                .multiply(BigDecimal(baseCostGroup))
                .divide(BigDecimal(fullGroupAmount), 0, RoundingMode.HALF_UP)
                .toLong()
        }

        // Recompute PERCENTAGE INCLUDED add-on amounts based on base cost
        val adjustedAddOns = expense.addOns.map { addOn ->
            if (addOn.mode != AddOnMode.INCLUDED ||
                addOn.type == AddOnType.DISCOUNT ||
                addOn.valueType != AddOnValueType.PERCENTAGE
            ) {
                return@map addOn
            }

            val uiModel = uiAddOns.find { it.id == addOn.id } ?: return@map addOn
            val normalized = CurrencyConverter.normalizeAmountString(uiModel.amountInput.trim())
            val percentage = normalized.toBigDecimalOrNull() ?: return@map addOn

            val newGroupAmountCents = BigDecimal(baseCostGroup)
                .multiply(percentage)
                .divide(BigDecimal("100"), 0, RoundingMode.HALF_UP)
                .toLong()

            val newAmountCents = if (addOn.exchangeRate.compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal(newGroupAmountCents)
                    .divide(addOn.exchangeRate, 0, RoundingMode.HALF_UP)
                    .toLong()
            } else {
                newGroupAmountCents
            }

            addOn.copy(amountCents = newAmountCents, groupAmountCents = newGroupAmountCents)
        }

        // Rescale splits proportionally from the full amount to the base cost
        val adjustedSplits = rescaleSplits(expense.splits, fullSourceAmount, baseCostSource)

        return expense.copy(
            sourceAmount = baseCostSource,
            groupAmount = baseCostGroup,
            addOns = adjustedAddOns,
            splits = adjustedSplits
        )
    }

    /**
     * Rescales split amounts proportionally from [originalTotal] to [newTotal].
     *
     * Uses floor rounding for each split, then distributes the remainder
     * (one unit at a time) to the first splits to ensure the sum equals
     * [newTotal] exactly (conservation of currency).
     */
    private fun rescaleSplits(
        splits: List<ExpenseSplit>,
        originalTotal: Long,
        newTotal: Long
    ): List<ExpenseSplit> {
        if (originalTotal == newTotal || originalTotal <= 0 || splits.isEmpty()) return splits

        val ratio = BigDecimal(newTotal).divide(BigDecimal(originalTotal), RATE_PRECISION, RoundingMode.DOWN)

        val scaled = splits.map { split ->
            val newAmount = BigDecimal(split.amountCents)
                .multiply(ratio)
                .setScale(0, RoundingMode.DOWN)
                .toLong()
            split.copy(amountCents = newAmount)
        }

        val allocatedTotal = scaled.sumOf { it.amountCents }
        var remainder = newTotal - allocatedTotal

        return scaled.map { split ->
            if (remainder > 0 && !split.isExcluded) {
                remainder--
                split.copy(amountCents = split.amountCents + 1)
            } else {
                split
            }
        }
    }

    companion object {
        private const val RATE_PRECISION = 6
    }
}
