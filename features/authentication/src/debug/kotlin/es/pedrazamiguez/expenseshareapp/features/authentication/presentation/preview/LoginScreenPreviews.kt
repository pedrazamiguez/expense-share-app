package es.pedrazamiguez.expenseshareapp.features.authentication.presentation.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import es.pedrazamiguez.expenseshareapp.features.authentication.presentation.model.AuthenticationUiState
import es.pedrazamiguez.expenseshareapp.features.authentication.presentation.screen.LoginScreen

@Preview
@Composable
private fun LoginScreenPreview() {
    LoginScreen(
        uiState = AuthenticationUiState(
            email = "user@example.com",
            password = "password123"
        )
    )
}
