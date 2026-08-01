package es.pedrazamiguez.splittrip.data.datasource

import es.pedrazamiguez.splittrip.domain.datasource.GroupDashboardDataSource
import es.pedrazamiguez.splittrip.domain.model.GroupDashboardReadModel
import es.pedrazamiguez.splittrip.domain.repository.CashWithdrawalRepository
import es.pedrazamiguez.splittrip.domain.repository.ContributionRepository
import es.pedrazamiguez.splittrip.domain.repository.ExpenseRepository
import es.pedrazamiguez.splittrip.domain.repository.GroupRepository
import es.pedrazamiguez.splittrip.domain.repository.SettlementRepository
import es.pedrazamiguez.splittrip.domain.repository.SubunitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull

class GroupDashboardDataSourceImpl(
    private val groupRepository: GroupRepository,
    private val contributionRepository: ContributionRepository,
    private val withdrawalRepository: CashWithdrawalRepository,
    private val expenseRepository: ExpenseRepository,
    private val settlementRepository: SettlementRepository,
    private val subunitRepository: SubunitRepository
) : GroupDashboardDataSource {
    override fun getDashboardSnapshotFlow(groupId: String): Flow<GroupDashboardReadModel> {
        return combine(
            combine(
                groupRepository.getGroupByIdFlow(groupId).filterNotNull(),
                contributionRepository.getGroupContributionsFlow(groupId),
                withdrawalRepository.getGroupWithdrawalsFlow(groupId)
            ) { g, c, w -> Triple(g, c, w) },
            combine(
                subunitRepository.getGroupSubunitsFlow(groupId),
                expenseRepository.getGroupExpensesFlow(groupId),
                settlementRepository.getGroupSettlementsFlow(groupId)
            ) { s, e, set -> Triple(s, e, set) }
        ) { (group, contributions, withdrawals), (subunits, expenses, settlements) ->
            GroupDashboardReadModel(
                group = group,
                contributions = contributions,
                withdrawals = withdrawals,
                subunits = subunits,
                expenses = expenses,
                settlements = settlements
            )
        }
    }
}
