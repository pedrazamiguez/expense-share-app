package es.pedrazamiguez.splittrip.domain.service

import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord

interface SettlementReconciliationService {
    fun applyResolvedSettlements(
        balances: List<MemberBalance>,
        settlements: List<SettlementRecord>,
        groupCurrency: String
    ): List<MemberBalance>
}
