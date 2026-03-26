package es.pedrazamiguez.expenseshareapp.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Preference repository for balance-related settings (last seen balance).
 *
 * Extracted from [PreferenceRepository] to keep function counts
 * within the configured detekt threshold.
 */
interface BalancePreferenceRepository {

    fun getLastSeenBalance(groupId: String): Flow<String?>

    suspend fun setLastSeenBalance(groupId: String, formattedBalance: String)
}
