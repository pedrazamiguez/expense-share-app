package es.pedrazamiguez.splittrip.features.settlement.presentation.model

/**
 * UI model for a single withdrawal's attributed cash share shown in the cash breakdown sheet.
 *
 * Each instance maps to one [CashWithdrawal], carrying only the member's attributed portion
 * of that withdrawal's remaining balance — not the full withdrawal amount.
 */
data class CashBreakdownUiModel(
    val withdrawalLabel: String = "",
    val dateText: String = "",
    val formattedRate: String = "",
    val formattedNativeRemaining: String = "",
    val formattedEquivalent: String = "",
    val scopeLabel: String = "",
    val isEstimatedShare: Boolean = false,
    val formattedAddOns: String = ""
)
