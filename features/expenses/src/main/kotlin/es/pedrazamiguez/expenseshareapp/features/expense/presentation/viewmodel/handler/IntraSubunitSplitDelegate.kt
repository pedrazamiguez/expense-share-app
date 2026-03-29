package es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.handler

import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.formatter.FormattingHelper
import es.pedrazamiguez.expenseshareapp.domain.enums.SplitType
import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import es.pedrazamiguez.expenseshareapp.domain.service.split.ExpenseSplitCalculatorFactory
import es.pedrazamiguez.expenseshareapp.domain.service.split.SplitPreviewService
import es.pedrazamiguez.expenseshareapp.domain.service.split.SubunitAwareSplitService
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.SplitUiModel
import java.math.BigDecimal
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * Encapsulates intra-subunit (Level 2) split recalculation logic.
 *
 * Given an entity-level [SplitUiModel] with nested member rows, this delegate
 * recalculates each member's share based on the entity's split type (EQUAL,
 * EXACT, PERCENT) and the subunit's configured `memberShares` weights.
 *
 * This is a **stateless** delegate — it receives all required context as
 * parameters and returns a modified [SplitUiModel] copy.
 */
class IntraSubunitSplitDelegate(
    private val splitCalculatorFactory: ExpenseSplitCalculatorFactory,
    private val splitPreviewService: SplitPreviewService,
    private val subunitAwareSplitService: SubunitAwareSplitService,
    private val formattingHelper: FormattingHelper
) {

    /**
     * Recalculates the member splits within a subunit entity row based on
     * the entity's current [SplitUiModel.amountCents] and [SplitUiModel.entitySplitType].
     *
     * @param entity         The entity-level row containing nested member rows.
     * @param currencyCode   The currency code for formatting.
     * @param groupSubunits  All subunits in the group (used to look up memberShares).
     * @param decimalDigits  Decimal digits for the selected currency.
     * @return The entity with updated [SplitUiModel.entityMembers].
     */
    fun recalculate(
        entity: SplitUiModel,
        currencyCode: String,
        groupSubunits: List<Subunit>,
        decimalDigits: Int
    ): SplitUiModel {
        if (entity.entityMembers.isEmpty()) return entity

        val subunitTotalCents = entity.amountCents
        val intraType = entity.entitySplitType?.let { SplitType.fromString(it.id) } ?: SplitType.EQUAL
        val memberIds = entity.entityMembers.map { it.userId }

        if (subunitTotalCents <= 0 || memberIds.isEmpty()) return entity

        val updatedMembers = when (intraType) {
            SplitType.EQUAL -> recalculateEqual(entity, subunitTotalCents, memberIds, currencyCode, groupSubunits)
            SplitType.EXACT -> recalculateExact(entity, subunitTotalCents, memberIds, currencyCode, decimalDigits)
            SplitType.PERCENT -> recalculatePercent(entity, subunitTotalCents, memberIds, currencyCode)
        }

        return entity.copy(entityMembers = updatedMembers)
    }

    /**
     * Parses the source amount string to cents using the currency's decimal places.
     */
    fun parseSourceAmountToCents(sourceAmount: String, decimalDigits: Int): Long {
        return splitPreviewService.parseAmountToCents(sourceAmount.trim(), decimalDigits)
    }

    // ── EQUAL ───────────────────────────────────────────────────────────

    /**
     * EQUAL intra-subunit recalculation: uses memberShares when available,
     * otherwise falls back to even split via the calculator factory.
     */
    internal fun recalculateEqual(
        entity: SplitUiModel,
        subunitTotalCents: Long,
        memberIds: List<String>,
        currencyCode: String,
        groupSubunits: List<Subunit>
    ): ImmutableList<SplitUiModel> {
        val subunit = groupSubunits.find { it.id == entity.userId }
        val memberShares = subunit?.memberShares ?: emptyMap()

        return if (memberShares.isNotEmpty()) {
            distributeByMemberShares(
                entity.entityMembers,
                subunitTotalCents,
                memberShares,
                currencyCode
            )
        } else {
            try {
                val calculator = splitCalculatorFactory.create(SplitType.EQUAL)
                val shares = calculator.calculateShares(subunitTotalCents, memberIds)
                    .associateBy { it.userId }
                entity.entityMembers.map { member ->
                    val share = shares[member.userId]
                    if (share != null) {
                        member.copy(
                            amountCents = share.amountCents,
                            formattedAmount = formattingHelper.formatCentsWithCurrency(
                                share.amountCents,
                                currencyCode
                            )
                        )
                    } else {
                        member
                    }
                }.toImmutableList()
            } catch (_: Exception) {
                entity.entityMembers
            }
        }
    }

    // ── EXACT ───────────────────────────────────────────────────────────

    /**
     * EXACT intra-subunit recalculation: pre-fills with even distribution
     * so inputs are never blank.
     */
    internal fun recalculateExact(
        entity: SplitUiModel,
        subunitTotalCents: Long,
        memberIds: List<String>,
        currencyCode: String,
        decimalDigits: Int
    ): ImmutableList<SplitUiModel> {
        return try {
            val calculator = splitCalculatorFactory.create(SplitType.EQUAL)
            val shares = calculator.calculateShares(subunitTotalCents, memberIds)
                .associateBy { it.userId }
            entity.entityMembers.map { member ->
                val share = shares[member.userId]
                if (share != null) {
                    member.copy(
                        amountCents = share.amountCents,
                        amountInput = formattingHelper.formatCentsValue(
                            share.amountCents,
                            decimalDigits
                        ),
                        formattedAmount = formattingHelper.formatCentsWithCurrency(
                            share.amountCents,
                            currencyCode
                        )
                    )
                } else {
                    member
                }
            }.toImmutableList()
        } catch (_: Exception) {
            entity.entityMembers
        }
    }

    // ── PERCENT ─────────────────────────────────────────────────────────

    /**
     * PERCENT intra-subunit recalculation: pre-fills with even percentage distribution.
     */
    internal fun recalculatePercent(
        entity: SplitUiModel,
        subunitTotalCents: Long,
        memberIds: List<String>,
        currencyCode: String
    ): ImmutableList<SplitUiModel> {
        val shares = splitPreviewService.distributePercentagesEvenly(
            subunitTotalCents,
            memberIds
        ).associateBy { it.userId }
        return entity.entityMembers.map { member ->
            val share = shares[member.userId]
            if (share != null) {
                val pct = share.percentage ?: BigDecimal.ZERO
                member.copy(
                    percentageInput = formattingHelper.formatPercentageForDisplay(pct),
                    amountCents = share.amountCents,
                    formattedAmount = if (subunitTotalCents > 0) {
                        formattingHelper.formatCentsWithCurrency(
                            share.amountCents,
                            currencyCode
                        )
                    } else {
                        ""
                    }
                )
            } else {
                member
            }
        }.toImmutableList()
    }

    // ── Private Helpers ─────────────────────────────────────────────────

    /**
     * Distributes [totalCents] among members proportionally based on [memberShares] weights.
     * Delegates to [SubunitAwareSplitService.distributeByMemberShares] for the core distribution
     * logic (DOWN rounding + remainder) to keep UI preview and domain calculations consistent.
     */
    private fun distributeByMemberShares(
        members: ImmutableList<SplitUiModel>,
        totalCents: Long,
        memberShares: Map<String, BigDecimal>,
        currencyCode: String
    ): ImmutableList<SplitUiModel> {
        val distributed = subunitAwareSplitService.distributeByMemberShares(
            memberIds = members.map { it.userId },
            totalCents = totalCents,
            memberShares = memberShares
        )
        return members.map { member ->
            val finalAmount = distributed[member.userId] ?: 0L
            member.copy(
                amountCents = finalAmount,
                formattedAmount = formattingHelper.formatCentsWithCurrency(finalAmount, currencyCode)
            )
        }.toImmutableList()
    }
}
