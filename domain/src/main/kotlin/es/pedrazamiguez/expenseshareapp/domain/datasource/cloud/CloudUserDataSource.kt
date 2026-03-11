package es.pedrazamiguez.expenseshareapp.domain.datasource.cloud

import es.pedrazamiguez.expenseshareapp.domain.model.User

interface CloudUserDataSource {
    suspend fun saveUser(user: User)
    suspend fun getUsersByIds(userIds: List<String>): List<User>
}

