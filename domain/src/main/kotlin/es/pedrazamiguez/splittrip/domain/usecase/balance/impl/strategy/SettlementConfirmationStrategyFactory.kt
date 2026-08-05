package es.pedrazamiguez.splittrip.domain.usecase.balance.impl.strategy

import es.pedrazamiguez.splittrip.domain.model.SettlementRecord

object SettlementConfirmationStrategyFactory {
    fun getStrategy(record: SettlementRecord): SettlementConfirmationStrategy {
        val isUnregisteredPayer = record.settlement.fromUserId.startsWith("pending_")
        val isUnregisteredPayee = record.settlement.toUserId.startsWith("pending_")

        return if (isUnregisteredPayer || isUnregisteredPayee) {
            UnilateralConfirmationStrategy()
        } else {
            MutualConfirmationStrategy()
        }
    }
}
