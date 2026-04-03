package es.pedrazamiguez.expenseshareapp.domain.repository

import es.pedrazamiguez.expenseshareapp.domain.model.Contribution
import kotlinx.coroutines.flow.Flow

interface ContributionRepository {

    suspend fun addContribution(groupId: String, contribution: Contribution)

    fun getGroupContributionsFlow(groupId: String): Flow<List<Contribution>>

    suspend fun deleteContribution(groupId: String, contributionId: String)

    suspend fun deleteByLinkedExpenseId(groupId: String, linkedExpenseId: String)

    suspend fun findByLinkedExpenseId(groupId: String, linkedExpenseId: String): Contribution?
}
