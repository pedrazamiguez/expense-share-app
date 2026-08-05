package es.pedrazamiguez.splittrip.domain.usecase.balance.impl.strategy

import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.SettlementStatus
import java.time.LocalDateTime

class MutualConfirmationStrategy : SettlementConfirmationStrategy {
    override fun confirm(record: SettlementRecord, currentUserId: String, isCreator: Boolean): SettlementRecord {
        return when (record.status) {
            SettlementStatus.SUGGESTED -> {
                val fromUserId = record.settlement.fromUserId
                require(currentUserId == fromUserId) { "Only payer can confirm in SUGGESTED state" }
                val now = LocalDateTime.now()
                record.copy(
                    status = SettlementStatus.CONFIRMED_BY_PAYER,
                    confirmedByPayerAt = now
                )
            }
            SettlementStatus.CONFIRMED_BY_PAYER -> {
                val toUserId = record.settlement.toUserId
                require(currentUserId == toUserId) { "Only payee can confirm in CONFIRMED_BY_PAYER state" }
                val now = LocalDateTime.now()
                record.copy(
                    status = SettlementStatus.RESOLVED,
                    confirmedByPayeeAt = now,
                    resolvedAt = now
                )
            }
            SettlementStatus.DISPUTED -> {
                val isPayee = record.settlement.toUserId == currentUserId
                require(isPayee || isCreator) { "Only payee or group creator can confirm in DISPUTED state" }
                val now = LocalDateTime.now()
                record.copy(
                    status = SettlementStatus.RESOLVED,
                    confirmedByPayeeAt = now,
                    resolvedAt = now
                )
            }
            SettlementStatus.RESOLVED -> {
                error("Settlement already resolved: ${record.id}")
            }
        }
    }
}
