package es.pedrazamiguez.expenseshareapp.domain.service

interface AuthenticationService {

    suspend fun signIn(
        email: String,
        password: String
    ): Result<String>

    suspend fun signUp(
        email: String,
        password: String
    ): Result<String>

}
