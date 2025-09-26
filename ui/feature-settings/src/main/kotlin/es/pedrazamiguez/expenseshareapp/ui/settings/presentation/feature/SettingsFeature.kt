package es.pedrazamiguez.expenseshareapp.ui.settings.presentation.feature

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.LocalNavController
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.screen.SettingsScreen

@Composable
fun SettingsFeature(
    navController: NavHostController = LocalNavController.current
) {
    SettingsScreen(
        onBack = { navController.popBackStack() },
        onNotificationsClick = { /* TODO */ },
        onLogoutClick = { /* TODO */ })
}
