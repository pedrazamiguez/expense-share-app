package es.pedrazamiguez.splittrip.data.repository.impl

import es.pedrazamiguez.splittrip.data.local.datastore.SettlementNudgePreferences
import es.pedrazamiguez.splittrip.domain.datasource.cloud.CloudSettlementDataSource
import es.pedrazamiguez.splittrip.domain.datasource.local.LocalSettlementDataSource
import es.pedrazamiguez.splittrip.domain.repository.SettlementNudgeRepository
import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import kotlinx.coroutines.flow.Flow

class SettlementNudgeRepositoryImpl(
    private val settlementNudgePreferences: SettlementNudgePreferences,
    private val cloudSettlementDataSource: CloudSettlementDataSource,
    private val localSettlementDataSource: LocalSettlementDataSource,
    private val authenticationService: AuthenticationService
) : SettlementNudgeRepository {

    override fun getNudgeTimestampsFlow(): Flow<Map<String, Long>> =
        settlementNudgePreferences.nudgeTimestampsFlow

    override suspend fun getLastNudgeTimestamp(settlementId: String): Long =
        settlementNudgePreferences.getLastNudgeTimestamp(settlementId)

    override suspend fun recordNudgeTimestamp(settlementId: String, timestamp: Long) =
        settlementNudgePreferences.recordNudgeTimestamp(settlementId, timestamp)

    override suspend fun sendDebtorNudge(groupId: String, settlementId: String): Result<Unit> = runCatching {
        val currentUserId = checkNotNull(authenticationService.currentUserId()) {
            "User not authenticated"
        }

        val settlementRecord = requireNotNull(localSettlementDataSource.getSettlementById(settlementId)) {
            "Settlement not found: $settlementId"
        }

        cloudSettlementDataSource.sendDebtorNudge(
            groupId = groupId,
            settlementId = settlementId,
            fromUserId = currentUserId,
            toUserId = settlementRecord.settlement.fromUserId
        )
    }
}
