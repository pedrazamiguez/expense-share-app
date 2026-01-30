package es.pedrazamiguez.expenseshareapp.core.common.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import es.pedrazamiguez.expenseshareapp.core.common.constant.AppConstants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    private companion object {
        private val ONBOARDING_COMPLETE_KEY = booleanPreferencesKey("onboarding_complete")
        private val SELECTED_GROUP_ID_KEY = stringPreferencesKey("selected_group_id")
        private val SELECTED_GROUP_NAME_KEY = stringPreferencesKey("selected_group_name")
        private val DEFAULT_CURRENCY_KEY = stringPreferencesKey("default_currency")
    }

    val isOnboardingComplete: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[ONBOARDING_COMPLETE_KEY] ?: false
    }

    suspend fun setOnboardingComplete() {
        context.dataStore.edit { prefs ->
            prefs[ONBOARDING_COMPLETE_KEY] = true
        }
    }

    val selectedGroupId: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[SELECTED_GROUP_ID_KEY]
    }

    val selectedGroupName: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[SELECTED_GROUP_NAME_KEY]
    }

    suspend fun setSelectedGroup(groupId: String?, groupName: String?) {
        context.dataStore.edit { prefs ->
            if (groupId != null && groupName != null) {
                prefs[SELECTED_GROUP_ID_KEY] = groupId
                prefs[SELECTED_GROUP_NAME_KEY] = groupName
            } else {
                prefs.remove(SELECTED_GROUP_ID_KEY)
                prefs.remove(SELECTED_GROUP_NAME_KEY)
            }
        }
    }

    val defaultCurrency: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[DEFAULT_CURRENCY_KEY] ?: AppConstants.DEFAULT_CURRENCY_CODE
    }

    suspend fun setDefaultCurrency(currencyCode: String) {
        context.dataStore.edit { prefs ->
            prefs[DEFAULT_CURRENCY_KEY] = currencyCode
        }
    }

}
