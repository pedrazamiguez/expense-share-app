package es.pedrazamiguez.expenseshareapp.ui.auth.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import es.pedrazamiguez.expenseshareapp.ui.auth.presentation.feature.LoginFeature

const val LOGIN_ROUTE = "login"

fun NavGraphBuilder.loginGraph(onLoginSuccess: () -> Unit) {
    composable(LOGIN_ROUTE) {
        LoginFeature(
            onLoginSuccess = onLoginSuccess
        )
    }
}
