package es.pedrazamiguez.splittrip.domain.usecase.balance.impl

import es.pedrazamiguez.splittrip.domain.model.MemberBalance
import es.pedrazamiguez.splittrip.domain.model.Settlement
import es.pedrazamiguez.splittrip.domain.model.SettlementPocketType
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.SettlementStatus
import es.pedrazamiguez.splittrip.domain.repository.CashWithdrawalRepository
import es.pedrazamiguez.splittrip.domain.repository.ContributionRepository
import es.pedrazamiguez.splittrip.domain.repository.ExpenseRepository
import es.pedrazamiguez.splittrip.domain.repository.GroupRepository
import es.pedrazamiguez.splittrip.domain.repository.SettlementRepository
import es.pedrazamiguez.splittrip.domain.repository.SubunitRepository
import es.pedrazamiguez.splittrip.domain.service.DebtSimplificationService
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetMemberBalancesFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetSettlementSuggestionsUseCase
import java.time.LocalDateTime
import java.util.UUID
import kotlinx.coroutines.flow.first

class GetSettlementSuggestionsUseCaseImpl(
    private val debtSimplificationService: DebtSimplificationService,
    private val settlementRepository: SettlementRepository,
    private val groupRepository: GroupRepository,
    private val expenseRepository: ExpenseRepository,
    private val contributionRepository: ContributionRepository,
    private val cashWithdrawalRepository: CashWithdrawalRepository,
    private val subunitRepository: SubunitRepository,
    private val getMemberBalancesFlowUseCase: GetMemberBalancesFlowUseCase
) : GetSettlementSuggestionsUseCase {

    override operator fun invoke(memberBalances: List<MemberBalance>): List<Settlement> =
        debtSimplificationService.simplify(memberBalances)

    override fun invokeByPocket(
        memberBalances: List<MemberBalance>,
        groupCurrency: String
    ): List<Settlement> =
        debtSimplificationService.simplifyByPocket(memberBalances, groupCurrency)

    override suspend fun persistForGroup(groupId: String): List<SettlementRecord> {
        val group = groupRepository.getGroupById(groupId)
            ?: return emptyList()

        val expenses = expenseRepository.getGroupExpensesFlow(groupId).first()
        val contributions = contributionRepository.getGroupContributionsFlow(groupId).first()
        val withdrawals = cashWithdrawalRepository.getGroupWithdrawalsFlow(groupId).first()
        val subunits = subunitRepository.getGroupSubunits(groupId)

        val memberBalances = getMemberBalancesFlowUseCase.computeMemberBalances(
            contributions = contributions,
            withdrawals = withdrawals,
            expenses = expenses,
            subunits = subunits,
            groupMemberIds = group.members,
            groupCurrency = group.currency
        )

        val computedSettlements = debtSimplificationService.simplifyByPocket(
            memberBalances,
            group.currency
        ).filter { it.sourcePocket != SettlementPocketType.NET }

        val existingRecords = settlementRepository.getGroupSettlements(groupId)

        for (settlement in computedSettlements) {
            val existing = existingRecords.find { existing ->
                existing.status != SettlementStatus.RESOLVED &&
                    existing.settlement.fromUserId == settlement.fromUserId &&
                    existing.settlement.toUserId == settlement.toUserId
            }

            if (existing != null) {
                if (existing.status == SettlementStatus.SUGGESTED && existing.settlement != settlement) {
                    settlementRepository.updateSettlement(existing.copy(settlement = settlement))
                }
                continue
            }

            val newRecord = SettlementRecord(
                id = UUID.randomUUID().toString(),
                groupId = groupId,
                settlement = settlement,
                status = SettlementStatus.SUGGESTED,
                createdAt = LocalDateTime.now()
            )
            settlementRepository.addSettlement(newRecord)
        }

        return settlementRepository.getGroupSettlements(groupId)
    }
}
