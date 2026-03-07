package es.pedrazamiguez.expenseshareapp.data.repository.impl

import es.pedrazamiguez.expenseshareapp.data.local.datastore.UserPreferences
import es.pedrazamiguez.expenseshareapp.domain.repository.PreferenceRepository
import kotlinx.coroutines.flow.Flow

class PreferenceRepositoryImpl(
    private val userPreferences: UserPreferences
) : PreferenceRepository {

    override fun isOnboardingComplete(): Flow<Boolean> {
        return userPreferences.isOnboardingComplete
    }

    override suspend fun setOnboardingComplete() {
        userPreferences.setOnboardingComplete()
    }

    override fun getSelectedGroupId(): Flow<String?> {
        return userPreferences.selectedGroupId
    }

    override fun getSelectedGroupName(): Flow<String?> {
        return userPreferences.selectedGroupName
    }

    override suspend fun setSelectedGroup(groupId: String?, groupName: String?) {
        userPreferences.setSelectedGroup(groupId, groupName)
    }

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

    override fun getLastSeenBalance(groupId: String): Flow<String?> {
        return userPreferences.getLastSeenBalance(groupId)
    }

    override suspend fun setLastSeenBalance(groupId: String, formattedBalance: String) {
        userPreferences.setLastSeenBalance(groupId, formattedBalance)
    }

}
