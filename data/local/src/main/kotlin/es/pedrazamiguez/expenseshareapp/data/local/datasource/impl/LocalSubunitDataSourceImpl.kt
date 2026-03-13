package es.pedrazamiguez.expenseshareapp.data.local.datasource.impl

import es.pedrazamiguez.expenseshareapp.data.local.dao.SubunitDao
import es.pedrazamiguez.expenseshareapp.data.local.mapper.toDomain
import es.pedrazamiguez.expenseshareapp.data.local.mapper.toEntity
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalSubunitDataSource
import es.pedrazamiguez.expenseshareapp.domain.model.Subunit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalSubunitDataSourceImpl(
    private val subunitDao: SubunitDao
) : LocalSubunitDataSource {

    override fun getSubunitsByGroupIdFlow(groupId: String): Flow<List<Subunit>> {
        return subunitDao.getSubunitsByGroupIdFlow(groupId).map { entities ->
            entities.toDomain()
        }
    }

    override suspend fun saveSubunit(subunit: Subunit) {
        subunitDao.insertSubunit(subunit.toEntity())
    }

    override suspend fun deleteSubunit(subunitId: String) {
        subunitDao.deleteSubunit(subunitId)
    }

    override suspend fun deleteSubunitsByGroupId(groupId: String) {
        subunitDao.deleteSubunitsByGroupId(groupId)
    }

    override suspend fun replaceSubunitsForGroup(
        groupId: String,
        subunits: List<Subunit>
    ) {
        subunitDao.replaceSubunitsForGroup(
            groupId,
            subunits.map { it.toEntity() }
        )
    }

    override suspend fun getSubunitIdsByGroup(groupId: String): List<String> {
        return subunitDao.getSubunitIdsByGroupId(groupId)
    }

    override suspend fun getSubunitById(subunitId: String): Subunit? {
        return subunitDao.getSubunitById(subunitId)?.toDomain()
    }

    override suspend fun clearAllSubunits() {
        subunitDao.clearAllSubunits()
    }
}

