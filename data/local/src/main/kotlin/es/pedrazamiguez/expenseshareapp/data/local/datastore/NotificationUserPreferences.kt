package es.pedrazamiguez.expenseshareapp.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * User-scoped DataStore preferences for notification settings.
 *
 * Extracted from [UserPreferences] to keep function counts within detekt thresholds.
 * Shares the same underlying DataStore file via [BaseUserPreferences].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NotificationUserPreferences(
    context: Context,
    authenticationService: AuthenticationService
) : BaseUserPreferences(context, authenticationService) {

    private companion object {
        private const val NOTIFICATION_MEMBERSHIP_ENABLED = "notification_membership_enabled"
        private const val NOTIFICATION_EXPENSES_ENABLED = "notification_expenses_enabled"
        private const val NOTIFICATION_FINANCIAL_ENABLED = "notification_financial_enabled"
    }

    val notificationMembershipEnabled: Flow<Boolean> = userScopedFlow { userId ->
        context.dataStore.data.map { prefs ->
            prefs[booleanPreferencesKey("${userId}_$NOTIFICATION_MEMBERSHIP_ENABLED")] ?: true
        }
    }

    val notificationExpensesEnabled: Flow<Boolean> = userScopedFlow { userId ->
        context.dataStore.data.map { prefs ->
            prefs[booleanPreferencesKey("${userId}_$NOTIFICATION_EXPENSES_ENABLED")] ?: true
        }
    }

    val notificationFinancialEnabled: Flow<Boolean> = userScopedFlow { userId ->
        context.dataStore.data.map { prefs ->
            prefs[booleanPreferencesKey("${userId}_$NOTIFICATION_FINANCIAL_ENABLED")] ?: true
        }
    }

    suspend fun setNotificationMembershipEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[booleanPreferencesKey(userKey(NOTIFICATION_MEMBERSHIP_ENABLED))] = enabled
        }
    }

    suspend fun setNotificationExpensesEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[booleanPreferencesKey(userKey(NOTIFICATION_EXPENSES_ENABLED))] = enabled
        }
    }

    suspend fun setNotificationFinancialEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[booleanPreferencesKey(userKey(NOTIFICATION_FINANCIAL_ENABLED))] = enabled
        }
    }
}
