package es.pedrazamiguez.expenseshareapp.domain.datasource.cloud

import es.pedrazamiguez.expenseshareapp.domain.model.Contribution
import kotlinx.coroutines.flow.Flow

interface CloudContributionDataSource {

    suspend fun addContribution(groupId: String, contribution: Contribution)

    suspend fun deleteContribution(groupId: String, contributionId: String)

    /**
     * One-shot fetch of contributions for sync purposes.
     */
    suspend fun fetchContributionsByGroupId(groupId: String): List<Contribution>

    /**
     * Reactive stream of contributions for real-time UI observers.
     * Emits local cache first, then server data as it arrives.
     */
    fun getContributionsByGroupIdFlow(groupId: String): Flow<List<Contribution>>
}
