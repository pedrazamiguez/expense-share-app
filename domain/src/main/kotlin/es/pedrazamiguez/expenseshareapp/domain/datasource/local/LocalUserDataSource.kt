package es.pedrazamiguez.expenseshareapp.domain.datasource.local

import es.pedrazamiguez.expenseshareapp.domain.model.User

interface LocalUserDataSource {
    suspend fun saveUsers(users: List<User>)
    suspend fun getUsersByIds(userIds: List<String>): List<User>
}
