package es.pedrazamiguez.expenseshareapp.ui.authentication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.domain.repository.AuthenticationRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val authenticationRepository: AuthenticationRepository) : ViewModel() {
    fun signIn(email: String, password: String, onResult: (Result<String>) -> Unit) {
        viewModelScope.launch {
            val result = authenticationRepository.signIn(email, password)
            onResult(result)
        }
    }
}
