package es.pedrazamiguez.expenseshareapp.domain.model

import java.math.BigDecimal

/**
 * Represents a single user's share in a **live preview** of an expense split.
 *
 * Unlike [ExpenseSplit] (which is the authoritative split stored at save time),
 * this model carries ephemeral UI-feedback data computed as the user types.
 *
 * @param userId       The participant's user ID.
 * @param amountCents  The preview amount in the smallest currency unit.
 * @param percentage   The preview percentage (only set in PERCENT mode).
 */
data class SplitPreviewShare(val userId: String, val amountCents: Long, val percentage: BigDecimal? = null)
