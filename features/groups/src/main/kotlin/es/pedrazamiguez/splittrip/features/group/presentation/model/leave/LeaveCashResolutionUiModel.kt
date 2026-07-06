package es.pedrazamiguez.splittrip.features.group.presentation.model.leave

data class LeaveCashResolutionUiModel(
    val requiresDeposit: Boolean = false,
    val requiresReimbursement: Boolean = false,
    val formattedAmount: String = ""
)
