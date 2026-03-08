package es.pedrazamiguez.expenseshareapp.data.repository.impl

import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudUserDataSource
import es.pedrazamiguez.expenseshareapp.domain.model.User
import es.pedrazamiguez.expenseshareapp.domain.repository.UserRepository

class UserRepositoryImpl(
    private val cloudUserDataSource: CloudUserDataSource
) : UserRepository {

    override suspend fun saveGoogleUser(user: User): Result<Unit> = runCatching {
        cloudUserDataSource.saveUser(user)
    }
}

