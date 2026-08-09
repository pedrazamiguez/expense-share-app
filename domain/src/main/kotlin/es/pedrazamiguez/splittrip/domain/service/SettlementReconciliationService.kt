package es.pedrazamiguez.splittrip.domain.service

import es.pedrazamiguez.splittrip.domain.model.CashTransfer
import es.pedrazamiguez.splittrip.domain.model.CashWithdrawal
import es.pedrazamiguez.splittrip.domain.model.Contribution
import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord

interface SettlementReconciliationService {
    fun applyResolvedSettlements(
        balances: List<MemberBalance>,
        settlements: List<SettlementRecord>,
        cashTransfers: List<CashTransfer>,
        contributions: List<Contribution>,
        withdrawals: List<CashWithdrawal>,
        groupCurrency: String
    ): List<MemberBalance>
}
