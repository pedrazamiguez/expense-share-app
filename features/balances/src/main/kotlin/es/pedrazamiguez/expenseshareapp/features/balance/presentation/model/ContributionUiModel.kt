package es.pedrazamiguez.expenseshareapp.features.balance.presentation.model

/**
 * UI model representing a single contribution entry in the activity history.
 */
data class ContributionUiModel(
    val id: String = "",
    val userId: String = "",
    val formattedAmount: String = "",
    val dateText: String = ""
)

