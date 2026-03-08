package es.pedrazamiguez.expenseshareapp.data.repository.impl

import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudUserDataSource
import es.pedrazamiguez.expenseshareapp.domain.repository.UserRepository
import java.math.BigDecimal

class UserRepositoryImpl(
    private val cloudUserDataSource: CloudUserDataSource
) : UserRepository {

    override suspend fun getUserBalance(userId: String): BigDecimal {
        // TODO: implement when balance logic is wired
        return BigDecimal.ZERO
    }

    override suspend fun saveGoogleUser(
        userId: String,
        email: String,
        displayName: String?,
        profilePictureUrl: String?
    ): Result<Unit> = runCatching {
        cloudUserDataSource.saveGoogleUser(
            userId = userId,
            email = email,
            displayName = displayName,
            profilePictureUrl = profilePictureUrl
        )
    }
}

