package es.pedrazamiguez.expenseshareapp.domain.repository

import kotlinx.coroutines.flow.Flow

interface PreferenceRepository {

    fun isOnboardingComplete(): Flow<Boolean>
    suspend fun setOnboardingComplete()

    fun getSelectedGroupId(): Flow<String?>
    fun getSelectedGroupName(): Flow<String?>
    suspend fun setSelectedGroup(groupId: String?, groupName: String?)

    fun getUserDefaultCurrency(): Flow<String>
    suspend fun setUserDefaultCurrency(currencyCode: String)

    fun getGroupLastUsedCurrency(groupId: String): Flow<String?>
    suspend fun setGroupLastUsedCurrency(groupId: String, currencyCode: String)

    fun getGroupLastUsedPaymentMethod(groupId: String): Flow<String?>
    suspend fun setGroupLastUsedPaymentMethod(groupId: String, paymentMethodId: String)

    fun getGroupLastUsedCategory(groupId: String): Flow<String?>
    suspend fun setGroupLastUsedCategory(groupId: String, categoryId: String)

    fun getLastSeenBalance(groupId: String): Flow<String?>
    suspend fun setLastSeenBalance(groupId: String, formattedBalance: String)
}
