package es.pedrazamiguez.expenseshareapp.domain.repository

interface UserRepository {
    suspend fun getUserBalance(userId: String): Double
}
