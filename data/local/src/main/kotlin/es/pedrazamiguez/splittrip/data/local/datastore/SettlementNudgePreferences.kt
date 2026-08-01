package es.pedrazamiguez.splittrip.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * User-scoped DataStore preferences for settlement nudge timestamps.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettlementNudgePreferences(
    context: Context,
    authenticationService: AuthenticationService
) : BaseUserPreferences(context, authenticationService) {

    private companion object {
        private const val KEY_PREFIX = "settlement_nudge_ts_"
    }

    val nudgeTimestampsFlow: Flow<Map<String, Long>> = userScopedFlow { userId ->
        val prefix = "${userId}_$KEY_PREFIX"
        context.dataStore.data.map { prefs ->
            prefs.asMap()
                .filterKeys { key -> key.name.startsWith(prefix) }
                .mapNotNull { (key, value) ->
                    if (value is Long) {
                        val settlementId = key.name.removePrefix(prefix)
                        settlementId to value
                    } else {
                        null
                    }
                }
                .toMap()
        }
    }

    suspend fun getLastNudgeTimestamp(settlementId: String): Long {
        val key = longPreferencesKey(userKey("$KEY_PREFIX$settlementId"))
        return context.dataStore.data.map { prefs -> prefs[key] ?: 0L }.first()
    }

    suspend fun recordNudgeTimestamp(settlementId: String, timestamp: Long) {
        val key = longPreferencesKey(userKey("$KEY_PREFIX$settlementId"))
        context.dataStore.edit { prefs ->
            prefs[key] = timestamp
        }
    }
}
