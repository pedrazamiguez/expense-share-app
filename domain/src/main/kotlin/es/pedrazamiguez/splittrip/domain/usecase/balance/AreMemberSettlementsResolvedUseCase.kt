package es.pedrazamiguez.splittrip.domain.usecase.balance

import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.usecase.UseCase

interface AreMemberSettlementsResolvedUseCase : UseCase {
    suspend operator fun invoke(groupId: String, userId: String): List<SettlementRecord>
}
