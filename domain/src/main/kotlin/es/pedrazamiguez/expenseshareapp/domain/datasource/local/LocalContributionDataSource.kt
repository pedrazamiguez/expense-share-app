package es.pedrazamiguez.expenseshareapp.domain.datasource.local

import es.pedrazamiguez.expenseshareapp.domain.model.Contribution
import kotlinx.coroutines.flow.Flow

interface LocalContributionDataSource {

    fun getContributionsByGroupIdFlow(groupId: String): Flow<List<Contribution>>

    suspend fun saveContribution(contribution: Contribution)

    suspend fun deleteContribution(contributionId: String)

    suspend fun deleteContributionsByGroupId(groupId: String)

    /**
     * Atomically replaces all contributions for a group with the provided list.
     * Used during real-time sync to reconcile local state with the cloud snapshot.
     */
    suspend fun replaceContributionsForGroup(groupId: String, contributions: List<Contribution>)

    suspend fun getContributionIdsByGroup(groupId: String): List<String>

    suspend fun clearAllContributions()

    /**
     * Deletes all contributions linked to the given expense ID.
     * Used for cascade-deletion when an out-of-pocket expense is deleted.
     */
    suspend fun deleteByLinkedExpenseId(linkedExpenseId: String)

    /**
     * Finds the contribution linked to the given expense ID, or null if none exists.
     * Used for edit/update support of out-of-pocket paired contributions.
     */
    suspend fun findByLinkedExpenseId(linkedExpenseId: String): Contribution?
}
