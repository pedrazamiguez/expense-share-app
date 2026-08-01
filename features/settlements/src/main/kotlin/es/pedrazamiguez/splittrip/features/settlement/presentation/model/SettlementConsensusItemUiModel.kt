package es.pedrazamiguez.splittrip.features.settlement.presentation.model

import es.pedrazamiguez.splittrip.domain.model.SettlementStatus

data class SettlementConsensusItemUiModel(
    val settlementId: String,
    val counterpartyName: String,
    val formattedAmount: String,
    val currencyCode: String,
    val pocketTypeLabel: String,
    val directionLabel: String,
    val statusLabel: String,
    val statusChipStyle: ConsensusChipStyle,
    val isCurrentUserPayer: Boolean,
    val canConfirm: Boolean,
    val confirmLabel: String,
    val canDispute: Boolean,
    val disputeReason: String? = null,
    val status: SettlementStatus,
    val canNudge: Boolean = false,
    val isNudgeRateLimited: Boolean = false,
    val nudgeButtonLabel: String = ""
)

enum class ConsensusChipStyle {
    SUGGESTED,
    IN_PROGRESS,
    DISPUTED
}
