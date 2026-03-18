package es.pedrazamiguez.expenseshareapp.features.balance.presentation.model

/**
 * UI model representing a single member's financial position within the group.
 * All amounts are pre-formatted by the mapper for direct display.
 *
 * Mirrors the domain [MemberBalance] model's cash/non-cash split:
 * - [formattedPocketBalance]: virtual pocket share (contributed − withdrawn − nonCashSpent)
 * - [formattedCashInHand]: physical cash remaining (withdrawn − cashSpent)
 * - [formattedTotalSpent]: all expenses (cashSpent + nonCashSpent)
 *
 * [displayName] holds the resolved human-readable name (not a raw userId).
 */
data class MemberBalanceUiModel(
    val userId: String = "",
    val displayName: String = "",
    val isCurrentUser: Boolean = false,
    val formattedContributed: String = "",
    val formattedCashInHand: String = "",
    val formattedTotalSpent: String = "",
    val formattedPocketBalance: String = "",
    val isPositiveBalance: Boolean = true
)
