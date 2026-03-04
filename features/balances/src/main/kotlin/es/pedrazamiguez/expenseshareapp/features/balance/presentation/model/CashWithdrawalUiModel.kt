package es.pedrazamiguez.expenseshareapp.features.balance.presentation.model

/**
 * UI model representing a cash withdrawal in the activity list.
 * Pre-formatted by the mapper for direct display.
 */
data class CashWithdrawalUiModel(
    val id: String = "",
    val withdrawnBy: String = "",
    val isCurrentUser: Boolean = false,
    val formattedAmount: String = "",
    val formattedDeducted: String = "",
    val currency: String = "",
    val isForeignCurrency: Boolean = false,
    val dateText: String = ""
)

