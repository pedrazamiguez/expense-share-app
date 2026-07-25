package es.pedrazamiguez.splittrip.domain.usecase.settlement.impl

import es.pedrazamiguez.splittrip.domain.repository.SettlementNudgeRepository
import es.pedrazamiguez.splittrip.domain.usecase.settlement.GetNudgeTimestampsFlowUseCase
import kotlinx.coroutines.flow.Flow

class GetNudgeTimestampsFlowUseCaseImpl(
    private val settlementNudgeRepository: SettlementNudgeRepository
) : GetNudgeTimestampsFlowUseCase {

    override fun invoke(): Flow<Map<String, Long>> =
        settlementNudgeRepository.getNudgeTimestampsFlow()
}
