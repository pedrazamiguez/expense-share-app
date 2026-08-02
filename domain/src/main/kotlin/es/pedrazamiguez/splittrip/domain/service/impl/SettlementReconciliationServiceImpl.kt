package es.pedrazamiguez.splittrip.domain.service.impl

import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.domain.model.Settlement
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.SettlementStatus
import es.pedrazamiguez.splittrip.domain.service.SettlementReconciliationService
import es.pedrazamiguez.splittrip.domain.service.settlement.CashSettlementReconciliationStrategy
import es.pedrazamiguez.splittrip.domain.service.settlement.PocketSettlementReconciliationStrategy

class SettlementReconciliationServiceImpl : SettlementReconciliationService {

    private val strategies = listOf(
        PocketSettlementReconciliationStrategy(),
        CashSettlementReconciliationStrategy()
    )

    override fun applyResolvedSettlements(
        balances: List<MemberBalance>,
        settlements: List<SettlementRecord>,
        groupCurrency: String
    ): List<MemberBalance> {
        val resolvedSettlements = settlements.filter { it.status == SettlementStatus.RESOLVED }
        if (resolvedSettlements.isEmpty()) return balances

        val balanceMap = balances.associateBy { it.userId }.toMutableMap()

        for (record in resolvedSettlements) {
            applySettlementRecord(balanceMap, record.settlement, groupCurrency)
        }

        return balances.map { balanceMap[it.userId]!! }
    }

    private fun applySettlementRecord(
        balanceMap: MutableMap<String, MemberBalance>,
        settlement: Settlement,
        groupCurrency: String
    ) {
        val fromUser = balanceMap[settlement.fromUserId] ?: return
        val toUser = balanceMap[settlement.toUserId] ?: return

        val strategy = strategies.firstOrNull { it.appliesTo(settlement.sourcePocket) }
        strategy?.apply(balanceMap, settlement, fromUser, toUser, groupCurrency)
    }
}
