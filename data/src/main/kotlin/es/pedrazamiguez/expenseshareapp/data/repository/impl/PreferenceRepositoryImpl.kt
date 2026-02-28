package es.pedrazamiguez.expenseshareapp.data.repository.impl

import es.pedrazamiguez.expenseshareapp.core.common.datastore.UserPreferences
import es.pedrazamiguez.expenseshareapp.domain.repository.PreferenceRepository
import kotlinx.coroutines.flow.Flow

class PreferenceRepositoryImpl(
    private val userPreferences: UserPreferences
) : PreferenceRepository {

    override fun getUserDefaultCurrency(): Flow<String> {
        return userPreferences.defaultCurrency
    }

    override suspend fun setUserDefaultCurrency(currencyCode: String) {
        userPreferences.setDefaultCurrency(currencyCode)
    }

    override fun getGroupLastUsedCurrency(groupId: String): Flow<String?> {
        return userPreferences.getGroupLastUsedCurrency(groupId)
    }

    override suspend fun setGroupLastUsedCurrency(groupId: String, currencyCode: String) {
        userPreferences.setGroupLastUsedCurrency(groupId, currencyCode)
    }

}
