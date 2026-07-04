package es.pedrazamiguez.splittrip.domain.usecase.balance

import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.usecase.UseCase
import kotlinx.coroutines.flow.Flow

interface GetGroupSettlementsFlowUseCase : UseCase {
    operator fun invoke(groupId: String): Flow<List<SettlementRecord>>
}
