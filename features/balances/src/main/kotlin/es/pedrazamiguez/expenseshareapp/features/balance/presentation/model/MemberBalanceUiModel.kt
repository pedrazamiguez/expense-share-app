package es.pedrazamiguez.expenseshareapp.features.balance.presentation.model

/**
 * UI model representing a single member's financial position within the group.
 * All amounts are pre-formatted by the mapper for direct display.
 *
 * [displayName] holds the resolved human-readable name (not a raw userId).
 */
data class MemberBalanceUiModel(
    val userId: String = "",
    val displayName: String = "",
    val isCurrentUser: Boolean = false,
    val formattedContributed: String = "",
    val formattedAvailable: String = "",
    val formattedSpent: String = "",
    val formattedNetBalance: String = "",
    val isPositiveBalance: Boolean = true
)
