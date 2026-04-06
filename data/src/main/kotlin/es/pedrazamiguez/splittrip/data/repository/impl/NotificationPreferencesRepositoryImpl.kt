package es.pedrazamiguez.splittrip.data.repository.impl

import es.pedrazamiguez.splittrip.data.local.datastore.NotificationUserPreferences
import es.pedrazamiguez.splittrip.domain.enums.NotificationCategory
import es.pedrazamiguez.splittrip.domain.model.NotificationPreferences
import es.pedrazamiguez.splittrip.domain.repository.NotificationPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class NotificationPreferencesRepositoryImpl(
    private val notificationUserPreferences: NotificationUserPreferences
) : NotificationPreferencesRepository {

    override fun getPreferencesFlow(): Flow<NotificationPreferences> = combine(
        notificationUserPreferences.notificationMembershipEnabled,
        notificationUserPreferences.notificationExpensesEnabled,
        notificationUserPreferences.notificationFinancialEnabled
    ) { membership, expenses, financial ->
        NotificationPreferences(
            membershipEnabled = membership,
            expensesEnabled = expenses,
            financialEnabled = financial
        )
    }

    override suspend fun updatePreference(category: NotificationCategory, enabled: Boolean) {
        when (category) {
            NotificationCategory.MEMBERSHIP -> notificationUserPreferences.setNotificationMembershipEnabled(enabled)
            NotificationCategory.EXPENSES -> notificationUserPreferences.setNotificationExpensesEnabled(enabled)
            NotificationCategory.FINANCIAL -> notificationUserPreferences.setNotificationFinancialEnabled(enabled)
        }
    }
}
