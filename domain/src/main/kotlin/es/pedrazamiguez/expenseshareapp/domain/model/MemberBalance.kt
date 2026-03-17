package es.pedrazamiguez.expenseshareapp.domain.model

/**
 * Represents a single group member's financial position within the shared pocket.
 *
 * All monetary values are stored in minor units (cents) to avoid
 * floating-point precision issues, consistent with the Expense model.
 *
 * Attribution accounts for sub-unit composition:
 * - Contributions made on behalf of a sub-unit are distributed by [Subunit.memberShares].
 * - Cash withdrawals are attributed based on their [CashWithdrawal.withdrawalScope].
 * - Expense splits are already per-user (expanded by SubunitAwareSplitService at save time).
 *
 * @param userId The member's unique identifier.
 * @param contributed How much this member effectively contributed (in cents),
 *                    including their proportional share of sub-unit contributions.
 * @param withdrawn How much this member effectively withdrew (in cents),
 *                  attributed by withdrawal scope (GROUP/SUBUNIT/USER).
 * @param spent How much this member spent on expenses (in cents), converted to group currency.
 * @param available Cash in hand for this member (in cents): withdrawn − spent.
 *                  Sums across all members should equal the group's total cash in hand.
 * @param netBalance The member's pocket share: contributed − withdrawn.
 *                   Sums across all members should equal the group pocket balance.
 *                   Positive = has funds in the pocket, negative = overdrew from the pocket.
 */
data class MemberBalance(
    val userId: String = "",
    val contributed: Long = 0,
    val withdrawn: Long = 0,
    val spent: Long = 0,
    val available: Long = 0,
    val netBalance: Long = 0
)
