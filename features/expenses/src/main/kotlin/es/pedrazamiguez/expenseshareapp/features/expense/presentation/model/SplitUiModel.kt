package es.pedrazamiguez.expenseshareapp.features.expense.presentation.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * UI model representing a single user's share of an expense.
 *
 * In **flat mode**, each row is a group member.
 * In **subunit mode**, rows are either solo members or entity headers (subunits)
 * with nested [entityMembers] containing the intra-subunit splits.
 *
 * [formattedAmount] is the locale-aware display string of the user's share.
 * [amountInput] is the raw user input for EXACT mode (editable).
 * [percentageInput] is the raw user input for PERCENT mode (editable).
 */
data class SplitUiModel(
    val userId: String,
    val displayName: String,
    val amountCents: Long = 0L,
    val formattedAmount: String = "",
    val amountInput: String = "",
    val percentageInput: String = "",
    val isExcluded: Boolean = false,
    /** Whether this member's share is locked (user-set and should not be overwritten by redistribution). */
    val isShareLocked: Boolean = false,
    // ── Subunit mode fields ────────────────────────────────────────
    /** Non-null when this user's split belongs to a subunit. */
    val subunitId: String? = null,
    /** True for entity-level rows (subunit headers) in subunit mode. */
    val isEntityRow: Boolean = false,
    /** Nested member rows when [isEntityRow] is true (accordion content). */
    val entityMembers: ImmutableList<SplitUiModel> = persistentListOf(),
    /** Whether the subunit accordion is expanded. */
    val isExpanded: Boolean = false,
    /** Per-subunit split type override (Level 2 strategy selector). */
    val entitySplitType: SplitTypeUiModel? = null
)
