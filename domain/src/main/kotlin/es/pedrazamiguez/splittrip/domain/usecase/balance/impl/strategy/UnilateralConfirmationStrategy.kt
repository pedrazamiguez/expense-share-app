package es.pedrazamiguez.splittrip.domain.usecase.balance.impl.strategy

import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.SettlementStatus
import java.time.LocalDateTime

class UnilateralConfirmationStrategy : SettlementConfirmationStrategy {
    override fun confirm(record: SettlementRecord, currentUserId: String, isCreator: Boolean): SettlementRecord {
        val isUnregisteredPayer = record.settlement.fromUserId.startsWith("pending_")
        val isUnregisteredPayee = record.settlement.toUserId.startsWith("pending_")

        return when (record.status) {
            SettlementStatus.SUGGESTED -> handleSuggested(
                record,
                currentUserId,
                isUnregisteredPayer,
                isUnregisteredPayee
            )
            SettlementStatus.CONFIRMED_BY_PAYER -> handleConfirmedByPayer(
                record,
                currentUserId,
                isUnregisteredPayer,
                isUnregisteredPayee
            )
            SettlementStatus.DISPUTED -> handleDisputed(record, currentUserId, isCreator)
            SettlementStatus.RESOLVED -> error("Settlement already resolved: ${record.id}")
        }
    }

    private fun handleSuggested(
        record: SettlementRecord,
        currentUserId: String,
        isUnregisteredPayer: Boolean,
        isUnregisteredPayee: Boolean
    ): SettlementRecord {
        val fromUserId = record.settlement.fromUserId
        val toUserId = record.settlement.toUserId
        val now = LocalDateTime.now()

        if (isUnregisteredPayee) {
            require(currentUserId == fromUserId) {
                "Only registered payer can confirm in SUGGESTED state"
            }
            return record.copy(
                status = SettlementStatus.RESOLVED,
                confirmedByPayerAt = now,
                resolvedAt = now
            )
        } else if (isUnregisteredPayer) {
            require(currentUserId == toUserId) {
                "Only registered payee can confirm in SUGGESTED state"
            }
            return record.copy(
                status = SettlementStatus.RESOLVED,
                confirmedByPayeeAt = now,
                resolvedAt = now
            )
        }
        error("UnilateralConfirmationStrategy called but neither party is unregistered")
    }

    private fun handleConfirmedByPayer(
        record: SettlementRecord,
        currentUserId: String,
        isUnregisteredPayer: Boolean,
        isUnregisteredPayee: Boolean
    ): SettlementRecord {
        val fromUserId = record.settlement.fromUserId
        val toUserId = record.settlement.toUserId
        val now = LocalDateTime.now()

        if (isUnregisteredPayee) {
            require(currentUserId == fromUserId) {
                "Only registered payer can re-confirm in CONFIRMED_BY_PAYER state"
            }
            return record.copy(
                status = SettlementStatus.RESOLVED,
                confirmedByPayerAt = now,
                resolvedAt = now
            )
        } else if (isUnregisteredPayer) {
            require(currentUserId == toUserId) {
                "Only registered payee can confirm in CONFIRMED_BY_PAYER state"
            }
            return record.copy(
                status = SettlementStatus.RESOLVED,
                confirmedByPayeeAt = now,
                resolvedAt = now
            )
        }
        error("UnilateralConfirmationStrategy called but neither party is unregistered")
    }

    private fun handleDisputed(record: SettlementRecord, currentUserId: String, isCreator: Boolean): SettlementRecord {
        val isPayee = record.settlement.toUserId == currentUserId
        require(isPayee || isCreator) {
            "Only payee or group creator can confirm in DISPUTED state"
        }
        val now = LocalDateTime.now()
        return record.copy(
            status = SettlementStatus.RESOLVED,
            confirmedByPayeeAt = now,
            resolvedAt = now
        )
    }
}
