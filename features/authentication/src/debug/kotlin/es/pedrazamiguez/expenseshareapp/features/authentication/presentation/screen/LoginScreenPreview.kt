package es.pedrazamiguez.expenseshareapp.features.authentication.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import es.pedrazamiguez.expenseshareapp.features.authentication.presentation.model.AuthenticationUiState

@Preview
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        uiState = AuthenticationUiState(
            email = "user@example.com",
            password = "password123"
        )
    )
}
