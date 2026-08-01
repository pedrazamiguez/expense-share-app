package es.pedrazamiguez.splittrip.domain.usecase.balance.impl

import es.pedrazamiguez.splittrip.domain.enums.PayerType
import es.pedrazamiguez.splittrip.domain.model.Contribution
import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.SettlementStatus
import es.pedrazamiguez.splittrip.domain.repository.ContributionRepository
import es.pedrazamiguez.splittrip.domain.repository.GroupRepository
import es.pedrazamiguez.splittrip.domain.repository.SettlementRepository
import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import es.pedrazamiguez.splittrip.domain.usecase.balance.ConfirmSettlementUseCase
import java.time.LocalDateTime
import java.util.UUID

class ConfirmSettlementUseCaseImpl(
    private val settlementRepository: SettlementRepository,
    private val authenticationService: AuthenticationService,
    private val groupRepository: GroupRepository,
    private val contributionRepository: ContributionRepository
) : ConfirmSettlementUseCase {

    override suspend operator fun invoke(
        groupId: String,
        settlementId: String
    ): Result<SettlementRecord> = runCatching {
        val currentUserId = authenticationService.requireUserId()
        val record = settlementRepository.getSettlementById(settlementId)
            ?: throw IllegalArgumentException("Settlement not found: $settlementId")

        val updated = when (record.status) {
            SettlementStatus.SUGGESTED -> {
                val fromUserId = record.settlement.fromUserId
                require(currentUserId == fromUserId) { "Only payer can confirm in SUGGESTED state" }
                val now = LocalDateTime.now()
                record.copy(
                    status = SettlementStatus.CONFIRMED_BY_PAYER,
                    confirmedByPayerAt = now
                )
            }
            SettlementStatus.CONFIRMED_BY_PAYER -> {
                val toUserId = record.settlement.toUserId
                require(currentUserId == toUserId) { "Only payee can confirm in CONFIRMED_BY_PAYER state" }
                val now = LocalDateTime.now()
                record.copy(
                    status = SettlementStatus.RESOLVED,
                    confirmedByPayeeAt = now,
                    resolvedAt = now
                )
            }
            SettlementStatus.DISPUTED -> {
                val group = groupRepository.getGroupById(groupId)
                    ?: throw IllegalArgumentException("Group not found: $groupId")
                val isPayee = record.settlement.toUserId == currentUserId
                val isCreator = group.createdBy == currentUserId
                require(isPayee || isCreator) { "Only payee or group creator can confirm in DISPUTED state" }
                val now = LocalDateTime.now()
                record.copy(
                    status = SettlementStatus.RESOLVED,
                    confirmedByPayeeAt = now,
                    resolvedAt = now
                )
            }
            SettlementStatus.RESOLVED -> {
                error("Settlement already resolved: $settlementId")
            }
        }

        settlementRepository.updateSettlement(updated)

        if (updated.status == SettlementStatus.RESOLVED) {
            val settlementContribution = Contribution(
                id = UUID.randomUUID().toString(),
                groupId = groupId,
                userId = record.settlement.fromUserId,
                createdBy = currentUserId,
                contributionScope = PayerType.USER,
                amount = record.settlement.amount,
                currency = record.settlement.currency,
                linkedSettlementId = record.id,
                createdAt = updated.resolvedAt ?: LocalDateTime.now()
            )
            contributionRepository.addContribution(groupId, settlementContribution)
        }

        updated
    }
}
