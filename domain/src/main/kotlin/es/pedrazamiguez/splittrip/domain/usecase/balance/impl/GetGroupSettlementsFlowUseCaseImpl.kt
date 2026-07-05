package es.pedrazamiguez.splittrip.domain.usecase.balance.impl

import es.pedrazamiguez.splittrip.domain.model.SettlementRecord
import es.pedrazamiguez.splittrip.domain.repository.SettlementRepository
import es.pedrazamiguez.splittrip.domain.usecase.balance.GetGroupSettlementsFlowUseCase
import kotlinx.coroutines.flow.Flow

class GetGroupSettlementsFlowUseCaseImpl(
    private val settlementRepository: SettlementRepository
) : GetGroupSettlementsFlowUseCase {

    override operator fun invoke(groupId: String): Flow<List<SettlementRecord>> =
        settlementRepository.getGroupSettlementsFlow(groupId)
}
