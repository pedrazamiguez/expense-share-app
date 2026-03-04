package es.pedrazamiguez.expenseshareapp.data.local.datasource.impl

import es.pedrazamiguez.expenseshareapp.data.local.dao.ContributionDao
import es.pedrazamiguez.expenseshareapp.data.local.mapper.toDomain
import es.pedrazamiguez.expenseshareapp.data.local.mapper.toEntity
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalContributionDataSource
import es.pedrazamiguez.expenseshareapp.domain.model.Contribution
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalContributionDataSourceImpl(
    private val contributionDao: ContributionDao
) : LocalContributionDataSource {

    override fun getContributionsByGroupIdFlow(groupId: String): Flow<List<Contribution>> {
        return contributionDao.getContributionsByGroupIdFlow(groupId).map { entities ->
            entities.toDomain()
        }
    }

    override suspend fun saveContribution(contribution: Contribution) {
        contributionDao.insertContribution(contribution.toEntity())
    }

    override suspend fun deleteContribution(contributionId: String) {
        contributionDao.deleteContribution(contributionId)
    }

    override suspend fun deleteContributionsByGroupId(groupId: String) {
        contributionDao.deleteContributionsByGroupId(groupId)
    }

    override suspend fun replaceContributionsForGroup(
        groupId: String,
        contributions: List<Contribution>
    ) {
        contributionDao.replaceContributionsForGroup(
            groupId,
            contributions.map { it.toEntity() }
        )
    }

    override suspend fun getContributionIdsByGroup(groupId: String): List<String> {
        return contributionDao.getContributionIdsByGroupId(groupId)
    }

    override suspend fun clearAllContributions() {
        contributionDao.clearAllContributions()
    }
}

