package es.pedrazamiguez.splittrip.domain.service.settlement

import es.pedrazamiguez.splittrip.domain.model.CashWithdrawal
import es.pedrazamiguez.splittrip.domain.model.Contribution
import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.domain.model.SettlementPocketType
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord

interface SettlementReconciliationStrategy {
    fun appliesTo(sourcePocket: SettlementPocketType): Boolean

    fun apply(
        balanceMap: MutableMap<String, MemberBalance>,
        record: SettlementRecord,
        fromUser: MemberBalance,
        toUser: MemberBalance,
        contributions: List<Contribution>,
        withdrawals: List<CashWithdrawal>,
        groupCurrency: String
    )
}
