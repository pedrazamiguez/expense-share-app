package es.pedrazamiguez.splittrip.domain.usecase.balance.impl

import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.model.SettlementStatus
import es.pedrazamiguez.splittrip.domain.repository.SettlementRepository
import es.pedrazamiguez.splittrip.domain.usecase.balance.AreGroupSettlementsResolvedUseCase

class AreGroupSettlementsResolvedUseCaseImpl(
    private val settlementRepository: SettlementRepository
) : AreGroupSettlementsResolvedUseCase {

    override suspend operator fun invoke(groupId: String): List<SettlementRecord> =
        settlementRepository
            .getGroupSettlements(groupId)
            .filter { it.status != SettlementStatus.RESOLVED }
}
