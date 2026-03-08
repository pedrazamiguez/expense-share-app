package es.pedrazamiguez.expenseshareapp.domain.repository

import java.math.BigDecimal

interface UserRepository {
    suspend fun getUserBalance(userId: String): BigDecimal
    suspend fun saveGoogleUser(
        userId: String,
        email: String,
        displayName: String?,
        profilePictureUrl: String?
    ): Result<Unit>
}
