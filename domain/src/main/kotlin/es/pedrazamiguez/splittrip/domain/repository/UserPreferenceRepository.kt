package es.pedrazamiguez.splittrip.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferenceRepository {

    fun getUserDefaultCurrency(): Flow<String>
    suspend fun setUserDefaultCurrency(currencyCode: String)

    suspend fun clearAll()
}
