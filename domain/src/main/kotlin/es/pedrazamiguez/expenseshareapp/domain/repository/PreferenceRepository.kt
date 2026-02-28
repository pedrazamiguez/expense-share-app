package es.pedrazamiguez.expenseshareapp.domain.repository

import kotlinx.coroutines.flow.Flow

interface PreferenceRepository {
    fun getUserDefaultCurrency(): Flow<String>
    suspend fun setUserDefaultCurrency(currencyCode: String)

    fun getGroupLastUsedCurrency(groupId: String): Flow<String?>
    suspend fun setGroupLastUsedCurrency(groupId: String, currencyCode: String)
}
