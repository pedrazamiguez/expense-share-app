package es.pedrazamiguez.expenseshareapp.features.expense.presentation.mapper

import es.pedrazamiguez.expenseshareapp.core.common.extensions.localeAwareComparator
import es.pedrazamiguez.expenseshareapp.core.common.provider.LocaleProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.FormattingHelper
import es.pedrazamiguez.expenseshareapp.domain.enums.SplitType
import es.pedrazamiguez.expenseshareapp.domain.model.ExpenseSplit
import es.pedrazamiguez.expenseshareapp.domain.model.User
import es.pedrazamiguez.expenseshareapp.domain.service.RemainderDistributionService
import es.pedrazamiguez.expenseshareapp.domain.service.split.SplitPreviewService
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.SplitUiModel
import java.math.BigDecimal
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
class AddExpenseSplitUiMapper(
    private val localeProvider: LocaleProvider,
    private val formattingHelper: FormattingHelper,
    private val splitPreviewService: SplitPreviewService,
    private val remainderDistributionService: RemainderDistributionService
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
    // Two-level entity flattening + conditional percentage distribution;
    // each branch handles a distinct structural case
    @Suppress("CognitiveComplexMethod", "NestedBlockDepth")
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
                val hundredBd = BigDecimal("100")

                val (withPct, withoutPct) = result.partition { it.percentage != null }
                if (withoutPct.isNotEmpty()) {
                    val claimedPct = withPct.sumOf { it.percentage ?: BigDecimal.ZERO }
                    val remainingPct = hundredBd.subtract(claimedPct)
                    val withoutPctTotal = withoutPct.sumOf { it.amountCents }

                    if (withoutPctTotal > 0) {
                        val amounts = withoutPct.map { it.amountCents }
                        val distributedPcts = remainderDistributionService.distributePercentages(
                            remainingPercentage = remainingPct,
                            amounts = amounts,
                            totalCents = withoutPctTotal
                        )

                        val updatedWithoutPct = withoutPct.mapIndexed { index, split ->
                            split.copy(percentage = distributedPcts[index])
                        }

                        return withPct + updatedWithoutPct
                    }
                }
            }
        }

        return result
    }

    // ── Private helpers ──────────────────────────────────────────────────

    private fun parseLocaleAwareDecimal(input: String): BigDecimal? =
        splitPreviewService.parseToDecimalOrNull(input)
}
