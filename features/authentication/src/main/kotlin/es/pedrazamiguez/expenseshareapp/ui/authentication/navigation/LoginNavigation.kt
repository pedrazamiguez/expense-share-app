package es.pedrazamiguez.expenseshareapp.ui.authentication.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.Routes
import es.pedrazamiguez.expenseshareapp.ui.authentication.presentation.feature.LoginFeature

fun NavGraphBuilder.loginGraph(onLoginSuccess: () -> Unit) {
    composable(Routes.LOGIN) {
        LoginFeature(
            onLoginSuccess = onLoginSuccess
        )
    }
}
