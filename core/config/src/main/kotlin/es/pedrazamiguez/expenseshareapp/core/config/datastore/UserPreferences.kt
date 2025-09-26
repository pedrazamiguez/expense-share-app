package es.pedrazamiguez.expenseshareapp.core.config.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    private companion object {
        private const val ONBOARDING_COMPLETE = "onboarding_complete"
    }

    private val onboardingCompleteKey = booleanPreferencesKey(ONBOARDING_COMPLETE)

    val isOnboardingComplete: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[onboardingCompleteKey] ?: false
    }

    suspend fun setOnboardingComplete() {
        context.dataStore.edit { prefs ->
            prefs[onboardingCompleteKey] = true
        }
    }
}