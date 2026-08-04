package es.pedrazamiguez.splittrip.domain.usecase.balance.impl.strategy

import es.pedrazamiguez.splittrip.domain.model.CashTransfer
import es.pedrazamiguez.splittrip.domain.model.Group
import es.pedrazamiguez.splittrip.domain.model.SettlementPocketType
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.repository.CashTransferRepository
import java.util.UUID

class CashSettlementPaymentStrategy(
    private val cashTransferRepository: CashTransferRepository
) : SettlementPaymentProcessingStrategy {

    override fun appliesTo(sourcePocket: SettlementPocketType): Boolean {
        return sourcePocket == SettlementPocketType.CASH
    }

    override suspend fun processPayment(
        record: SettlementRecord,
        updated: SettlementRecord,
        group: Group,
        groupId: String,
        currentUserId: String
    ) {
        // For CASH settlements, the payer is transferring existing physical group cash.
        // We persist a CashTransfer event so the reconciliation service can zero-sum
        // the physical cash debt in subsequent balance recalculations.
        val transfer = CashTransfer(
            id = UUID.randomUUID().toString(),
            groupId = groupId,
            fromUserId = record.settlement.fromUserId,
            toUserId = record.settlement.toUserId,
            amountCents = record.settlement.amount,
            currency = record.settlement.currency,
            // equivalentBaseAmountCents uses the raw amount as a safe default;
            // the reconciliation service uses the user's stored exchange rate for conversion.
            equivalentBaseAmountCents = record.settlement.amount,
            createdAt = System.currentTimeMillis()
        )
        cashTransferRepository.addTransfer(transfer)
    }
}
