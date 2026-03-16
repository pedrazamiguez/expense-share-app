package es.pedrazamiguez.expenseshareapp.features.balance.presentation.model

/**
 * UI model representing a single contribution entry in the activity history.
 *
 * [displayName] holds the resolved human-readable name (not a raw userId).
 * [subunitName] is non-null when the contribution was made on behalf of a sub-unit.
 */
data class ContributionUiModel(
    val id: String = "",
    val displayName: String = "",
    val isCurrentUser: Boolean = false,
    val formattedAmount: String = "",
    val dateText: String = "",
    val subunitName: String? = null
)
