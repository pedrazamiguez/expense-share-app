package es.pedrazamiguez.expenseshareapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.domain.repository.AuthRepository
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    fun signIn(email: String, password: String, onResult: (Result<String>) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.signIn(email, password)
            onResult(result)
        }
    }
}