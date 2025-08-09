package es.pedrazamiguez.expenseshareapp.ui.auth

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import org.koin.androidx.compose.koinViewModel

@Composable
fun AuthScreen(viewModel: AuthViewModel = koinViewModel()) {
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    Button(onClick = {
        viewModel.signIn(email.value, password.value) { result ->
            result.onSuccess { uid -> println("Signed in: $uid") }
                .onFailure { println("Error: $it") }
        }
    }) {
        Text("Sign In")
    }
}
