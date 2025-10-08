package es.pedrazamiguez.expenseshareapp.core.config.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    private companion object {
        private val ONBOARDING_COMPLETE_KEY = booleanPreferencesKey("onboarding_complete")
        private val SELECTED_GROUP_ID_KEY = stringPreferencesKey("selected_group_id")
    }

    val isOnboardingComplete: Flow<Boolean?> = context.dataStore.data.map { prefs ->
        prefs[ONBOARDING_COMPLETE_KEY]
    }

    suspend fun setOnboardingComplete() {
        context.dataStore.edit { prefs ->
            prefs[ONBOARDING_COMPLETE_KEY] = true
        }
    }

    val selectedGroupId: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[SELECTED_GROUP_ID_KEY]
    }

    suspend fun setSelectedGroupId(groupId: String?) {
        context.dataStore.edit { prefs ->
            if (groupId == null) {
                prefs.remove(SELECTED_GROUP_ID_KEY)
            } else {
                prefs[SELECTED_GROUP_ID_KEY] = groupId
            }
        }
    }
}
