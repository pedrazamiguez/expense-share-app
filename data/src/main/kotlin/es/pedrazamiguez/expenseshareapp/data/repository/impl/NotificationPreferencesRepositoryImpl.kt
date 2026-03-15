package es.pedrazamiguez.expenseshareapp.data.repository.impl

import es.pedrazamiguez.expenseshareapp.data.local.datastore.UserPreferences
import es.pedrazamiguez.expenseshareapp.domain.enums.NotificationCategory
import es.pedrazamiguez.expenseshareapp.domain.model.NotificationPreferences
import es.pedrazamiguez.expenseshareapp.domain.repository.NotificationPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class NotificationPreferencesRepositoryImpl(private val userPreferences: UserPreferences) :
    NotificationPreferencesRepository {

    override fun getPreferencesFlow(): Flow<NotificationPreferences> = combine(
        userPreferences.notificationMembershipEnabled,
        userPreferences.notificationExpensesEnabled,
        userPreferences.notificationFinancialEnabled
    ) { membership, expenses, financial ->
        NotificationPreferences(
            membershipEnabled = membership,
            expensesEnabled = expenses,
            financialEnabled = financial
        )
    }

    override suspend fun updatePreference(category: NotificationCategory, enabled: Boolean) {
        when (category) {
            NotificationCategory.MEMBERSHIP -> userPreferences.setNotificationMembershipEnabled(enabled)
            NotificationCategory.EXPENSES -> userPreferences.setNotificationExpensesEnabled(enabled)
            NotificationCategory.FINANCIAL -> userPreferences.setNotificationFinancialEnabled(enabled)
        }
    }
}
