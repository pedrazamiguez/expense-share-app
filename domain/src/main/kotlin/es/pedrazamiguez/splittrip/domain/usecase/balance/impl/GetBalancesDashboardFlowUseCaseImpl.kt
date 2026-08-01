package es.pedrazamiguez.splittrip.domain.usecase.balance.impl

import es.pedrazamiguez.splittrip.domain.datasource.GroupDashboardDataSource
import es.pedrazamiguez.splittrip.domain.model.BalancesDashboardDomainModel
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetBalancesDashboardFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetGroupPocketBalanceFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetMemberBalancesFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetSettlementSuggestionsUseCase
import es.pedrazamiguez.splittrip.domain.usecase.user.GetMemberProfilesUseCase
import kotlinx.coroutines.flow.Flow

class GetBalancesDashboardFlowUseCaseImpl(
    private val groupDashboardDataSource: GroupDashboardDataSource,
    private val getGroupPocketBalanceFlowUseCase: GetGroupPocketBalanceFlowUseCase,
    private val getMemberBalancesFlowUseCase: GetMemberBalancesFlowUseCase,
    private val getSettlementSuggestionsUseCase: GetSettlementSuggestionsUseCase,
    private val getMemberProfilesUseCase: GetMemberProfilesUseCase
) : GetBalancesDashboardFlowUseCase {
    override fun invoke(
        groupId: String,
        currency: String,
        groupMemberIds: List<String>
    ): Flow<BalancesDashboardDomainModel> {
        return kotlinx.coroutines.flow.combine(
            groupDashboardDataSource.getDashboardSnapshotFlow(groupId),
            getGroupPocketBalanceFlowUseCase(groupId, currency)
        ) { snapshot, balance ->
            val memberBalances = getMemberBalancesFlowUseCase.computeMemberBalances(
                contributions = snapshot.contributions,
                withdrawals = snapshot.withdrawals,
                expenses = snapshot.expenses,
                subunits = snapshot.subunits,
                groupMemberIds = groupMemberIds,
                groupCurrency = currency
            )

            val allUserIds = buildSet {
                addAll(groupMemberIds)
                snapshot.contributions.forEach { add(it.userId) }
                snapshot.withdrawals.forEach { add(it.withdrawnBy) }
                memberBalances.forEach { add(it.userId) }
            }.toList()
            val memberProfiles = getMemberProfilesUseCase(allUserIds)

            val settlementSuggestions = getSettlementSuggestionsUseCase(memberBalances)

            BalancesDashboardDomainModel(
                balance = balance,
                contributions = snapshot.contributions,
                withdrawals = snapshot.withdrawals,
                subunits = snapshot.subunits,
                expenses = snapshot.expenses,
                settlements = snapshot.settlements,
                memberBalances = memberBalances,
                settlementSuggestions = settlementSuggestions,
                memberProfiles = memberProfiles
            )
        }
    }
}
