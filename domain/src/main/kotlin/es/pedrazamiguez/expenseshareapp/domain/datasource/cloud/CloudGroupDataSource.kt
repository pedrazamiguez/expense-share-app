package es.pedrazamiguez.expenseshareapp.domain.datasource.cloud

import es.pedrazamiguez.expenseshareapp.domain.model.Group

interface CloudGroupDataSource {
    suspend fun createGroup(group: Group): String
    suspend fun getGroupById(groupId: String): Group?
    suspend fun getAllGroups(): List<Group>
}
