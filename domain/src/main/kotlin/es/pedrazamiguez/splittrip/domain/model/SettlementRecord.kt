package es.pedrazamiguez.splittrip.domain.model

import java.time.LocalDateTime

data class SettlementRecord(
    val id: String,
    val groupId: String,
    val settlement: Settlement,
    val status: SettlementStatus,
    val createdAt: LocalDateTime,
    val confirmedByPayerAt: LocalDateTime? = null,
    val confirmedByPayeeAt: LocalDateTime? = null,
    val resolvedAt: LocalDateTime? = null,
    val disputedBy: String? = null,
    val disputeReason: String? = null
)
