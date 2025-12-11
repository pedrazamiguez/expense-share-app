package es.pedrazamiguez.expenseshareapp.ui.authentication.presentation.model

sealed interface AuthenticationUiAction {
    data class ShowError(val message: String) : AuthenticationUiAction
    data object LoginSuccess : AuthenticationUiAction
}
