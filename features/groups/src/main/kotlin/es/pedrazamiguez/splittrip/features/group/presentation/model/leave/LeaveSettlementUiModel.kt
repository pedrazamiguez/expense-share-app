package es.pedrazamiguez.splittrip.features.group.presentation.model.leave

import es.pedrazamiguez.splittrip.domain.model.SettlementPocketType

data class LeaveSettlementUiModel(
    val settlementId: String = "",
    val debtorName: String = "",
    val creditorName: String = "",
    val directionTitle: String = "",
    val formattedAmount: String = "",
    val pocketTypeLabel: String = "",
    val pocketType: SettlementPocketType = SettlementPocketType.POCKET,
    val statusLabel: String = "",
    val statusType: LeaveSettlementStatusType = LeaveSettlementStatusType.WAITING_FOR_OTHER,
    val isCurrentUserDebtor: Boolean = false,
    val isCurrentUserCreditor: Boolean = false,
    val canCurrentUserConfirm: Boolean = false,
    val isConfirmed: Boolean = false
)
