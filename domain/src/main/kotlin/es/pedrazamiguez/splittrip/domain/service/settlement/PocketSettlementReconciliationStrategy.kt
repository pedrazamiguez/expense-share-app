package es.pedrazamiguez.splittrip.domain.service.settlement

import es.pedrazamiguez.splittrip.domain.model.CashWithdrawal
import es.pedrazamiguez.splittrip.domain.model.Contribution
import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.domain.model.SettlementPocketType
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord

class PocketSettlementReconciliationStrategy : SettlementReconciliationStrategy {

    override fun appliesTo(sourcePocket: SettlementPocketType): Boolean {
        return sourcePocket == SettlementPocketType.POCKET || sourcePocket == SettlementPocketType.NET
    }

    override fun apply(
        balanceMap: MutableMap<String, MemberBalance>,
        record: SettlementRecord,
        fromUser: MemberBalance,
        toUser: MemberBalance,
        contributions: List<Contribution>,
        withdrawals: List<CashWithdrawal>,
        groupCurrency: String
    ) {
        // If a linked contribution exists, the settlement is already materialized in the pot.
        val isAlreadyMaterialized = contributions.any { it.linkedSettlementId == record.id }

        // If it is NOT materialized, we must apply the resolution virtually for BOTH users.
        // If it IS materialized, we do NOTHING (the pot absorbed the contribution).
        if (!isAlreadyMaterialized) {
            balanceMap[record.settlement.fromUserId] = fromUser.copy(
                contributed = fromUser.contributed + record.settlement.amount,
                pocketBalance = fromUser.pocketBalance + record.settlement.amount
            )
            balanceMap[record.settlement.toUserId] = toUser.copy(
                withdrawn = toUser.withdrawn + record.settlement.amount,
                pocketBalance = toUser.pocketBalance - record.settlement.amount
            )
        }
    }
}
