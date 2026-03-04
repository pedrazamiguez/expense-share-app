package es.pedrazamiguez.expenseshareapp.features.balance.presentation.model

/**
 * UI model representing the group pocket balance summary.
 * All amounts are pre-formatted by the mapper for direct display.
 */
data class GroupPocketBalanceUiModel(
    val formattedBalance: String = "",
    val formattedTotalContributed: String = "",
    val formattedTotalSpent: String = "",
    val currency: String = ""
)

