package es.pedrazamiguez.expenseshareapp.domain.service

import kotlinx.coroutines.flow.Flow

interface AuthenticationService {

    fun currentUserId(): String?

    val authState: Flow<Boolean>

    suspend fun signIn(
        email: String,
        password: String
    ): Result<String>

    suspend fun signUp(
        email: String,
        password: String
    ): Result<String>

    suspend fun signOut(): Result<Unit>

}
