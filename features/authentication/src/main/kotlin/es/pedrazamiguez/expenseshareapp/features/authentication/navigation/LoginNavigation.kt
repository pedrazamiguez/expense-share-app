package es.pedrazamiguez.expenseshareapp.features.authentication.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.Routes
import es.pedrazamiguez.expenseshareapp.features.authentication.presentation.feature.LoginFeature

fun NavGraphBuilder.loginGraph(onLoginSuccess: () -> Unit) {
    composable(Routes.LOGIN) {
        LoginFeature(
            onLoginSuccess = onLoginSuccess
        )
    }
}
