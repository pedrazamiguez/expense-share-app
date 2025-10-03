package es.pedrazamiguez.expenseshareapp.data.repository.impl

import es.pedrazamiguez.expenseshareapp.domain.model.Group
import es.pedrazamiguez.expenseshareapp.domain.repository.GroupRepository

class GroupRepositoryImpl : GroupRepository {

    override suspend fun createGroup(group: Group): String {
        TODO("Not yet implemented")
    }

    override suspend fun getGroupById(groupId: String): Group? {
        TODO("Not yet implemented")
    }

    override suspend fun getAllGroups(): List<Group> {
        TODO("Not yet implemented")
    }

}
