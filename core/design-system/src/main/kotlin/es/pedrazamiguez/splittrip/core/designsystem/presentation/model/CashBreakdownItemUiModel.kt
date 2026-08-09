package es.pedrazamiguez.splittrip.core.designsystem.presentation.model

/**
 * Generic UI model for a single entry shown in the cash breakdown sheet.
 */
data class CashBreakdownItemUiModel(
    val withdrawalLabel: String = "",
    val dateText: String = "",
    val formattedRate: String = "",
    val formattedNativeRemaining: String = "",
    val formattedEquivalent: String = "",
    val scopeLabel: String = "",
    val isEstimatedShare: Boolean = false,
    val formattedAddOns: String = ""
)
