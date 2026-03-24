package es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper

import es.pedrazamiguez.expenseshareapp.core.common.extensions.localeAwareComparator
import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.FormattingHelper
import es.pedrazamiguez.expenseshareapp.domain.enums.SplitType
import es.pedrazamiguez.expenseshareapp.domain.model.ExpenseSplit
import es.pedrazamiguez.expenseshareapp.domain.model.User
import es.pedrazamiguez.expenseshareapp.domain.service.split.SplitPreviewService
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.SplitUiModel
import java.math.BigDecimal
import java.math.RoundingMode
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * Handles split-related UI mapping and locale-aware display formatting for
 * the Add Expense form.
 *
 * Responsible for:
 * - Building and sorting initial [SplitUiModel] lists
 * - Resolving member display names
 * - Mapping splits to domain [ExpenseSplit] objects (flat and entity modes)
 * - Formatting cents as plain values or currency-symbol strings for split rows
 *
 * Extracted from [AddExpenseUiMapper] to keep class function count within the
 * configured Detekt threshold.
 */
class AddExpenseSplitMapper(
    private val localeProvider: LocaleProvider,
    private val formattingHelper: FormattingHelper,
    private val splitPreviewService: SplitPreviewService
) {

    /**
     * Builds initial split UI models for all group members.
     */
    fun buildInitialSplits(
        memberIds: List<String>,
        shares: List<ExpenseSplit>,
        memberProfiles: Map<String, User> = emptyMap()
    ): ImmutableList<SplitUiModel> =
        memberIds.map { userId ->
            val share = shares.find { it.userId == userId }
            val amountCents = share?.amountCents ?: 0L
            SplitUiModel(
                userId = userId,
                displayName = resolveDisplayName(userId, memberProfiles),
                amountCents = amountCents,
                formattedAmount = formattingHelper.formatCentsValue(amountCents),
                amountInput = formattingHelper.formatCentsValue(amountCents),
                percentageInput = share?.percentage?.toPlainString() ?: ""
            )
        }.sortedWith(
            localeAwareComparator(localeProvider.getCurrentLocale()) { it.displayName }
        ).toImmutableList()

    /**
     * Resolves a userId to a human-readable display name using the
     * fallback hierarchy: displayName → email → raw userId.
     */
    fun resolveDisplayName(userId: String, memberProfiles: Map<String, User>): String {
        val user = memberProfiles[userId] ?: return userId
        return user.displayName?.takeIf { it.isNotBlank() }
            ?: user.email.takeIf { it.isNotBlank() }
            ?: userId
    }

    /**
     * Maps flat split UI models to domain [ExpenseSplit] list.
     */
    fun mapSplitsToDomain(splits: List<SplitUiModel>, splitType: SplitType): List<ExpenseSplit> =
        splits.filter { !it.isExcluded }.map { uiModel ->
            ExpenseSplit(
                userId = uiModel.userId,
                amountCents = uiModel.amountCents,
                percentage = if (splitType == SplitType.PERCENT) {
                    parseLocaleAwareDecimal(uiModel.percentageInput)
                } else {
                    null
                },
                subunitId = uiModel.subunitId
            )
        }

    /**
     * Flattens entity-level splits into per-user [ExpenseSplit] entries for domain mapping.
     *
     * In subunit mode, entity rows contain nested member rows. This method extracts
     * all member rows from subunit entities and includes solo entity rows directly,
     * producing the flat list needed for storage.
     *
     * When [splitType] is PERCENT, effective per-user percentages are computed using
     * DOWN rounding + remainder distribution so the total sums to exactly 100.00.
     */
    fun mapEntitySplitsToDomain(
        entitySplits: List<SplitUiModel>,
        splitType: SplitType
    ): List<ExpenseSplit> {
        val result = mutableListOf<ExpenseSplit>()
        for (entity in entitySplits) {
            if (entity.isExcluded) continue
            if (entity.entityMembers.isEmpty()) {
                result.add(
                    ExpenseSplit(
                        userId = entity.userId,
                        amountCents = entity.amountCents,
                        percentage = if (splitType == SplitType.PERCENT) {
                            parseLocaleAwareDecimal(entity.percentageInput)
                        } else {
                            null
                        },
                        subunitId = null
                    )
                )
            } else {
                for (member in entity.entityMembers) {
                    result.add(
                        ExpenseSplit(
                            userId = member.userId,
                            amountCents = member.amountCents,
                            percentage = null,
                            subunitId = member.subunitId ?: entity.userId
                        )
                    )
                }
            }
        }

        if (splitType == SplitType.PERCENT) {
            val totalCents = result.sumOf { it.amountCents }
            if (totalCents > 0) {
                val totalBd = BigDecimal(totalCents)
                val hundredBd = BigDecimal(100)
                val smallestUnit = BigDecimal("0.01")

                val (withPct, withoutPct) = result.partition { it.percentage != null }
                if (withoutPct.isNotEmpty()) {
                    val claimedPct = withPct.sumOf { it.percentage ?: BigDecimal.ZERO }
                    val remainingPct = hundredBd.subtract(claimedPct)

                    val rawPcts = withoutPct.map { split ->
                        val pct = BigDecimal(split.amountCents)
                            .multiply(hundredBd)
                            .divide(totalBd, 2, RoundingMode.DOWN)
                        split to pct
                    }

                    val allocatedPct = rawPcts.sumOf { it.second }
                    var remainderUnits = remainingPct.subtract(allocatedPct)
                        .divide(smallestUnit, 0, RoundingMode.DOWN)
                        .toInt()
                        .coerceAtLeast(0)

                    val updatedWithoutPct = rawPcts.map { (split, pct) ->
                        val extra = if (remainderUnits > 0) {
                            remainderUnits--
                            smallestUnit
                        } else {
                            BigDecimal.ZERO
                        }
                        split.copy(percentage = pct.add(extra))
                    }

                    return withPct + updatedWithoutPct
                }
            }
        }

        return result
    }

    /**
     * Formats cents to a plain decimal string for input fields.
     * Delegates to [FormattingHelper.formatCentsValue].
     */
    fun formatCentsValue(cents: Long, decimalDigits: Int = 2): String =
        formattingHelper.formatCentsValue(cents, decimalDigits)

    /**
     * Formats cents to a locale-aware string WITH currency symbol.
     * Used for read-only split displays (e.g., EQUAL mode: "€16.67").
     */
    fun formatCentsWithCurrency(cents: Long, currencyCode: String): String =
        formattingHelper.formatCentsWithCurrency(cents, currencyCode)

    /**
     * Formats a BigDecimal percentage for display (e.g., "33.33").
     */
    fun formatPercentageForDisplay(percentage: BigDecimal): String =
        formattingHelper.formatPercentageForDisplay(percentage)

    // ── Private helpers ──────────────────────────────────────────────────

    private fun parseLocaleAwareDecimal(input: String): BigDecimal? {
        if (input.isBlank()) return null
        return splitPreviewService.parseToDecimal(input)
    }
}
