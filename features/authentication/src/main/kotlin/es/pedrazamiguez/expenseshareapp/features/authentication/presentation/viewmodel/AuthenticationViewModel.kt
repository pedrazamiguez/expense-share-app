package es.pedrazamiguez.expenseshareapp.features.authentication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.domain.usecase.notification.RegisterDeviceTokenUseCase
import es.pedrazamiguez.expenseshareapp.features.authentication.presentation.model.AuthenticationUiEvent
import es.pedrazamiguez.expenseshareapp.features.authentication.presentation.model.AuthenticationUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class AuthenticationViewModel(
    private val authenticationService: AuthenticationService,
    private val registerDeviceTokenUseCase: RegisterDeviceTokenUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthenticationUiState())
    val uiState = _uiState.asStateFlow()

    fun onEvent(
        event: AuthenticationUiEvent,
        onLoginSuccess: () -> Unit
    ) {
        when (event) {
            is AuthenticationUiEvent.EmailChanged -> {
                _uiState.value = _uiState.value.copy(email = event.email)
            }

            is AuthenticationUiEvent.PasswordChanged -> {
                _uiState.value = _uiState.value.copy(password = event.password)
            }

            AuthenticationUiEvent.SubmitLogin -> {
                login(onLoginSuccess)
            }
        }
    }

    private fun login(onLoginSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            authenticationService
                .signIn(
                    _uiState.value.email,
                    _uiState.value.password
                )
                .onSuccess {
                    registerDeviceTokenUseCase().onFailure { e ->
                        Timber.e(
                            e,
                            "Failed to register device token"
                        )
                    }

                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onLoginSuccess()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
        }
    }

}
