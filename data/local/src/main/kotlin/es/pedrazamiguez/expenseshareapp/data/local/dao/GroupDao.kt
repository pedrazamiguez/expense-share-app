package es.pedrazamiguez.expenseshareapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import es.pedrazamiguez.expenseshareapp.data.local.entity.GroupEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Group entities.
 * Provides reactive access to locally stored groups using Flow.
 */
@Dao
interface GroupDao {

    /**
     * Observes all groups in the database.
     * This Flow emits automatically whenever the groups table changes.
     * This is the foundation of the Offline-First pattern - UI always reads from here.
     */
    @Query("SELECT * FROM groups ORDER BY lastUpdatedAtMillis DESC")
    fun getAllGroupsFlow(): Flow<List<GroupEntity>>

    /**
     * Gets a single group by ID.
     */
    @Query("SELECT * FROM groups WHERE id = :groupId")
    suspend fun getGroupById(groupId: String): GroupEntity?

    /**
     * Observes a single group by ID.
     */
    @Query("SELECT * FROM groups WHERE id = :groupId")
    fun getGroupByIdFlow(groupId: String): Flow<GroupEntity?>

    /**
     * Inserts or updates groups.
     * Uses @Upsert to perform a true UPDATE if the ID exists, INSERT if not.
     * This prevents DELETE+INSERT behavior of REPLACE, which would trigger
     * CASCADE deletion of related expenses via ForeignKey constraints.
     */
    @Upsert
    suspend fun insertGroups(groups: List<GroupEntity>)

    /**
     * Inserts or updates a single group.
     */
    @Upsert
    suspend fun insertGroup(group: GroupEntity)

    /**
     * Deletes all groups from the table.
     */
    @Query("DELETE FROM groups")
    suspend fun clearAllGroups()

    /**
     * Deletes a specific group by ID.
     */
    @Query("DELETE FROM groups WHERE id = :groupId")
    suspend fun deleteGroup(groupId: String)

    /**
     * Replaces all groups atomically.
     * Useful for full sync operations where we want to ensure consistency.
     */
    @Transaction
    suspend fun replaceAllGroups(groups: List<GroupEntity>) {
        clearAllGroups()
        insertGroups(groups)
    }
}
