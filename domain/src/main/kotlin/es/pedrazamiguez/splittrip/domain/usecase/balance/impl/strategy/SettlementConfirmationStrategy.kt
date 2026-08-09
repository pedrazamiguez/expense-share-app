package es.pedrazamiguez.splittrip.domain.usecase.balance.impl.strategy

import es.pedrazamiguez.splittrip.domain.model.SettlementRecord

interface SettlementConfirmationStrategy {
    fun confirm(record: SettlementRecord, currentUserId: String, isCreator: Boolean): SettlementRecord
}
