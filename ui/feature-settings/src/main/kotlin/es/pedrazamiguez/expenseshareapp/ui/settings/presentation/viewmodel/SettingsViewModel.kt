package es.pedrazamiguez.expenseshareapp.ui.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.core.config.datastore.UserPreferences
import es.pedrazamiguez.expenseshareapp.domain.enums.Currency
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val authenticationService: AuthenticationService, private val userPreferences: UserPreferences
) : ViewModel() {

    val currentCurrency: StateFlow<Currency?> = userPreferences.defaultCurrency
        .map { code ->
            try {
                Currency.fromString(code)
            } catch (e: Exception) {
                Currency.EUR // Fallback to EUR if parsing fails
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun signOut(onSignedOut: () -> Unit) {
        viewModelScope.launch {
            authenticationService.signOut()
            onSignedOut()
        }
    }

}
