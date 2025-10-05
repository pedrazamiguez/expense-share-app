package es.pedrazamiguez.expenseshareapp.data.repository.impl

import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudGroupDataSource
import es.pedrazamiguez.expenseshareapp.domain.model.Group
import es.pedrazamiguez.expenseshareapp.domain.repository.GroupRepository
import kotlinx.coroutines.flow.Flow

class GroupRepositoryImpl(
    private val cloudGroupDataSource: CloudGroupDataSource,
) : GroupRepository {

    override suspend fun createGroup(group: Group): String = cloudGroupDataSource.createGroup(group)

    override suspend fun getGroupById(groupId: String): Group? {
        TODO("Not yet implemented")
    }

    override fun getAllGroupsFlow(): Flow<List<Group>> = cloudGroupDataSource.getAllGroupsFlow()

}
