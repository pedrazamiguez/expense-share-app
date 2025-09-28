package es.pedrazamiguez.expenseshareapp.ui.settings.presentation.feature

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.LocalNavController
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.screen.SettingsScreen
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsFeature(
    navController: NavHostController = LocalNavController.current,
    viewModel: SettingsViewModel = koinViewModel<SettingsViewModel>()
) {

    SettingsScreen(
        onBack = { navController.popBackStack() },
        onNotificationsClick = { /* TODO */ },
        onLogoutClick = {
            viewModel.signOut {
                navController.navigate("login") {
                    popUpTo("main") { inclusive = true }
                }
            }
        })

}
