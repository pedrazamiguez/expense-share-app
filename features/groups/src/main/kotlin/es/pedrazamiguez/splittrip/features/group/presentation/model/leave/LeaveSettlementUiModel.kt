package es.pedrazamiguez.splittrip.features.group.presentation.model.leave

data class LeaveSettlementUiModel(
    val settlementId: String = "",
    val debtorName: String = "",
    val creditorName: String = "",
    val formattedAmount: String = "",
    val pocketTypeLabel: String = "",
    val isCurrentUserDebtor: Boolean = false,
    val isCurrentUserCreditor: Boolean = false,
    val canCurrentUserConfirm: Boolean = false,
    val isConfirmed: Boolean = false
)
