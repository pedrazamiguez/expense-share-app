package es.pedrazamiguez.expenseshareapp.features.authentication.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import es.pedrazamiguez.expenseshareapp.core.designsystem.extension.asString
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.DoubleTapBackToExitHandler
import es.pedrazamiguez.expenseshareapp.features.authentication.R
import es.pedrazamiguez.expenseshareapp.features.authentication.presentation.model.AuthenticationUiEvent
import es.pedrazamiguez.expenseshareapp.features.authentication.presentation.model.AuthenticationUiState

/** Fraction of the screen width used by the login form. */
private const val FORM_WIDTH_FRACTION = 0.8f

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    uiState: AuthenticationUiState,
    isGoogleSignInAvailable: Boolean = true,
    onEvent: (AuthenticationUiEvent) -> Unit = {},
    onGoogleSignInClick: () -> Unit = {},
    doubleTapBackHandler: DoubleTapBackToExitHandler = remember { DoubleTapBackToExitHandler() },
    navController: NavHostController = rememberNavController()
) {
    val activity = LocalActivity.current
    val anyLoading = uiState.isLoading || uiState.isGoogleLoading

    Scaffold { innerPadding ->
        Box(
            modifier = modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth(FORM_WIDTH_FRACTION).verticalScroll(rememberScrollState()).imePadding()
            ) {
                LoginFormContent(
                    uiState = uiState,
                    anyLoading = anyLoading,
                    onEvent = onEvent
                )

                if (isGoogleSignInAvailable) {
                    GoogleSignInSection(
                        isGoogleLoading = uiState.isGoogleLoading,
                        anyLoading = anyLoading,
                        onGoogleSignInClick = onGoogleSignInClick
                    )
                }

                if (uiState.error != null) {
                    LoginErrorText(errorMessage = uiState.error.asString())
                }
            }
        }
    }

    BackHandler {
        val didPop = navController.popBackStack()
        if (!didPop && doubleTapBackHandler.shouldExit()) {
            activity?.finish()
        }
    }
}

@Composable
private fun LoginFormContent(
    uiState: AuthenticationUiState,
    anyLoading: Boolean,
    onEvent: (AuthenticationUiEvent) -> Unit
) {
    OutlinedTextField(
        value = uiState.email,
        onValueChange = { onEvent(AuthenticationUiEvent.EmailChanged(it)) },
        label = { Text(stringResource(R.string.login_email_label)) },
        singleLine = true,
        enabled = !anyLoading,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = uiState.password,
        onValueChange = { onEvent(AuthenticationUiEvent.PasswordChanged(it)) },
        label = { Text(stringResource(R.string.login_password_label)) },
        visualTransformation = PasswordVisualTransformation(),
        singleLine = true,
        enabled = !anyLoading,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        modifier = Modifier.fillMaxWidth()
    )
    Button(
        onClick = { onEvent(AuthenticationUiEvent.SubmitLogin) },
        enabled = !anyLoading,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Text(stringResource(R.string.login_button))
        }
    }
}

@Composable
private fun GoogleSignInSection(
    isGoogleLoading: Boolean,
    anyLoading: Boolean,
    onGoogleSignInClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(R.string.login_or_divider),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
    OutlinedButton(onClick = onGoogleSignInClick, enabled = !anyLoading, modifier = Modifier.fillMaxWidth()) {
        if (isGoogleLoading) {
            CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
        } else {
            Text(stringResource(R.string.login_google_button))
        }
    }
}

@Composable
private fun LoginErrorText(errorMessage: String) {
    Text(
        text = errorMessage,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(top = 8.dp)
    )
}
