package es.pedrazamiguez.splittrip.data.repository.impl

import es.pedrazamiguez.splittrip.data.local.datastore.UserPreferences
import es.pedrazamiguez.splittrip.domain.repository.BalancePreferenceRepository
import kotlinx.coroutines.flow.Flow

class BalancePreferenceRepositoryImpl(
    private val userPreferences: UserPreferences
) : BalancePreferenceRepository {

    override fun getLastSeenBalance(groupId: String): Flow<String?> =
        userPreferences.getLastSeenBalance(groupId)

    override suspend fun setLastSeenBalance(groupId: String, formattedBalance: String) {
        userPreferences.setLastSeenBalance(groupId, formattedBalance)
    }
}
