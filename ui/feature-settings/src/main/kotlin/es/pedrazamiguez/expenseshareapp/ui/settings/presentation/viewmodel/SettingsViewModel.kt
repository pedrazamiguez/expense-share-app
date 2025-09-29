package es.pedrazamiguez.expenseshareapp.ui.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val authenticationService: AuthenticationService
) : ViewModel() {

    fun signOut(onSignedOut: () -> Unit) {
        viewModelScope.launch {
            authenticationService.signOut()
            onSignedOut()
        }
    }

}
