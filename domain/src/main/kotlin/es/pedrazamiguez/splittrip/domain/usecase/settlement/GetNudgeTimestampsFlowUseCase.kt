package es.pedrazamiguez.splittrip.domain.usecase.settlement

import kotlinx.coroutines.flow.Flow

interface GetNudgeTimestampsFlowUseCase {
    operator fun invoke(): Flow<Map<String, Long>>
}
