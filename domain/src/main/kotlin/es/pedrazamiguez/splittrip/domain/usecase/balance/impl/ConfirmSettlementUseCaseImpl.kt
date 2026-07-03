package es.pedrazamiguez.splittrip.domain.usecase.balance.impl

import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.SettlementStatus
import es.pedrazamiguez.splittrip.domain.repository.SettlementRepository
import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import es.pedrazamiguez.splittrip.domain.usecase.balance.ConfirmSettlementUseCase
import java.time.LocalDateTime

class ConfirmSettlementUseCaseImpl(
    private val settlementRepository: SettlementRepository,
    private val authenticationService: AuthenticationService
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
            SettlementStatus.CONFIRMED_BY_BOTH -> {
                error("Settlement already confirmed by both parties: $settlementId")
            }
            SettlementStatus.DISPUTED -> {
                error("Cannot confirm disputed settlement: $settlementId")
            }
            SettlementStatus.RESOLVED -> {
                error("Settlement already resolved: $settlementId")
            }
        }

        settlementRepository.updateSettlement(updated)
        updated
    }
}
