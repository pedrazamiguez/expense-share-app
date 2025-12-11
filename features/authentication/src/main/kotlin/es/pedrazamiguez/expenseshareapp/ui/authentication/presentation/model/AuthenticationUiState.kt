package es.pedrazamiguez.expenseshareapp.ui.authentication.presentation.model

data class AuthenticationUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
