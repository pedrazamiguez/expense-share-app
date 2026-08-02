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
        // For CASH settlements, we are transferring existing physical group cash.
        // We DO NOT create a paired contribution, because the Payer is not introducing new funds.
        // The reconciliation service will zero-sum the physical cash debt.
    }
}
