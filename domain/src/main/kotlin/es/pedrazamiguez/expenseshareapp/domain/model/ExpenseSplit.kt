package es.pedrazamiguez.expenseshareapp.domain.model

import java.math.BigDecimal

/**
 * Represents a single user's share of an expense.
 *
 * [amountCents] is the **single source of truth** for the debt amount (3NF).
 * When using the PERCENT strategy, [percentage] is stored alongside for reference,
 * but [amountCents] is always the authoritative calculated value.
 * Percentages should never be back-calculated from amounts at read time.
 */
data class ExpenseSplit(
    val userId: String,
    val amountCents: Long,
    val percentage: BigDecimal? = null,
    val isExcluded: Boolean = false,
    val isCoveredById: String? = null
)
