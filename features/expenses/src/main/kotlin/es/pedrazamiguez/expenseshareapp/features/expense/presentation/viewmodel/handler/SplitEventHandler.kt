package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler

import es.pedrazamiguez.expenseshareapp.core.common.constant.AppConstants
import es.pedrazamiguez.expenseshareapp.domain.enums.SplitType
import es.pedrazamiguez.expenseshareapp.domain.service.split.ExpenseSplitCalculatorFactory
import es.pedrazamiguez.expenseshareapp.domain.service.split.SplitPreviewService
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper.AddExpenseUiMapper
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.action.AddExpenseUiAction
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.AddExpenseUiState
import java.math.BigDecimal
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

/**
 * Handles expense split calculation events:
 * [SplitTypeChanged], [SplitAmountChanged], [SplitPercentageChanged], [SplitExcludedToggled].
 *
 * Also exposes [recalculateSplits] for cross-handler calls (e.g., when source amount changes).
 *
 * All BigDecimal math is delegated to [SplitPreviewService] (domain layer).
 */
class SplitEventHandler(
    private val splitCalculatorFactory: ExpenseSplitCalculatorFactory,
    private val splitPreviewService: SplitPreviewService,
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
            } else {
                split
            }
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
        val decimalDigits = state.selectedCurrency?.decimalDigits ?: 2
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
        val typedCents = splitPreviewService.parseAmountToCents(typedAmount, decimalDigits)
        val remainingCents = (sourceAmountCents - typedCents).coerceAtLeast(0)

        // Other active (non-excluded) members
        val otherActiveIds = state.splits
            .filter { !it.isExcluded && it.userId != editedUserId }
            .map { it.userId }

        // Distribute remainder evenly among the others
        val otherSharesByUserId = if (otherActiveIds.isNotEmpty() && remainingCents > 0) {
            try {
                val calculator = splitCalculatorFactory.create(SplitType.EQUAL)
                calculator.calculateShares(remainingCents, otherActiveIds)
                    .associateBy { it.userId }
            } catch (_: Exception) {
                emptyMap()
            }
        } else {
            emptyMap()
        }

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
                    val share = otherSharesByUserId[uiModel.userId]
                    val cents = share?.amountCents ?: 0L
                    uiModel.copy(
                        amountCents = cents,
                        amountInput = addExpenseUiMapper.formatCentsValue(cents, decimalDigits),
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

        val typedPct = splitPreviewService.parseToDecimal(typedPercentage)

        val otherActiveIds = state.splits
            .filter { !it.isExcluded && it.userId != editedUserId }
            .map { it.userId }
            .toSet()

        // Delegate redistribution math to domain service
        val editedAmountCents =
            splitPreviewService.calculateAmountFromPercentage(typedPct, sourceAmountCents)
        val otherSharesByUserId =
            splitPreviewService.redistributeRemainingPercentage(
                typedPct,
                sourceAmountCents,
                otherActiveIds.toList()
            ).associateBy { it.userId }

        val updatedSplits = state.splits.map { uiModel ->
            when {
                uiModel.userId == editedUserId && !uiModel.isExcluded -> {
                    uiModel.copy(
                        percentageInput = typedPercentage,
                        amountCents = editedAmountCents,
                        formattedAmount = if (sourceAmountCents > 0) {
                            addExpenseUiMapper.formatCentsWithCurrency(
                                editedAmountCents,
                                currencyCode
                            )
                        } else {
                            ""
                        }
                    )
                }

                !uiModel.isExcluded && uiModel.userId in otherActiveIds -> {
                    val share = otherSharesByUserId[uiModel.userId]
                    val pct = share?.percentage ?: BigDecimal.ZERO
                    val amountCents = share?.amountCents ?: 0L
                    uiModel.copy(
                        percentageInput = addExpenseUiMapper.formatPercentageForDisplay(pct),
                        amountCents = amountCents,
                        formattedAmount = if (sourceAmountCents > 0) {
                            addExpenseUiMapper.formatCentsWithCurrency(amountCents, currencyCode)
                        } else {
                            ""
                        }
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
            val sharesByUserId = calculator.calculateShares(sourceAmountCents, activeParticipantIds)
                .associateBy { it.userId }

            val state = _uiState.value
            val updatedSplits = state.splits.map { uiModel ->
                val share = sharesByUserId[uiModel.userId]
                if (share != null && !uiModel.isExcluded) {
                    uiModel.copy(
                        amountCents = share.amountCents,
                        formattedAmount = addExpenseUiMapper.formatCentsWithCurrency(
                            share.amountCents,
                            currencyCode
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
            val sharesByUserId = calculator.calculateShares(sourceAmountCents, activeParticipantIds)
                .associateBy { it.userId }

            val decimalDigits = _uiState.value.selectedCurrency?.decimalDigits ?: 2
            val state = _uiState.value
            val updatedSplits = state.splits.map { uiModel ->
                val share = sharesByUserId[uiModel.userId]
                if (share != null && !uiModel.isExcluded) {
                    uiModel.copy(
                        amountCents = share.amountCents,
                        amountInput = addExpenseUiMapper.formatCentsValue(share.amountCents, decimalDigits),
                        formattedAmount = addExpenseUiMapper.formatCentsWithCurrency(
                            share.amountCents,
                            currencyCode
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

        val sharesByUserId = splitPreviewService.distributePercentagesEvenly(
            sourceAmountCents,
            activeParticipantIds
        ).associateBy { it.userId }

        val state = _uiState.value
        val updatedSplits = state.splits.map { uiModel ->
            val share = sharesByUserId[uiModel.userId]
            if (!uiModel.isExcluded && share != null) {
                val pct = share.percentage ?: BigDecimal.ZERO
                uiModel.copy(
                    percentageInput = addExpenseUiMapper.formatPercentageForDisplay(pct),
                    amountCents = share.amountCents,
                    formattedAmount = if (sourceAmountCents > 0) {
                        addExpenseUiMapper.formatCentsWithCurrency(share.amountCents, currencyCode)
                    } else {
                        ""
                    }
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
        val decimalPlaces = state.selectedCurrency?.decimalDigits ?: 2
        return splitPreviewService.parseAmountToCents(state.sourceAmount.trim(), decimalPlaces)
    }
}
