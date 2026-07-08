package es.pedrazamiguez.splittrip.domain.usecase.balance.impl

import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.SettlementStatus
import es.pedrazamiguez.splittrip.domain.repository.SettlementRepository
import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import es.pedrazamiguez.splittrip.domain.usecase.balance.DisputeSettlementUseCase

class DisputeSettlementUseCaseImpl(
    private val settlementRepository: SettlementRepository,
    private val authenticationService: AuthenticationService
) : DisputeSettlementUseCase {

    override suspend operator fun invoke(
        groupId: String,
        settlementId: String,
        reason: String
    ): Result<SettlementRecord> = runCatching {
        val currentUserId = authenticationService.requireUserId()
        val record = settlementRepository.getSettlementById(settlementId)
            ?: throw IllegalArgumentException("Settlement not found: $settlementId")

        val isPayer = record.settlement.fromUserId == currentUserId
        val isPayee = record.settlement.toUserId == currentUserId
        require(isPayer || isPayee) { "Only settlement parties can dispute" }

        require(record.status != SettlementStatus.RESOLVED) {
            "Cannot dispute a resolved settlement: $settlementId"
        }
        require(record.status != SettlementStatus.DISPUTED) {
            "Settlement already disputed: $settlementId"
        }

        val updated = record.copy(
            status = SettlementStatus.DISPUTED,
            disputedBy = currentUserId,
            disputeReason = reason
        )

        settlementRepository.updateSettlement(updated)
        updated
    }
}
