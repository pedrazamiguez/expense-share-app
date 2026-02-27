package es.pedrazamiguez.expenseshareapp.data.repository.impl

import es.pedrazamiguez.expenseshareapp.core.common.datastore.UserPreferences
import es.pedrazamiguez.expenseshareapp.domain.repository.PreferenceRepository
import kotlinx.coroutines.flow.Flow

class PreferenceRepositoryImpl(
    private val userPreferences: UserPreferences
) : PreferenceRepository {

    override fun getGroupLastUsedCurrency(groupId: String): Flow<String?> {
        return userPreferences.getGroupLastUsedCurrency(groupId)
    }

    override suspend fun setGroupLastUsedCurrency(groupId: String, currencyCode: String) {
        userPreferences.setGroupLastUsedCurrency(groupId, currencyCode)
    }

}
