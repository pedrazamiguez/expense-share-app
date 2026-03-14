package es.pedrazamiguez.expenseshareapp.data.repository.impl

import es.pedrazamiguez.expenseshareapp.data.local.datastore.UserPreferences
import es.pedrazamiguez.expenseshareapp.domain.repository.PreferenceRepository
import kotlinx.coroutines.flow.Flow

class PreferenceRepositoryImpl(private val userPreferences: UserPreferences) : PreferenceRepository {

    override fun isOnboardingComplete(): Flow<Boolean> = userPreferences.isOnboardingComplete

    override suspend fun setOnboardingComplete() {
        userPreferences.setOnboardingComplete()
    }

    override fun getSelectedGroupId(): Flow<String?> = userPreferences.selectedGroupId

    override fun getSelectedGroupName(): Flow<String?> = userPreferences.selectedGroupName

    override suspend fun setSelectedGroup(groupId: String?, groupName: String?) {
        userPreferences.setSelectedGroup(groupId, groupName)
    }

    override fun getUserDefaultCurrency(): Flow<String> = userPreferences.defaultCurrency

    override suspend fun setUserDefaultCurrency(currencyCode: String) {
        userPreferences.setDefaultCurrency(currencyCode)
    }

    override fun getGroupLastUsedCurrency(groupId: String): Flow<String?> =
        userPreferences.getGroupLastUsedCurrency(groupId)

    override suspend fun setGroupLastUsedCurrency(groupId: String, currencyCode: String) {
        userPreferences.setGroupLastUsedCurrency(groupId, currencyCode)
    }

    override fun getGroupLastUsedPaymentMethod(groupId: String): Flow<List<String>> =
        userPreferences.getGroupLastUsedPaymentMethod(groupId)

    override suspend fun setGroupLastUsedPaymentMethod(groupId: String, paymentMethodId: String) {
        userPreferences.setGroupLastUsedPaymentMethod(groupId, paymentMethodId)
    }

    override fun getGroupLastUsedCategory(groupId: String): Flow<List<String>> =
        userPreferences.getGroupLastUsedCategory(groupId)

    override suspend fun setGroupLastUsedCategory(groupId: String, categoryId: String) {
        userPreferences.setGroupLastUsedCategory(groupId, categoryId)
    }

    override fun getLastSeenBalance(groupId: String): Flow<String?> = userPreferences.getLastSeenBalance(groupId)

    override suspend fun setLastSeenBalance(groupId: String, formattedBalance: String) {
        userPreferences.setLastSeenBalance(groupId, formattedBalance)
    }

    override suspend fun clearAll() {
        userPreferences.clearAll()
    }
}
