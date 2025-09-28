package es.pedrazamiguez.expenseshareapp.ui.authentication.presentation.model

sealed interface AuthenticationUiEvent {
    data class EmailChanged(val email: String) : AuthenticationUiEvent
    data class PasswordChanged(val password: String) : AuthenticationUiEvent
    data object SubmitLogin : AuthenticationUiEvent
}
