package es.pedrazamiguez.expenseshareapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import es.pedrazamiguez.expenseshareapp.data.local.entity.ContributionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContributionDao {

    @Upsert
    suspend fun insertContribution(contribution: ContributionEntity)

    @Upsert
    suspend fun insertContributions(contributions: List<ContributionEntity>)

    @Query("SELECT * FROM contributions WHERE groupId = :groupId ORDER BY createdAtMillis DESC")
    fun getContributionsByGroupIdFlow(groupId: String): Flow<List<ContributionEntity>>

    @Query("SELECT * FROM contributions WHERE id = :contributionId")
    suspend fun getContributionById(contributionId: String): ContributionEntity?

    @Query("DELETE FROM contributions WHERE id = :contributionId")
    suspend fun deleteContribution(contributionId: String)

    @Query("DELETE FROM contributions WHERE groupId = :groupId")
    suspend fun deleteContributionsByGroupId(groupId: String)

    @Query("SELECT id FROM contributions WHERE groupId = :groupId")
    suspend fun getContributionIdsByGroupId(groupId: String): List<String>

    @Query("DELETE FROM contributions")
    suspend fun clearAllContributions()

    @Query("DELETE FROM contributions WHERE linkedExpenseId = :expenseId")
    suspend fun deleteByLinkedExpenseId(expenseId: String)

    @Query("SELECT * FROM contributions WHERE linkedExpenseId = :expenseId LIMIT 1")
    suspend fun findByLinkedExpenseId(expenseId: String): ContributionEntity?

    /**
     * Deletes contributions whose IDs are in the provided list.
     * Used to selectively remove stale contributions during sync reconciliation.
     */
    @Query("DELETE FROM contributions WHERE id IN (:ids)")
    suspend fun deleteContributionsByIds(ids: List<String>)

    /**
     * Reconciles local contributions for a group with the authoritative cloud snapshot.
     *
     * Uses a merge strategy instead of destructive delete+insert:
     * 1. Upsert all remote contributions (adds new, updates existing)
     * 2. Delete only local contributions whose IDs are NOT in the remote set
     *
     * This preserves locally-created contributions that haven't synced to the cloud yet.
     */
    @Transaction
    suspend fun replaceContributionsForGroup(groupId: String, contributions: List<ContributionEntity>) {
        val remoteIds = contributions.map { it.id }.toSet()
        val localIds = getContributionIdsByGroupId(groupId)
        val staleIds = localIds.filter { it !in remoteIds }

        // 1. Upsert remote contributions (adds new ones, updates existing)
        insertContributions(contributions)

        // 2. Remove only stale contributions (exist locally but not in remote snapshot)
        if (staleIds.isNotEmpty()) {
            deleteContributionsByIds(staleIds)
        }
    }
}
