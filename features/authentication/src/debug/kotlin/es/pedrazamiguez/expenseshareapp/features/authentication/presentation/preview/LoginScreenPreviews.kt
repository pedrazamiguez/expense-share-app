package es.pedrazamiguez.expenseshareapp.features.authentication.presentation.preview

import androidx.compose.runtime.Composable
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewThemeWrapper
import es.pedrazamiguez.expenseshareapp.features.authentication.presentation.model.AuthenticationUiState
import es.pedrazamiguez.expenseshareapp.features.authentication.presentation.screen.LoginScreen

@PreviewComplete
@Composable
private fun LoginScreenPreview() {
    PreviewThemeWrapper {
        LoginScreen(
            uiState = AuthenticationUiState(
                email = "user@example.com",
                password = "password123"
            )
        )
    }
}
