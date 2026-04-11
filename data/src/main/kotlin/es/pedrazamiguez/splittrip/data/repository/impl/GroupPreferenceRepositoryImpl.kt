package es.pedrazamiguez.splittrip.data.repository.impl

import es.pedrazamiguez.splittrip.data.local.datastore.UserPreferences
import es.pedrazamiguez.splittrip.domain.repository.GroupPreferenceRepository
import kotlinx.coroutines.flow.Flow

class GroupPreferenceRepositoryImpl(
    private val userPreferences: UserPreferences
) : GroupPreferenceRepository {

    override fun getSelectedGroupId(): Flow<String?> = userPreferences.selectedGroupId

    override fun getSelectedGroupName(): Flow<String?> = userPreferences.selectedGroupName

    override fun getSelectedGroupCurrency(): Flow<String?> = userPreferences.selectedGroupCurrency

    override suspend fun setSelectedGroup(groupId: String?, groupName: String?, currency: String?) {
        userPreferences.setSelectedGroup(groupId, groupName, currency)
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
}
