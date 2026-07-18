package es.pedrazamiguez.splittrip.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import es.pedrazamiguez.splittrip.data.local.entity.MembershipRemovalEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MembershipRemovalEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: MembershipRemovalEventEntity)

    @Query(
        "SELECT * FROM membership_removal_events " +
            "WHERE groupId = :groupId AND processed = 0 ORDER BY createdAtMillis ASC"
    )
    fun getUnprocessedEventsFlow(groupId: String): Flow<List<MembershipRemovalEventEntity>>

    @Query("UPDATE membership_removal_events SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: String)

    @Query("UPDATE membership_removal_events SET processed = 1 WHERE id = :id")
    suspend fun markProcessed(id: String)
}
