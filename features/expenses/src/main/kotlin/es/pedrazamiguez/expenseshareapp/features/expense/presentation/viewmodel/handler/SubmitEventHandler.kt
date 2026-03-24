package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler

import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.FormattingHelper
import es.pedrazamiguez.expenseshareapp.domain.converter.CurrencyConverter
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnMode
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnType
import es.pedrazamiguez.expenseshareapp.domain.enums.AddOnValueType
import es.pedrazamiguez.expenseshareapp.domain.enums.PaymentStatus
import es.pedrazamiguez.expenseshareapp.domain.exception.InsufficientCashException
import es.pedrazamiguez.expenseshareapp.domain.model.AddOn
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.domain.model.ExpenseSplit
import es.pedrazamiguez.expenseshareapp.domain.model.ValidationResult
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseCalculatorService
import es.pedrazamiguez.expenseshareapp.domain.service.ExpenseValidationService
import es.pedrazamiguez.expenseshareapp.domain.service.RemainderDistributionService
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
    private val remainderDistributionService: RemainderDistributionService,
    private val setGroupLastUsedCurrencyUseCase: SetGroupLastUsedCurrencyUseCase,
    private val setGroupLastUsedPaymentMethodUseCase: SetGroupLastUsedPaymentMethodUseCase,
    private val setGroupLastUsedCategoryUseCase: SetGroupLastUsedCategoryUseCase,
    private val addExpenseUiMapper: AddExpenseUiMapper,
    private val formattingHelper: FormattingHelper
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
                                val required = formattingHelper.formatCentsWithCurrency(
                                    e.requiredCents,
                                    cashCurrency.code
                                )
                                val available = formattingHelper.formatCentsWithCurrency(
                                    e.availableCents,
                                    cashCurrency.code
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
     * - Computes the base cost via [computeBaseCosts].
     * - Redistributes PERCENTAGE INCLUDED add-on amounts via [adjustIncludedPercentageAddOns]
     *   using a residual approach to guarantee `base + includedExact + sum(includedPct) == total`.
     * - Rescales splits proportionally to the new base source amount.
     *
     * When no INCLUDED add-ons exist, returns the expense unchanged.
     */
    internal fun adjustForIncludedAddOns(
        expense: Expense,
        uiAddOns: ImmutableList<AddOnUiModel>
    ): Expense {
        val includedNonDiscount = expense.addOns.filter {
            it.mode == AddOnMode.INCLUDED && it.type != AddOnType.DISCOUNT
        }
        if (includedNonDiscount.isEmpty()) return expense

        val (baseCostGroup, baseCostSource) = computeBaseCosts(expense, uiAddOns)
        val adjustedAddOns = adjustIncludedPercentageAddOns(expense, uiAddOns, baseCostGroup)
        val adjustedSplits = rescaleSplits(expense.splits, expense.sourceAmount, baseCostSource)

        return expense.copy(
            sourceAmount = baseCostSource,
            groupAmount = baseCostGroup,
            addOns = adjustedAddOns,
            splits = adjustedSplits
        )
    }

    /**
     * Derives base costs in both group and source currencies from the user-entered total.
     *
     * Returns `(baseCostGroup, baseCostSource)`.
     */
    private fun computeBaseCosts(
        expense: Expense,
        uiAddOns: ImmutableList<AddOnUiModel>
    ): Pair<Long, Long> {
        val includedNonDiscount = expense.addOns.filter {
            it.mode == AddOnMode.INCLUDED && it.type != AddOnType.DISCOUNT
        }
        val includedExactGroupCents = includedNonDiscount
            .filter { it.valueType == AddOnValueType.EXACT }
            .sumOf { it.groupAmountCents }

        val totalIncludedPercentage = expenseCalculatorService.sumPercentagesFromInputs(
            uiAddOns
                .filter {
                    it.mode == AddOnMode.INCLUDED &&
                        it.type != AddOnType.DISCOUNT &&
                        it.valueType == AddOnValueType.PERCENTAGE &&
                        it.resolvedAmountCents > 0
                }
                .map { it.amountInput }
        )

        val baseCostGroup = expenseCalculatorService.calculateIncludedBaseCost(
            totalAmountCents = expense.groupAmount,
            includedExactCents = includedExactGroupCents,
            totalIncludedPercentage = totalIncludedPercentage
        )
        val baseCostSource = expenseCalculatorService.computeProportionalAmount(
            amount = expense.sourceAmount,
            targetAmount = baseCostGroup,
            totalAmount = expense.groupAmount
        )
        return baseCostGroup to baseCostSource
    }

    /**
     * Recomputes INCLUDED PERCENTAGE add-on amounts using a **residual approach**:
     *
     *   `residual = originalGroupAmount − includedExactCents − baseCostGroup`
     *
     * The residual is distributed proportionally across all INCLUDED PERCENTAGE add-ons
     * (floor rounding + one-cent remainder redistribution). This guarantees that
     * `base + includedExact + sum(includedPct) == originalGroupAmount` exactly, with
     * no rounding drift from independent `base × pct / 100` recomputation.
     */
    private fun adjustIncludedPercentageAddOns(
        expense: Expense,
        uiAddOns: ImmutableList<AddOnUiModel>,
        baseCostGroup: Long
    ): List<AddOn> {
        val includedExactCents = expense.addOns
            .filter {
                it.mode == AddOnMode.INCLUDED &&
                    it.type != AddOnType.DISCOUNT &&
                    it.valueType == AddOnValueType.EXACT
            }
            .sumOf { it.groupAmountCents }
        val percentageResidual = (expense.groupAmount - includedExactCents - baseCostGroup).coerceAtLeast(0L)

        val pctAddOns = expense.addOns.filter {
            it.mode == AddOnMode.INCLUDED && it.type != AddOnType.DISCOUNT && it.valueType == AddOnValueType.PERCENTAGE
        }
        if (pctAddOns.isEmpty()) return expense.addOns

        val weights = pctAddOns.map { addOn ->
            val ui = uiAddOns.find { it.id == addOn.id }
            val inputStr = ui?.amountInput?.trim() ?: ""
            val normalized = CurrencyConverter.normalizeAmountString(inputStr)
            normalized.toBigDecimalOrNull() ?: BigDecimal.ZERO
        }

        val newGroupCents = remainderDistributionService.distributeByWeights(percentageResidual, weights)

        val allocationsById = pctAddOns.mapIndexed { i, addOn -> addOn.id to newGroupCents[i] }.toMap()
        return expense.addOns.map { addOn ->
            val newGroupAmountCents = allocationsById[addOn.id] ?: return@map addOn
            val newAmountCents = expenseCalculatorService.convertGroupToSourceCents(
                groupAmountCents = newGroupAmountCents,
                exchangeRate = addOn.exchangeRate
            )
            addOn.copy(amountCents = newAmountCents, groupAmountCents = newGroupAmountCents)
        }
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

        val amounts = splits.map { it.amountCents }
        val isExcluded = splits.map { it.isExcluded }
        val rescaled = remainderDistributionService.rescaleAmounts(
            originalTotal = originalTotal,
            newTotal = newTotal,
            amounts = amounts,
            isExcluded = isExcluded
        )

        return splits.mapIndexed { index, split ->
            split.copy(amountCents = rescaled[index])
        }
    }
}
