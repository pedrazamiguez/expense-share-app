package es.pedrazamiguez.expenseshareapp.data.repository.impl

import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudUserDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalUserDataSource
import es.pedrazamiguez.expenseshareapp.domain.model.User
import es.pedrazamiguez.expenseshareapp.domain.repository.UserRepository
import timber.log.Timber

class UserRepositoryImpl(
    private val cloudUserDataSource: CloudUserDataSource,
    private val localUserDataSource: LocalUserDataSource
) : UserRepository {

    override suspend fun saveGoogleUser(user: User): Result<Unit> = runCatching {
        cloudUserDataSource.saveUser(user)
        // Also cache locally so the current user's display name is
        // available offline immediately without a Firestore round-trip.
        localUserDataSource.saveUsers(listOf(user))
    }

    override suspend fun getUsersByIds(userIds: List<String>): Map<String, User> {
        if (userIds.isEmpty()) return emptyMap()

        // 1. Try local (Room) first
        val localUsers = localUserDataSource.getUsersByIds(userIds)
        val localMap = localUsers.associateBy { it.userId }

        // 2. Identify missing IDs
        val missingIds = userIds.filter { it !in localMap }

        // 3. Fetch missing from cloud, cache locally
        if (missingIds.isNotEmpty()) {
            try {
                val cloudUsers = cloudUserDataSource.getUsersByIds(missingIds)
                if (cloudUsers.isNotEmpty()) {
                    localUserDataSource.saveUsers(cloudUsers)
                }
                return (localUsers + cloudUsers).associateBy { it.userId }
            } catch (e: Exception) {
                Timber.e(e, "Failed to fetch missing user profiles from cloud")
            }
        }

        return localMap
    }
}
