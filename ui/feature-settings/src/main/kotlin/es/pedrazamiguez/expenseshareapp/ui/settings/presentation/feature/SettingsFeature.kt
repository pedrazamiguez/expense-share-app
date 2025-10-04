package es.pedrazamiguez.expenseshareapp.ui.settings.presentation.feature

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.LocalRootNavController
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.Routes
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.screen.SettingsScreen
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsFeature(
    navController: NavHostController = LocalRootNavController.current,
    viewModel: SettingsViewModel = koinViewModel<SettingsViewModel>()
) {

    SettingsScreen(
        onBack = { navController.popBackStack() },
        onNotificationsClick = { /* TODO */ },
        onLogoutClick = {
            viewModel.signOut {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(Routes.MAIN) { inclusive = true }
                }
            }
        })

}
