package es.pedrazamiguez.splittrip.domain.service.settlement

import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.domain.model.Settlement
import es.pedrazamiguez.splittrip.domain.model.SettlementPocketType

class PocketSettlementReconciliationStrategy : SettlementReconciliationStrategy {

    override fun appliesTo(sourcePocket: SettlementPocketType): Boolean {
        return sourcePocket == SettlementPocketType.POCKET || sourcePocket == SettlementPocketType.NET
    }

    override fun apply(
        balanceMap: MutableMap<String, MemberBalance>,
        settlement: Settlement,
        fromUser: MemberBalance,
        toUser: MemberBalance,
        groupCurrency: String
    ) {
        val isAlreadyMaterialized = fromUser.contributed >= settlement.amount
        if (!isAlreadyMaterialized) {
            balanceMap[settlement.fromUserId] = fromUser.copy(
                contributed = fromUser.contributed + settlement.amount,
                pocketBalance = fromUser.pocketBalance + settlement.amount
            )
        }

        balanceMap[settlement.toUserId] = toUser.copy(
            withdrawn = toUser.withdrawn + settlement.amount,
            pocketBalance = toUser.pocketBalance - settlement.amount
        )
    }
}
