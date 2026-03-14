package es.pedrazamiguez.expenseshareapp.data.local.datasource.impl

import es.pedrazamiguez.expenseshareapp.data.local.dao.UserDao
import es.pedrazamiguez.expenseshareapp.data.local.mapper.toDomain
import es.pedrazamiguez.expenseshareapp.data.local.mapper.toEntities
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalUserDataSource
import es.pedrazamiguez.expenseshareapp.domain.model.User

class LocalUserDataSourceImpl(private val userDao: UserDao) : LocalUserDataSource {

    override suspend fun saveUsers(users: List<User>) {
        userDao.insertUsers(users.toEntities())
    }

    override suspend fun getUsersByIds(userIds: List<String>): List<User> = userDao.getUsersByIds(userIds).toDomain()
}
