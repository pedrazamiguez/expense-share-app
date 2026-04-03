package es.pedrazamiguez.expenseshareapp.domain.repository

import es.pedrazamiguez.expenseshareapp.domain.model.Contribution
import kotlinx.coroutines.flow.Flow

interface ContributionRepository {

    suspend fun addContribution(groupId: String, contribution: Contribution)

    fun getGroupContributionsFlow(groupId: String): Flow<List<Contribution>>

    suspend fun deleteContribution(groupId: String, contributionId: String)

    /**
     * Deletes the contribution paired with the given expense ID within the specified group.
     *
     * The domain model expects at most one linked contribution for a given
     * `(groupId, linkedExpenseId)` pair. Used for cascade-deletion when an
     * out-of-pocket expense is deleted.
     */
    suspend fun deleteByLinkedExpenseId(groupId: String, linkedExpenseId: String)

    /**
     * Finds the contribution linked to the given expense ID within the specified group,
     * or null if none exists.
     *
     * The domain model expects at most one linked contribution for a given
     * `(groupId, linkedExpenseId)` pair. Used for edit/update support of
     * out-of-pocket paired contributions.
     */
    suspend fun findByLinkedExpenseId(groupId: String, linkedExpenseId: String): Contribution?
}
