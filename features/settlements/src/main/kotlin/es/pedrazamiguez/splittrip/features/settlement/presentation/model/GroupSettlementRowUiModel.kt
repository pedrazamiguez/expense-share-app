package es.pedrazamiguez.splittrip.features.settlement.presentation.model

import es.pedrazamiguez.splittrip.domain.model.SettlementStatus

data class SettlementRowUiModel(
    val settlementId: String,
    val debtorId: String,
    val creditorId: String,
    val debtorName: String,
    val creditorName: String,
    val directionTitle: String,
    val formattedAmount: String,
    val isCurrentUserDebtor: Boolean,
    val isCurrentUserCreditor: Boolean,
    val pocketTypeLabel: String,
    val currencyCode: String,
    val statusLabel: String,
    val statusChipStyle: SettlementRowStatusStyle,
    val canCurrentUserConfirm: Boolean,
    val canCurrentUserDispute: Boolean,
    val disputedByCurrentUser: Boolean,
    val disputeReason: String? = null,
    val status: SettlementStatus
)

enum class SettlementRowStatusStyle {
    NEUTRAL,
    WARNING,
    SUCCESS,
    ERROR
}
