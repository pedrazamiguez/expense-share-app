package es.pedrazamiguez.splittrip.domain.service.impl.strategy

import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.domain.model.Settlement
import es.pedrazamiguez.splittrip.domain.model.SettlementPocketType

interface SettlementReconciliationStrategy {
    fun appliesTo(sourcePocket: SettlementPocketType): Boolean

    fun apply(
        balanceMap: MutableMap<String, MemberBalance>,
        settlement: Settlement,
        fromUser: MemberBalance,
        toUser: MemberBalance,
        groupCurrency: String
    )
}
