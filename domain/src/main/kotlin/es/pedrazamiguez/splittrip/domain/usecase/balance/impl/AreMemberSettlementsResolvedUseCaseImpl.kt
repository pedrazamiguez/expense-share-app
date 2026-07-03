package es.pedrazamiguez.splittrip.domain.usecase.balance.impl

import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.SettlementStatus
import es.pedrazamiguez.splittrip.domain.repository.SettlementRepository
import es.pedrazamiguez.splittrip.domain.usecase.balance.AreMemberSettlementsResolvedUseCase

class AreMemberSettlementsResolvedUseCaseImpl(
    private val settlementRepository: SettlementRepository
) : AreMemberSettlementsResolvedUseCase {

    override suspend operator fun invoke(groupId: String, userId: String): List<SettlementRecord> =
        settlementRepository
            .getMemberSettlements(groupId, userId)
            .filter { it.status != SettlementStatus.RESOLVED }
}
