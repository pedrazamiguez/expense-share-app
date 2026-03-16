package es.pedrazamiguez.expenseshareapp.features.balance.presentation.model

/**
 * UI model representing a cash withdrawal in the activity list.
 * Pre-formatted by the mapper for direct display.
 *
 * [displayName] holds the resolved human-readable name (not a raw userId).
 * [scopeLabel] is a pre-formatted label for sub-unit or personal withdrawals (null for group-scoped).
 */
data class CashWithdrawalUiModel(
    val id: String = "",
    val displayName: String = "",
    val isCurrentUser: Boolean = false,
    val formattedAmount: String = "",
    val formattedDeducted: String = "",
    val currency: String = "",
    val isForeignCurrency: Boolean = false,
    val dateText: String = "",
    val scopeLabel: String? = null,
    val isSubunitWithdrawal: Boolean = false,
    val isPersonalWithdrawal: Boolean = false
)
