package es.pedrazamiguez.expenseshareapp.domain.repository

import es.pedrazamiguez.expenseshareapp.domain.model.User

interface UserRepository {
    suspend fun saveGoogleUser(user: User): Result<Unit>
}
