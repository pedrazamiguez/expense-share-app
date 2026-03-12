package es.pedrazamiguez.expenseshareapp.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import es.pedrazamiguez.expenseshareapp.core.common.constant.AppConstants
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "user_prefs")

@OptIn(ExperimentalCoroutinesApi::class)
class UserPreferences(
    private val context: Context,
    private val authenticationService: AuthenticationService
) {

    private companion object {
        private const val MAX_RECENT_ITEMS = 3
        private const val ANONYMOUS_USER = "anonymous"

        // Device-scoped key (NOT user-scoped) — onboarding is a device concern
        private val ONBOARDING_COMPLETE_KEY = booleanPreferencesKey("onboarding_complete")

        // User-scoped key name constants (prefixed at access time via userKey())
        private const val SELECTED_GROUP_ID = "selected_group_id"
        private const val SELECTED_GROUP_NAME = "selected_group_name"
        private const val DEFAULT_CURRENCY = "default_currency"
        private const val NOTIFICATION_MEMBERSHIP_ENABLED = "notification_membership_enabled"
        private const val NOTIFICATION_EXPENSES_ENABLED = "notification_expenses_enabled"
        private const val NOTIFICATION_FINANCIAL_ENABLED = "notification_financial_enabled"
    }

    // ── Auth-Reactive Flow ───────────────────────────────────────────────

    /**
     * Emits the current userId whenever the auth state changes.
     * Used via [userScopedFlow] so that all user-scoped Flows automatically
     * recompute when a different user signs in (preventing stale cross-user data).
     */
    private val currentUserId: Flow<String?> = authenticationService.authState.map {
        authenticationService.currentUserId()
    }

    /**
     * Creates a user-scoped Flow that restarts whenever the authenticated user changes.
     * Ensures long-lived collectors (e.g., SharedViewModel's `stateIn`) never retain
     * a previous user's values in memory after an auth change.
     */
    private fun <T> userScopedFlow(block: (userId: String) -> Flow<T>): Flow<T> {
        return currentUserId.flatMapLatest { userId ->
            block(userId ?: ANONYMOUS_USER)
        }
    }

    // ── Key Scoping ──────────────────────────────────────────────────────

    /**
     * Builds a user-scoped key name by prefixing the authenticated user's ID.
     * Falls back to [ANONYMOUS_USER] if called before authentication (safety net).
     */
    private fun userKey(name: String): String {
        val userId = authenticationService.currentUserId() ?: ANONYMOUS_USER
        return "${userId}_$name"
    }

    /**
     * Returns the current user prefix used for scoping keys.
     * Used by [clearAll] to selectively remove keys.
     */
    private fun currentUserPrefix(): String {
        val userId = authenticationService.currentUserId() ?: ANONYMOUS_USER
        return "${userId}_"
    }

    // ── Onboarding (Device-scoped) ───────────────────────────────────────

    val isOnboardingComplete: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[ONBOARDING_COMPLETE_KEY] ?: false
    }

    suspend fun setOnboardingComplete() {
        context.dataStore.edit { prefs ->
            prefs[ONBOARDING_COMPLETE_KEY] = true
        }
    }

    // ── Selected Group (User-scoped, auth-reactive) ──────────────────────

    val selectedGroupId: Flow<String?> = userScopedFlow { userId ->
        context.dataStore.data.map { prefs ->
            prefs[stringPreferencesKey("${userId}_$SELECTED_GROUP_ID")]
        }
    }

    val selectedGroupName: Flow<String?> = userScopedFlow { userId ->
        context.dataStore.data.map { prefs ->
            prefs[stringPreferencesKey("${userId}_$SELECTED_GROUP_NAME")]
        }
    }

    suspend fun setSelectedGroup(groupId: String?, groupName: String?) {
        context.dataStore.edit { prefs ->
            val idKey = stringPreferencesKey(userKey(SELECTED_GROUP_ID))
            val nameKey = stringPreferencesKey(userKey(SELECTED_GROUP_NAME))
            if (groupId != null && groupName != null) {
                prefs[idKey] = groupId
                prefs[nameKey] = groupName
            } else {
                prefs.remove(idKey)
                prefs.remove(nameKey)
            }
        }
    }

    // ── Default Currency (User-scoped, auth-reactive) ────────────────────

    val defaultCurrency: Flow<String> = userScopedFlow { userId ->
        context.dataStore.data.map { prefs ->
            prefs[stringPreferencesKey("${userId}_$DEFAULT_CURRENCY")]
                ?: AppConstants.DEFAULT_CURRENCY_CODE
        }
    }

    suspend fun setDefaultCurrency(currencyCode: String) {
        context.dataStore.edit { prefs ->
            prefs[stringPreferencesKey(userKey(DEFAULT_CURRENCY))] = currencyCode
        }
    }

    // ── Per-Group Last-Used Currency (User + Group scoped) ───────────────

    fun getGroupLastUsedCurrency(groupId: String): Flow<String?> {
        return userScopedFlow { userId ->
            val key = stringPreferencesKey("${userId}_last_used_currency_$groupId")
            context.dataStore.data.map { prefs -> prefs[key] }
        }
    }

    suspend fun setGroupLastUsedCurrency(groupId: String, currencyCode: String) {
        val key = stringPreferencesKey(userKey("last_used_currency_$groupId"))
        context.dataStore.edit { prefs ->
            prefs[key] = currencyCode
        }
    }

    // ── MRU List Helpers (User + Group scoped) ───────────────────────────

    private fun getRecentIds(keyPrefix: String, groupId: String): Flow<List<String>> {
        return userScopedFlow { userId ->
            val key = stringPreferencesKey("${userId}_${keyPrefix}_$groupId")
            context.dataStore.data.map { prefs ->
                prefs[key]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
            }
        }
    }

    private suspend fun addRecentId(keyPrefix: String, groupId: String, id: String) {
        val key = stringPreferencesKey(userKey("${keyPrefix}_$groupId"))
        context.dataStore.edit { prefs ->
            val current = prefs[key]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
            val updated = (listOf(id) + current.filter { it != id })
                .take(MAX_RECENT_ITEMS)
            prefs[key] = updated.joinToString(",")
        }
    }

    fun getGroupLastUsedPaymentMethod(groupId: String): Flow<List<String>> {
        return getRecentIds("last_used_payment_method", groupId)
    }

    suspend fun setGroupLastUsedPaymentMethod(groupId: String, paymentMethodId: String) {
        addRecentId("last_used_payment_method", groupId, paymentMethodId)
    }

    fun getGroupLastUsedCategory(groupId: String): Flow<List<String>> {
        return getRecentIds("last_used_category", groupId)
    }

    suspend fun setGroupLastUsedCategory(groupId: String, categoryId: String) {
        addRecentId("last_used_category", groupId, categoryId)
    }

    // ── Last Seen Balance (User + Group scoped) ──────────────────────────

    fun getLastSeenBalance(groupId: String): Flow<String?> {
        return userScopedFlow { userId ->
            val key = stringPreferencesKey("${userId}_last_seen_balance_$groupId")
            context.dataStore.data.map { prefs -> prefs[key] }
        }
    }

    suspend fun setLastSeenBalance(groupId: String, formattedBalance: String) {
        val key = stringPreferencesKey(userKey("last_seen_balance_$groupId"))
        context.dataStore.edit { prefs ->
            prefs[key] = formattedBalance
        }
    }

    // ── Notification Preferences (User-scoped, auth-reactive) ──────────

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

    // ── Cleanup ──────────────────────────────────────────────────────────

    /**
     * Selectively clears only the current user's scoped preferences.
     * Device-scoped keys (e.g., onboarding) and other users' keys are preserved.
     */
    suspend fun clearAll() {
        val prefix = currentUserPrefix()
        context.dataStore.edit { prefs ->
            val keysToRemove = prefs.asMap().keys.filter { it.name.startsWith(prefix) }
            keysToRemove.forEach { prefs.remove(it) }
        }
    }

}
