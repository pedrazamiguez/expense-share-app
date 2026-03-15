package es.pedrazamiguez.expenseshareapp.features.balance.presentation.model

/**
 * UI model representing a single currency's remaining cash balance.
 * Pre-formatted by the mapper for direct display.
 */
data class CashBalanceUiModel(
    val currency: String = "",
    val formattedAmount: String = "",
    val formattedEquivalent: String = ""
)
