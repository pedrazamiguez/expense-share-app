package es.pedrazamiguez.expenseshareapp.ui.authentication.presentation.feature

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import es.pedrazamiguez.expenseshareapp.ui.authentication.presentation.viewmodel.AuthViewModel
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

@Composable
fun LoginFeature(viewModel: AuthViewModel = koinViewModel(), onLoginSuccess: () -> Unit = {}) {
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    Button(onClick = {
        viewModel.signIn(email.value, password.value) { result ->
            result.onSuccess { uid ->
                Timber.d("Signed in: $uid")
                onLoginSuccess()
            }.onFailure { Timber.e(it, "Sign in failed") }
        }
    }) {
        Text("Sign In")
    }
}
