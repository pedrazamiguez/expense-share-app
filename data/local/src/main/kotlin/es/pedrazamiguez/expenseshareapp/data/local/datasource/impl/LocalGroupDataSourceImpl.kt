package es.pedrazamiguez.expenseshareapp.data.local.datasource.impl

import es.pedrazamiguez.expenseshareapp.data.local.dao.GroupDao
import es.pedrazamiguez.expenseshareapp.data.local.mapper.toDomain
import es.pedrazamiguez.expenseshareapp.data.local.mapper.toEntity
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalGroupDataSource
import es.pedrazamiguez.expenseshareapp.domain.model.Group
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementation of LocalGroupDataSource using Room.
 * This serves as the Single Source of Truth for Group data in the UI.
 */
class LocalGroupDataSourceImpl(
    private val groupDao: GroupDao
) : LocalGroupDataSource {

    override fun getGroupsFlow(): Flow<List<Group>> {
        return groupDao.getAllGroupsFlow().map { entities ->
            entities.toDomain()
        }
    }

    override suspend fun getGroupById(groupId: String): Group? {
        return groupDao.getGroupById(groupId)?.toDomain()
    }

    override fun getGroupByIdFlow(groupId: String): Flow<Group?> {
        return groupDao.getGroupByIdFlow(groupId).map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun saveGroups(groups: List<Group>) {
        groupDao.insertGroups(groups.toEntity())
    }

    override suspend fun saveGroup(group: Group) {
        groupDao.insertGroup(group.toEntity())
    }

    override suspend fun replaceAllGroups(groups: List<Group>) {
        groupDao.replaceAllGroups(groups.toEntity())
    }

    override suspend fun deleteGroup(groupId: String) {
        groupDao.deleteGroup(groupId)
    }

    override suspend fun clearAllGroups() {
        groupDao.clearAllGroups()
    }
}
