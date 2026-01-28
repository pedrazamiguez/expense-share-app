package es.pedrazamiguez.expenseshareapp.features.authentication.presentation.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pedrazamiguez.expenseshareapp.features.authentication.presentation.screen.LoginScreen
import es.pedrazamiguez.expenseshareapp.features.authentication.presentation.viewmodel.AuthenticationViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginFeature(
    viewModel: AuthenticationViewModel = koinViewModel<AuthenticationViewModel>(),
    onLoginSuccess: () -> Unit = {}
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LoginScreen(
        uiState = uiState,
        onEvent = { event ->
            viewModel.onEvent(
                event,
                onLoginSuccess
            )
        })

}
