package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler

import es.pedrazamiguez.expenseshareapp.core.common.constant.AppConstants
import es.pedrazamiguez.expenseshareapp.domain.converter.CurrencyConverter
import es.pedrazamiguez.expenseshareapp.domain.enums.SplitType
import es.pedrazamiguez.expenseshareapp.domain.service.split.ExpenseSplitCalculatorFactory
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper.AddExpenseUiMapper
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.action.AddExpenseUiAction
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Handles expense split calculation events:
 * [SplitTypeChanged], [SplitAmountChanged], [SplitPercentageChanged], [SplitExcludedToggled].
 *
 * Also exposes [recalculateSplits] for cross-handler calls (e.g., when source amount changes).
 */
class SplitEventHandler(
    private val splitCalculatorFactory: ExpenseSplitCalculatorFactory,
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

    fun handleSplitTypeChanged(splitTypeId: String) {
        val selectedSplitType = _uiState.value.availableSplitTypes
            .find { it.id == splitTypeId } ?: return
        _uiState.update {
            it.copy(
                selectedSplitType = selectedSplitType,
                splitError = null
            )
        }
        recalculateSplits()
    }

    fun handleSplitExcludedToggled(userId: String) {
        val updatedSplits = _uiState.value.splits.map { split ->
            if (split.userId == userId) {
                split.copy(isExcluded = !split.isExcluded)
            } else split
        }.toImmutableList()
        _uiState.update { it.copy(splits = updatedSplits, splitError = null) }
        recalculateSplits()
    }

    /**
     * Recalculates the per-user splits based on the current split type,
     * source amount, and active participants.
     *
     * - EQUAL: auto-calculates shares with currency display (e.g., "€16.67").
     * - EXACT: auto-distributes remainder evenly among users who haven't been edited.
     * - PERCENT: auto-distributes remaining percentage evenly among unedited users.
     */
    fun recalculateSplits() {
        val state = _uiState.value
        val splitType = state.selectedSplitType?.let { SplitType.fromString(it.id) } ?: return
        val currencyCode = state.selectedCurrency?.code ?: AppConstants.DEFAULT_CURRENCY_CODE

        val activeParticipantIds = state.splits
            .filter { !it.isExcluded }
            .map { it.userId }
        if (activeParticipantIds.isEmpty()) return

        val sourceAmountCents = parseSourceAmountToCents()

        when (splitType) {
            SplitType.EQUAL -> recalculateEqualSplits(
                sourceAmountCents,
                activeParticipantIds,
                currencyCode
            )

            SplitType.EXACT -> recalculateExactSplits(
                sourceAmountCents,
                activeParticipantIds,
                currencyCode
            )

            SplitType.PERCENT -> recalculatePercentSplits(
                sourceAmountCents,
                activeParticipantIds,
                currencyCode
            )
        }
    }

    /**
     * Handles EXACT mode: user typed an amount for one member.
     * Auto-distributes the remaining amount evenly among the other active members.
     */
    fun handleExactAmountChanged(editedUserId: String, typedAmount: String) {
        val state = _uiState.value
        val currencyCode = state.selectedCurrency?.code ?: AppConstants.DEFAULT_CURRENCY_CODE
        val sourceAmountCents = parseSourceAmountToCents()
        if (sourceAmountCents <= 0) {
            // Just store the typed value, nothing to distribute
            val updatedSplits = state.splits.map { split ->
                if (split.userId == editedUserId) split.copy(amountInput = typedAmount) else split
            }.toImmutableList()
            _uiState.update { it.copy(splits = updatedSplits, splitError = null) }
            return
        }

        // Parse the typed amount to cents
        val typedCents = parseInputToCents(typedAmount)
        val remainingCents = (sourceAmountCents - typedCents).coerceAtLeast(0)

        // Other active (non-excluded) members
        val otherActiveIds = state.splits
            .filter { !it.isExcluded && it.userId != editedUserId }
            .map { it.userId }

        // Distribute remainder evenly among the others
        val otherShares = if (otherActiveIds.isNotEmpty() && remainingCents > 0) {
            try {
                val calculator = splitCalculatorFactory.create(SplitType.EQUAL)
                calculator.calculateShares(remainingCents, otherActiveIds)
            } catch (_: Exception) {
                emptyList()
            }
        } else emptyList()

        val updatedSplits = state.splits.map { uiModel ->
            when {
                uiModel.userId == editedUserId && !uiModel.isExcluded -> {
                    uiModel.copy(
                        amountInput = typedAmount,
                        amountCents = typedCents,
                        formattedAmount = addExpenseUiMapper.formatCentsWithCurrency(
                            typedCents,
                            currencyCode
                        )
                    )
                }

                !uiModel.isExcluded -> {
                    val share = otherShares.find { it.userId == uiModel.userId }
                    val cents = share?.amountCents ?: 0L
                    uiModel.copy(
                        amountCents = cents,
                        amountInput = addExpenseUiMapper.formatCentsValue(cents),
                        formattedAmount = addExpenseUiMapper.formatCentsWithCurrency(
                            cents,
                            currencyCode
                        )
                    )
                }

                else -> uiModel // excluded — keep as-is
            }
        }.toImmutableList()

        _uiState.update { it.copy(splits = updatedSplits, splitError = null) }
    }

    /**
     * Handles PERCENT mode: user typed a percentage for one member.
     * Auto-distributes the remaining percentage evenly among the other active members.
     */
    fun handlePercentageChanged(editedUserId: String, typedPercentage: String) {
        val state = _uiState.value
        val currencyCode = state.selectedCurrency?.code ?: AppConstants.DEFAULT_CURRENCY_CODE
        val sourceAmountCents = parseSourceAmountToCents()

        val typedPct = parseInputToDecimal(typedPercentage)
        val hundred = BigDecimal("100")
        val remainingPct = hundred.subtract(typedPct).coerceAtLeast(BigDecimal.ZERO)

        val otherActiveIds = state.splits
            .filter { !it.isExcluded && it.userId != editedUserId }
            .map { it.userId }

        // Distribute remaining percentage evenly
        val otherCount = otherActiveIds.size
        val otherBasePct = if (otherCount > 0) {
            remainingPct.divide(BigDecimal(otherCount), 2, RoundingMode.DOWN)
        } else BigDecimal.ZERO

        // Remainder distribution for rounding
        val allocatedOtherPct = otherBasePct.multiply(BigDecimal(otherCount))
        var pctRemainder = remainingPct.subtract(allocatedOtherPct)
            .movePointRight(2)
            .setScale(0, RoundingMode.DOWN)
            .toInt()
        val smallestUnit = BigDecimal("0.01")

        val updatedSplits = state.splits.map { uiModel ->
            when {
                uiModel.userId == editedUserId && !uiModel.isExcluded -> {
                    val amountCents = if (sourceAmountCents > 0) {
                        sourceAmountCents.toBigDecimal()
                            .multiply(typedPct)
                            .divide(hundred, 0, RoundingMode.DOWN)
                            .toLong()
                    } else 0L
                    uiModel.copy(
                        percentageInput = typedPercentage,
                        amountCents = amountCents,
                        formattedAmount = if (sourceAmountCents > 0) {
                            addExpenseUiMapper.formatCentsWithCurrency(amountCents, currencyCode)
                        } else ""
                    )
                }

                !uiModel.isExcluded && uiModel.userId in otherActiveIds -> {
                    val pct = if (pctRemainder > 0) {
                        pctRemainder--
                        otherBasePct.add(smallestUnit)
                    } else {
                        otherBasePct
                    }
                    val amountCents = if (sourceAmountCents > 0) {
                        sourceAmountCents.toBigDecimal()
                            .multiply(pct)
                            .divide(hundred, 0, RoundingMode.DOWN)
                            .toLong()
                    } else 0L
                    uiModel.copy(
                        percentageInput = addExpenseUiMapper.formatPercentageForDisplay(pct),
                        amountCents = amountCents,
                        formattedAmount = if (sourceAmountCents > 0) {
                            addExpenseUiMapper.formatCentsWithCurrency(amountCents, currencyCode)
                        } else ""
                    )
                }

                else -> uiModel // excluded — keep as-is
            }
        }.toImmutableList()

        _uiState.update { it.copy(splits = updatedSplits, splitError = null) }
    }

    // ── Private helpers ──────────────────────────────────────────────────

    /**
     * EQUAL: Splits the total evenly, showing amounts with currency symbol (read-only).
     */
    private fun recalculateEqualSplits(
        sourceAmountCents: Long,
        activeParticipantIds: List<String>,
        currencyCode: String
    ) {
        if (sourceAmountCents <= 0) return

        try {
            val calculator = splitCalculatorFactory.create(SplitType.EQUAL)
            val shares = calculator.calculateShares(sourceAmountCents, activeParticipantIds)

            val state = _uiState.value
            val updatedSplits = state.splits.map { uiModel ->
                val share = shares.find { it.userId == uiModel.userId }
                if (share != null && !uiModel.isExcluded) {
                    uiModel.copy(
                        amountCents = share.amountCents,
                        formattedAmount = addExpenseUiMapper.formatCentsWithCurrency(
                            share.amountCents, currencyCode
                        )
                    )
                } else if (uiModel.isExcluded) {
                    uiModel.copy(amountCents = 0L, formattedAmount = "")
                } else {
                    uiModel
                }
            }.toImmutableList()

            _uiState.update { it.copy(splits = updatedSplits, splitError = null) }
        } catch (e: Exception) {
            Timber.w(e, "Failed to calculate equal splits")
        }
    }

    /**
     * EXACT: Auto-distributes the remaining amount evenly among the
     * other active members. The user who was just edited keeps their typed value.
     * Called from [recalculateSplits] when switching types (resets all to even).
     */
    private fun recalculateExactSplits(
        sourceAmountCents: Long,
        activeParticipantIds: List<String>,
        currencyCode: String
    ) {
        if (sourceAmountCents <= 0 || activeParticipantIds.isEmpty()) return

        // When switching to EXACT mode, start with an even split
        try {
            val calculator = splitCalculatorFactory.create(SplitType.EQUAL)
            val shares = calculator.calculateShares(sourceAmountCents, activeParticipantIds)

            val state = _uiState.value
            val updatedSplits = state.splits.map { uiModel ->
                val share = shares.find { it.userId == uiModel.userId }
                if (share != null && !uiModel.isExcluded) {
                    uiModel.copy(
                        amountCents = share.amountCents,
                        amountInput = addExpenseUiMapper.formatCentsValue(share.amountCents),
                        formattedAmount = addExpenseUiMapper.formatCentsWithCurrency(
                            share.amountCents, currencyCode
                        )
                    )
                } else if (uiModel.isExcluded) {
                    uiModel.copy(amountCents = 0L, amountInput = "", formattedAmount = "")
                } else {
                    uiModel
                }
            }.toImmutableList()

            _uiState.update { it.copy(splits = updatedSplits, splitError = null) }
        } catch (e: Exception) {
            Timber.w(e, "Failed to calculate initial exact splits")
        }
    }

    /**
     * PERCENT: Auto-distributes 100% evenly among active members.
     * Called from [recalculateSplits] when switching types (resets all to even).
     */
    private fun recalculatePercentSplits(
        sourceAmountCents: Long,
        activeParticipantIds: List<String>,
        currencyCode: String
    ) {
        if (activeParticipantIds.isEmpty()) return

        val totalPercent = BigDecimal("100")
        val count = activeParticipantIds.size
        val basePercent = totalPercent.divide(
            BigDecimal(count), 2, RoundingMode.DOWN
        )
        val allocatedPercent = basePercent.multiply(BigDecimal(count))
        var remainderCents = totalPercent.subtract(allocatedPercent)
            .movePointRight(2)
            .setScale(0, RoundingMode.DOWN)
            .toInt()
        val smallestUnit = BigDecimal("0.01")

        val state = _uiState.value
        val updatedSplits = state.splits.map { uiModel ->
            if (!uiModel.isExcluded && uiModel.userId in activeParticipantIds) {
                val pct = if (remainderCents > 0) {
                    remainderCents--
                    basePercent.add(smallestUnit)
                } else {
                    basePercent
                }
                // Calculate cents from percentage
                val amountCents = sourceAmountCents.toBigDecimal()
                    .multiply(pct)
                    .divide(totalPercent, 0, RoundingMode.DOWN)
                    .toLong()

                uiModel.copy(
                    percentageInput = addExpenseUiMapper.formatPercentageForDisplay(pct),
                    amountCents = amountCents,
                    formattedAmount = if (sourceAmountCents > 0) {
                        addExpenseUiMapper.formatCentsWithCurrency(amountCents, currencyCode)
                    } else ""
                )
            } else if (uiModel.isExcluded) {
                uiModel.copy(percentageInput = "", amountCents = 0L, formattedAmount = "")
            } else {
                uiModel
            }
        }.toImmutableList()

        _uiState.update { it.copy(splits = updatedSplits, splitError = null) }
    }

    /**
     * Parses the current source amount from UiState to cents.
     */
    private fun parseSourceAmountToCents(): Long {
        val state = _uiState.value
        return try {
            val normalized = CurrencyConverter.normalizeAmountString(state.sourceAmount.trim())
            val decimalPlaces = state.selectedCurrency?.decimalDigits ?: 2
            val amount = normalized.toBigDecimalOrNull() ?: BigDecimal.ZERO
            val multiplier = BigDecimal.TEN.pow(decimalPlaces)
            amount.multiply(multiplier).setScale(0, RoundingMode.HALF_UP).toLong()
        } catch (_: Exception) {
            0L
        }
    }

    /**
     * Parses a locale-aware amount input string to cents (Long).
     * Uses the selected currency's decimal digits for correct conversion
     * (e.g., 0 for JPY, 2 for EUR, 3 for TND).
     */
    private fun parseInputToCents(input: String): Long {
        return try {
            val normalized = CurrencyConverter.normalizeAmountString(input.trim())
            val decimalPlaces = _uiState.value.selectedCurrency?.decimalDigits ?: 2
            val amount = normalized.toBigDecimalOrNull() ?: BigDecimal.ZERO
            amount.movePointRight(decimalPlaces).setScale(0, RoundingMode.HALF_UP).toLong()
        } catch (_: Exception) {
            0L
        }
    }

    /**
     * Parses a locale-aware decimal input string to BigDecimal.
     */
    private fun parseInputToDecimal(input: String): BigDecimal {
        return try {
            val normalized = CurrencyConverter.normalizeAmountString(input.trim())
            normalized.toBigDecimalOrNull() ?: BigDecimal.ZERO
        } catch (_: Exception) {
            BigDecimal.ZERO
        }
    }

    private fun BigDecimal.coerceAtLeast(min: BigDecimal): BigDecimal =
        if (this < min) min else this
}

