package es.pedrazamiguez.splittrip.domain.usecase.balance.impl.strategy

import es.pedrazamiguez.splittrip.domain.model.Group
import es.pedrazamiguez.splittrip.domain.model.SettlementPocketType
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
class CashSettlementPaymentStrategy : SettlementPaymentProcessingStrategy {

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
        // We do not persist CashTransfer events because it creates an infinite feedback loop
        // with the DebtSimplificationService. CASH settlements are read-only in the UI.
        throw UnsupportedOperationException(
            "CASH settlements are informational and cannot be processed via the system."
        )
    }
}
