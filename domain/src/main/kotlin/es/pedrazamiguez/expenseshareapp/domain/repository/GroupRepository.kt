package es.pedrazamiguez.expenseshareapp.domain.repository

import es.pedrazamiguez.expenseshareapp.domain.model.Group
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    suspend fun createGroup(group: Group): String
    suspend fun getGroupById(groupId: String): Group?
    fun getAllGroupsFlow(): Flow<List<Group>>
}
