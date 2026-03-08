package es.pedrazamiguez.expenseshareapp.features.authentication.presentation.model

import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText

data class AuthenticationUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isGoogleLoading: Boolean = false,
    val error: UiText? = null
)
