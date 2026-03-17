package es.pedrazamiguez.expenseshareapp.features.expense.presentation.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * UI model representing a single user's share of an expense.
 *
 * In **flat mode**, each row is a group member.
 * In **sub-unit mode**, rows are either solo members or entity headers (sub-units)
 * with nested [entityMembers] containing the intra-sub-unit splits.
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
    // ── Sub-unit mode fields ────────────────────────────────────────
    /** Non-null when this user's split belongs to a sub-unit. */
    val subunitId: String? = null,
    /** True for entity-level rows (sub-unit headers) in sub-unit mode. */
    val isEntityRow: Boolean = false,
    /** Nested member rows when [isEntityRow] is true (accordion content). */
    val entityMembers: ImmutableList<SplitUiModel> = persistentListOf(),
    /** Whether the sub-unit accordion is expanded. */
    val isExpanded: Boolean = false,
    /** Per-sub-unit split type override (Level 2 strategy selector). */
    val entitySplitType: SplitTypeUiModel? = null
)


