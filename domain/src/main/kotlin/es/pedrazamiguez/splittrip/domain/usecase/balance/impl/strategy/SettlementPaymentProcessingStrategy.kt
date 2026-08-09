package es.pedrazamiguez.splittrip.domain.usecase.balance.impl.strategy

import es.pedrazamiguez.splittrip.domain.model.Group
import es.pedrazamiguez.splittrip.domain.model.SettlementPocketType
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord

/**
 * Strategy pattern to determine how a resolved settlement is processed in the persistent storage layer.
 */
interface SettlementPaymentProcessingStrategy {

    /**
     * @return true if this strategy applies to the given pocket type.
     */
    fun appliesTo(sourcePocket: SettlementPocketType): Boolean

    /**
     * Processes the persistence layer changes needed when a settlement is confirmed as RESOLVED.
     */
    suspend fun processPayment(
        record: SettlementRecord,
        updated: SettlementRecord,
        group: Group,
        groupId: String,
        currentUserId: String
    )
}
