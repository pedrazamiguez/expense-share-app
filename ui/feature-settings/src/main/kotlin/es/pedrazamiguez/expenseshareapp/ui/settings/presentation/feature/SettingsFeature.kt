package es.pedrazamiguez.expenseshareapp.ui.settings.presentation.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.LocalRootNavController
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.Routes
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.screen.SettingsScreen
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.viewmodel.InstallationIdViewModel
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsFeature(
    navController: NavHostController = LocalRootNavController.current,
    settingsViewModel: SettingsViewModel = koinViewModel<SettingsViewModel>(),
    installationIdViewModel: InstallationIdViewModel = koinViewModel<InstallationIdViewModel>()
) {

    val installationId by installationIdViewModel.installationId.collectAsState()
    val showInstallationIdDialog by installationIdViewModel.showDialog.collectAsState()

    SettingsScreen(
        onBack = { navController.popBackStack() },
        onNotificationsClick = { /* TODO */ },
        onInstallationIdClick = {
            installationIdViewModel.fetchInstallationId()
            installationIdViewModel.showDialog()
        },
        onLogoutClick = {
            settingsViewModel.signOut {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(Routes.MAIN) { inclusive = true }
                }
            }
        },
        installationId = installationId,
        showInstallationIdDialog = showInstallationIdDialog,
        onDismissInstallationDialog = { installationIdViewModel.hideDialog() })

}
