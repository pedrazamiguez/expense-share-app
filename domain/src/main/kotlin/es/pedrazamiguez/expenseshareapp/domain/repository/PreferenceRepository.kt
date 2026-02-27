package es.pedrazamiguez.expenseshareapp.domain.repository

import kotlinx.coroutines.flow.Flow

interface PreferenceRepository {
    fun getGroupLastUsedCurrency(groupId: String): Flow<String?>
    suspend fun setGroupLastUsedCurrency(groupId: String, currencyCode: String)
}
