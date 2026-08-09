package es.pedrazamiguez.splittrip.domain.model

data class BalancesDashboardDomainModel(
    val balance: GroupPocketBalance,
    val contributions: List<Contribution>,
    val withdrawals: List<CashWithdrawal>,
    val subunits: List<Subunit>,
    val expenses: List<Expense>,
    val settlements: List<SettlementRecord>,
    val memberBalances: List<MemberBalance>,
    val settlementSuggestions: List<Settlement>,
    val memberProfiles: Map<String, User>
)
