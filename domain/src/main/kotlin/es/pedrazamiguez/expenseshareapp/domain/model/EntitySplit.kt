package es.pedrazamiguez.expenseshareapp.domain.model

import java.math.BigDecimal

/**
 * Represents an entity-level share (Level 1) in two-level sub-unit-aware splitting.
 *
 * An "entity" is either a solo user or a sub-unit treated as a single participant.
 * This model carries the entity's share of the total expense before it is expanded
 * into per-user [ExpenseSplit] entries.
 *
 * @param entityId  User ID for solo participants, sub-unit ID for sub-units.
 * @param amountCents  The entity's share in the smallest currency unit (used by EXACT strategy).
 * @param percentage  The entity's percentage share (used by PERCENT strategy).
 */
data class EntitySplit(
    val entityId: String,
    val amountCents: Long = 0,
    val percentage: BigDecimal? = null
)
