package es.pedrazamiguez.expenseshareapp.data.repository.impl

import es.pedrazamiguez.expenseshareapp.domain.repository.AuthenticationRepository
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService

class AuthenticationRepositoryImpl(
    private val authenticationService: AuthenticationService
) : AuthenticationRepository {

    override suspend fun signIn(
        email: String,
        password: String
    ): Result<String> = authenticationService.signIn(
        email,
        password
    )

    override suspend fun signUp(
        email: String,
        password: String
    ): Result<String> = authenticationService.signUp(
        email,
        password
    )

    override suspend fun signOut(): Result<Unit> = authenticationService.signOut()

}
