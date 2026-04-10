package es.pedrazamiguez.splittrip.domain.repository

import kotlinx.coroutines.flow.Flow

interface PreferenceRepository {

    fun isOnboardingComplete(): Flow<Boolean>
    suspend fun setOnboardingComplete()

    fun getSelectedGroupId(): Flow<String?>
    fun getSelectedGroupName(): Flow<String?>
    fun getSelectedGroupCurrency(): Flow<String?>
    suspend fun setSelectedGroup(groupId: String?, groupName: String?, currency: String?)

    fun getUserDefaultCurrency(): Flow<String>
    suspend fun setUserDefaultCurrency(currencyCode: String)

    fun getGroupLastUsedCurrency(groupId: String): Flow<String?>
    suspend fun setGroupLastUsedCurrency(groupId: String, currencyCode: String)

    fun getGroupLastUsedPaymentMethod(groupId: String): Flow<List<String>>
    suspend fun setGroupLastUsedPaymentMethod(groupId: String, paymentMethodId: String)

    fun getGroupLastUsedCategory(groupId: String): Flow<List<String>>
    suspend fun setGroupLastUsedCategory(groupId: String, categoryId: String)

    suspend fun clearAll()
}
