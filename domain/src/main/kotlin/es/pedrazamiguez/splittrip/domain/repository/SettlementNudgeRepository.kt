package es.pedrazamiguez.splittrip.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettlementNudgeRepository {
    fun getNudgeTimestampsFlow(): Flow<Map<String, Long>>
    suspend fun getLastNudgeTimestamp(settlementId: String): Long
    suspend fun recordNudgeTimestamp(settlementId: String, timestamp: Long)
    suspend fun sendDebtorNudge(groupId: String, settlementId: String): Result<Unit>
}
