package es.pedrazamiguez.splittrip.domain.model

data class GroupDashboardReadModel(
    val group: Group,
    val contributions: List<Contribution>,
    val withdrawals: List<CashWithdrawal>,
    val subunits: List<Subunit>,
    val expenses: List<Expense>,
    val settlements: List<SettlementRecord>
)
