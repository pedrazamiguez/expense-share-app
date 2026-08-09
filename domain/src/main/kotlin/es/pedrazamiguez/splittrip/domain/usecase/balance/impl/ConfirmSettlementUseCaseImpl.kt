package es.pedrazamiguez.splittrip.domain.usecase.balance.impl

import es.pedrazamiguez.splittrip.domain.datasource.GroupDashboardDataSource
import es.pedrazamiguez.splittrip.domain.model.Group
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.SettlementStatus
import es.pedrazamiguez.splittrip.domain.repository.ContributionRepository
import es.pedrazamiguez.splittrip.domain.repository.GroupRepository
import es.pedrazamiguez.splittrip.domain.repository.SettlementRepository
import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import es.pedrazamiguez.splittrip.domain.usecase.balance.ConfirmSettlementUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetMemberBalancesFlowUseCase
import es.pedrazamiguez.splittrip.domain.usecase.balance.impl.strategy.CashSettlementPaymentStrategy
import es.pedrazamiguez.splittrip.domain.usecase.balance.impl.strategy.PocketSettlementPaymentStrategy
import es.pedrazamiguez.splittrip.domain.usecase.balance.impl.strategy.SettlementConfirmationStrategyFactory

class ConfirmSettlementUseCaseImpl(
    private val settlementRepository: SettlementRepository,
    private val authenticationService: AuthenticationService,
    private val groupRepository: GroupRepository,
    private val contributionRepository: ContributionRepository,
    private val groupDashboardDataSource: GroupDashboardDataSource,
    private val getMemberBalancesFlowUseCase: GetMemberBalancesFlowUseCase
) : ConfirmSettlementUseCase {

    @Suppress("LongMethod")
    override suspend operator fun invoke(
        groupId: String,
        settlementId: String
    ): Result<SettlementRecord> = runCatching {
        val currentUserId = authenticationService.requireUserId()
        val record = settlementRepository.getSettlementById(settlementId)
            ?: throw IllegalArgumentException("Settlement not found: $settlementId")

        val group = groupRepository.getGroupById(groupId)
            ?: throw IllegalArgumentException("Group not found: $groupId")
        val isCreator = group.createdBy == currentUserId

        val strategy = SettlementConfirmationStrategyFactory.getStrategy(record)
        val updated = strategy.confirm(record, currentUserId, isCreator)

        settlementRepository.updateSettlement(updated)

        if (updated.status == SettlementStatus.RESOLVED) {
            handleResolvedSettlement(
                record = record,
                updated = updated,
                group = group,
                groupId = groupId,
                currentUserId = currentUserId
            )
        }

        updated
    }

    private suspend fun handleResolvedSettlement(
        record: SettlementRecord,
        updated: SettlementRecord,
        group: Group,
        groupId: String,
        currentUserId: String
    ) {
        val strategies = listOf(
            PocketSettlementPaymentStrategy(
                contributionRepository = contributionRepository,
                groupDashboardDataSource = groupDashboardDataSource,
                getMemberBalancesFlowUseCase = getMemberBalancesFlowUseCase
            ),
            CashSettlementPaymentStrategy()
        )

        val strategy = strategies.firstOrNull { it.appliesTo(record.settlement.sourcePocket) }
            ?: throw IllegalArgumentException(
                "No payment strategy found for pocket type: ${record.settlement.sourcePocket}"
            )

        strategy.processPayment(record, updated, group, groupId, currentUserId)
    }
}
