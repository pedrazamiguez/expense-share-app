package es.pedrazamiguez.expenseshareapp.features.balance.presentation.model

/**
 * UI model representing a single contribution entry in the activity history.
 *
 * [displayName] holds the resolved human-readable name (not a raw userId).
 * [scopeLabel] is a pre-formatted label indicating scope: "Personal", subunit name, or "Group".
 * [createdByDisplayName] is the resolved actor name when the contribution was logged on behalf
 * of another member (impersonation). `null` when actor == target, `createdBy` is blank
 * (legacy data), or the actor's profile could not be resolved.
 */
data class ContributionUiModel(
    val id: String = "",
    val displayName: String = "",
    val isCurrentUser: Boolean = false,
    val formattedAmount: String = "",
    val dateText: String = "",
    val scopeLabel: String? = null,
    val isSubunitContribution: Boolean = false,
    val isPersonalContribution: Boolean = false,
    val isGroupContribution: Boolean = false,
    val createdByDisplayName: String? = null
)
