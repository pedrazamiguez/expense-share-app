package es.pedrazamiguez.expenseshareapp.ui.authentication.presentation.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import es.pedrazamiguez.expenseshareapp.ui.authentication.presentation.screen.LoginScreen
import es.pedrazamiguez.expenseshareapp.ui.authentication.presentation.viewmodel.AuthenticationViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginFeature(
    viewModel: AuthenticationViewModel = koinViewModel<AuthenticationViewModel>(),
    onLoginSuccess: () -> Unit = {}
) {

    val uiState by viewModel.uiState.collectAsState()

    LoginScreen(
        uiState = uiState,
        onEvent = { event ->
            viewModel.onEvent(
                event,
                onLoginSuccess
            )
        })

}
