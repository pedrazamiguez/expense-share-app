package es.pedrazamiguez.splittrip.data.datasource

import es.pedrazamiguez.splittrip.domain.datasource.GroupDashboardDataSource
import es.pedrazamiguez.splittrip.domain.model.GroupDashboardReadModel
import es.pedrazamiguez.splittrip.domain.repository.CashTransferRepository
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
    private val subunitRepository: SubunitRepository,
    private val cashTransferRepository: CashTransferRepository
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
                settlementRepository.getGroupSettlementsFlow(groupId),
                cashTransferRepository.observeGroupCashTransfers(groupId)
            ) { s, e, set, ct -> Tuple4(s, e, set, ct) }
        ) { (group, contributions, withdrawals), other ->
            GroupDashboardReadModel(
                group = group,
                contributions = contributions,
                withdrawals = withdrawals,
                subunits = other.t1,
                expenses = other.t2,
                settlements = other.t3,
                cashTransfers = other.t4
            )
        }
    }
}

internal data class Tuple4<T1, T2, T3, T4>(val t1: T1, val t2: T2, val t3: T3, val t4: T4)
