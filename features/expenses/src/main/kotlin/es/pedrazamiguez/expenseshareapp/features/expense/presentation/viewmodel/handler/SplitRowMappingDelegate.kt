package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler

import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.FormattingHelper
import es.pedrazamiguez.expenseshareapp.domain.enums.SplitType
import es.pedrazamiguez.expenseshareapp.domain.model.SplitPreviewShare
import es.pedrazamiguez.expenseshareapp.domain.service.split.ExpenseSplitCalculatorFactory
import es.pedrazamiguez.expenseshareapp.domain.service.split.SplitPreviewService
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.SplitUiModel
import java.math.BigDecimal
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * Stateless delegate that encapsulates the 4-way split row mapping pattern
 * used when a user edits an exact amount or percentage for a split row.
 *
 * The pattern:
 * 1. **Edited row** → apply the typed value, auto-lock
 * 2. **Locked + active row** → keep as-is
 * 3. **Unlocked + active row** → apply redistributed value
 * 4. **Excluded / inactive** → keep as-is
 *
 * Extracted from [SplitEventHandler] and [SubunitSplitEventHandler] to reduce
 * cognitive complexity while keeping the mapping logic testable.
 */
class SplitRowMappingDelegate(
    private val splitCalculatorFactory: ExpenseSplitCalculatorFactory,
    private val splitPreviewService: SplitPreviewService,
    private val formattingHelper: FormattingHelper
) {

    /**
     * Distributes an edited exact amount across active, unlocked split rows.
     *
     * Returns the updated split list with the edited user locked at the typed value,
     * locked users kept as-is, and the remainder distributed evenly among
     * unlocked active users.
     */
    fun applyExactAmountUpdate(
        splits: List<SplitUiModel>,
        editedUserId: String,
        typedAmount: String,
        typedCents: Long,
        sourceAmountCents: Long,
        currencyCode: String,
        decimalDigits: Int
    ): ImmutableList<SplitUiModel> {
        val lockedCents = splits
            .filter { it.isShareLocked && !it.isExcluded && it.userId != editedUserId }
            .sumOf { it.amountCents }

        val remainingCents = (sourceAmountCents - typedCents - lockedCents).coerceAtLeast(0)

        val otherActiveIds = splits
            .filter { !it.isExcluded && it.userId != editedUserId && !it.isShareLocked }
            .map { it.userId }

        val otherSharesByUserId = distributeRemainderEvenly(otherActiveIds, remainingCents)

        return splits.map { uiModel ->
            mapExactRow(
                uiModel = uiModel,
                editedUserId = editedUserId,
                typedAmount = typedAmount,
                typedCents = typedCents,
                otherSharesByUserId = otherSharesByUserId,
                currencyCode = currencyCode,
                decimalDigits = decimalDigits
            )
        }.toImmutableList()
    }

    /**
     * Distributes an edited percentage across active, unlocked split rows.
     *
     * Returns the updated split list with the edited user locked at the typed percentage,
     * locked users kept as-is, and the remaining percentage distributed evenly among
     * unlocked active users.
     */
    fun applyPercentageUpdate(
        splits: List<SplitUiModel>,
        editedUserId: String,
        typedPercentage: String,
        sourceAmountCents: Long,
        currencyCode: String
    ): ImmutableList<SplitUiModel> {
        val typedPct = splitPreviewService.parseToDecimal(typedPercentage)

        val otherActiveIds = splits
            .filter { !it.isExcluded && it.userId != editedUserId }
            .map { it.userId }

        val lockedPercentages = splits
            .filter { it.isShareLocked && !it.isExcluded && it.userId != editedUserId && it.userId in otherActiveIds }
            .associate { it.userId to splitPreviewService.parseToDecimal(it.percentageInput) }

        val editedAmountCents = splitPreviewService.calculateAmountFromPercentage(typedPct, sourceAmountCents)
        val otherSharesByUserId = splitPreviewService.redistributeRemainingPercentage(
            typedPct,
            sourceAmountCents,
            otherActiveIds,
            lockedPercentages
        ).associateBy { it.userId }

        return splits.map { uiModel ->
            mapPercentageRow(
                uiModel = uiModel,
                editedUserId = editedUserId,
                typedPercentage = typedPercentage,
                editedAmountCents = editedAmountCents,
                otherActiveIds = otherActiveIds.toSet(),
                otherSharesByUserId = otherSharesByUserId,
                sourceAmountCents = sourceAmountCents,
                currencyCode = currencyCode
            )
        }.toImmutableList()
    }

    // ── Row-level mapping ────────────────────────────────────────────────

    internal fun mapExactRow(
        uiModel: SplitUiModel,
        editedUserId: String,
        typedAmount: String,
        typedCents: Long,
        otherSharesByUserId: Map<String, Long>,
        currencyCode: String,
        decimalDigits: Int
    ): SplitUiModel = when {
        uiModel.userId == editedUserId && !uiModel.isExcluded -> uiModel.copy(
            amountInput = typedAmount,
            amountCents = typedCents,
            isShareLocked = true,
            formattedAmount = formattingHelper.formatCentsWithCurrency(typedCents, currencyCode)
        )
        uiModel.isShareLocked && !uiModel.isExcluded -> uiModel
        !uiModel.isExcluded -> {
            val cents = otherSharesByUserId[uiModel.userId] ?: 0L
            uiModel.copy(
                amountCents = cents,
                amountInput = formattingHelper.formatCentsValue(cents, decimalDigits),
                formattedAmount = formattingHelper.formatCentsWithCurrency(cents, currencyCode)
            )
        }
        else -> uiModel
    }

    internal fun mapPercentageRow(
        uiModel: SplitUiModel,
        editedUserId: String,
        typedPercentage: String,
        editedAmountCents: Long,
        otherActiveIds: Set<String>,
        otherSharesByUserId: Map<String, SplitPreviewShare>,
        sourceAmountCents: Long,
        currencyCode: String
    ): SplitUiModel = when {
        uiModel.userId == editedUserId && !uiModel.isExcluded -> uiModel.copy(
            percentageInput = typedPercentage,
            amountCents = editedAmountCents,
            isShareLocked = true,
            formattedAmount = formatAmountOrEmpty(editedAmountCents, sourceAmountCents, currencyCode)
        )
        uiModel.isShareLocked && !uiModel.isExcluded && uiModel.userId in otherActiveIds -> uiModel
        !uiModel.isExcluded && uiModel.userId in otherActiveIds -> {
            val share = otherSharesByUserId[uiModel.userId]
            val pct = share?.percentage ?: BigDecimal.ZERO
            val amountCents = share?.amountCents ?: 0L
            uiModel.copy(
                percentageInput = formattingHelper.formatPercentageForDisplay(pct),
                amountCents = amountCents,
                formattedAmount = formatAmountOrEmpty(amountCents, sourceAmountCents, currencyCode)
            )
        }
        else -> uiModel
    }

    // ── Shared helpers ───────────────────────────────────────────────────

    internal fun distributeRemainderEvenly(
        activeIds: List<String>,
        remainingCents: Long
    ): Map<String, Long> {
        if (activeIds.isEmpty() || remainingCents <= 0) return emptyMap()
        return try {
            val calculator = splitCalculatorFactory.create(SplitType.EQUAL)
            calculator.calculateShares(remainingCents, activeIds)
                .associate { it.userId to it.amountCents }
        } catch (_: Exception) {
            emptyMap()
        }
    }

    internal fun formatAmountOrEmpty(amountCents: Long, sourceAmountCents: Long, currencyCode: String): String =
        if (sourceAmountCents > 0) {
            formattingHelper.formatCentsWithCurrency(amountCents, currencyCode)
        } else {
            ""
        }
}
