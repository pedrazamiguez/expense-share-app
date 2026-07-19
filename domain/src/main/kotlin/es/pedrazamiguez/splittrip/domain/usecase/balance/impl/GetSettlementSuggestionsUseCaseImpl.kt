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
import es.pedrazamiguez.splittrip.domain.usecase.balance.PhysicalContributionAttributionStrategy
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

        val existingRecords = settlementRepository.getGroupSettlements(groupId)

        val memberBalances = getMemberBalancesFlowUseCase.computeMemberBalances(
            contributions = contributions,
            withdrawals = withdrawals,
            expenses = expenses,
            subunits = subunits,
            groupMemberIds = group.members,
            groupCurrency = group.currency,
            settlements = existingRecords,
            attributionStrategy = PhysicalContributionAttributionStrategy
        )

        val computedSettlements = debtSimplificationService.simplifyByPocket(
            memberBalances,
            group.currency
        ).filter { it.sourcePocket != SettlementPocketType.NET }

        reconcileSettlements(groupId, computedSettlements, existingRecords)
        purgeObsoleteSuggested(computedSettlements, existingRecords)

        return settlementRepository.getGroupSettlements(groupId)
    }

    private suspend fun reconcileSettlements(
        groupId: String,
        computedSettlements: List<Settlement>,
        existingRecords: List<SettlementRecord>
    ) {
        val deletedIds = deduplicateExistingRecords(existingRecords)
        val activeRecords = existingRecords.filter { it.id !in deletedIds }

        for (settlement in computedSettlements) {
            val existing = activeRecords.find { existing ->
                existing.status != SettlementStatus.RESOLVED &&
                    existing.settlement.fromUserId == settlement.fromUserId &&
                    existing.settlement.toUserId == settlement.toUserId &&
                    existing.settlement.sourcePocket == settlement.sourcePocket &&
                    existing.settlement.currency == settlement.currency
            }

            if (existing != null) {
                if (existing.status == SettlementStatus.SUGGESTED && existing.settlement != settlement) {
                    settlementRepository.updateSettlement(existing.copy(settlement = settlement))
                } else if (existing.status == SettlementStatus.DISPUTED && existing.settlement != settlement) {
                    settlementRepository.updateSettlement(
                        existing.copy(
                            settlement = settlement,
                            status = SettlementStatus.SUGGESTED,
                            disputedBy = null,
                            disputeReason = null
                        )
                    )
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
    }

    private suspend fun deduplicateExistingRecords(existingRecords: List<SettlementRecord>): Set<String> {
        val existingByKey = existingRecords.groupBy { record ->
            SettlementKey(
                fromUserId = record.settlement.fromUserId,
                toUserId = record.settlement.toUserId,
                sourcePocket = record.settlement.sourcePocket,
                currency = record.settlement.currency
            )
        }
        val deletedIds = mutableSetOf<String>()

        for ((_, records) in existingByKey) {
            val uncompleted = records.filter { it.status != SettlementStatus.RESOLVED }
            if (uncompleted.size > 1) {
                val primary = uncompleted.firstOrNull { it.status != SettlementStatus.SUGGESTED }
                    ?: uncompleted.first()
                val duplicates = uncompleted.filter { it.id != primary.id && it.status == SettlementStatus.SUGGESTED }
                for (dup in duplicates) {
                    settlementRepository.deleteSettlement(dup)
                    deletedIds.add(dup.id)
                }
            }
        }
        return deletedIds
    }

    private data class SettlementKey(
        val fromUserId: String,
        val toUserId: String,
        val sourcePocket: SettlementPocketType,
        val currency: String
    )

    private suspend fun purgeObsoleteSuggested(
        computedSettlements: List<Settlement>,
        existingRecords: List<SettlementRecord>
    ) {
        val obsoleteSuggested = existingRecords.filter { existing ->
            existing.status == SettlementStatus.SUGGESTED &&
                computedSettlements.none { comp ->
                    comp.fromUserId == existing.settlement.fromUserId &&
                        comp.toUserId == existing.settlement.toUserId &&
                        comp.sourcePocket == existing.settlement.sourcePocket &&
                        comp.currency == existing.settlement.currency
                }
        }
        for (staleRecord in obsoleteSuggested) {
            settlementRepository.deleteSettlement(staleRecord)
        }
    }
}
