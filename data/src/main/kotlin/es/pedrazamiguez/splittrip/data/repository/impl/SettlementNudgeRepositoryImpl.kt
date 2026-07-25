package es.pedrazamiguez.splittrip.data.repository.impl

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import es.pedrazamiguez.splittrip.data.local.datastore.SettlementNudgePreferences
import es.pedrazamiguez.splittrip.domain.datasource.local.LocalSettlementDataSource
import es.pedrazamiguez.splittrip.domain.repository.SettlementNudgeRepository
import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class SettlementNudgeRepositoryImpl(
    private val settlementNudgePreferences: SettlementNudgePreferences,
    private val firestore: FirebaseFirestore,
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

        val nudgeId = UUID.randomUUID().toString()
        val nudgeDocument = hashMapOf(
            "id" to nudgeId,
            "settlementId" to settlementId,
            "groupId" to groupId,
            "fromUserId" to currentUserId,
            "toUserId" to settlementRecord.settlement.fromUserId,
            "createdAt" to Timestamp.now()
        )

        firestore
            .collection("groups")
            .document(groupId)
            .collection("nudges")
            .document(nudgeId)
            .set(nudgeDocument)
            .await()
    }
}
