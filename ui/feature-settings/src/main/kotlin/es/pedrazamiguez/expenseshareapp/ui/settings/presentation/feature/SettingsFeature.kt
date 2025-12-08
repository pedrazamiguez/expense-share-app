package es.pedrazamiguez.expenseshareapp.ui.settings.presentation.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.LocalRootNavController
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.Routes
import es.pedrazamiguez.expenseshareapp.core.ui.permission.hasNotificationPermission
import es.pedrazamiguez.expenseshareapp.core.ui.permission.rememberRequestNotificationPermission
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.screen.SettingsScreen
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsFeature(
    navController: NavHostController = LocalRootNavController.current,
    settingsViewModel: SettingsViewModel = koinViewModel<SettingsViewModel>(),
) {

    val currentCurrency by settingsViewModel.currentCurrency.collectAsState()

    val hasPermission = hasNotificationPermission()
    val requestPermission = rememberRequestNotificationPermission { isGranted ->
        // Actualizar estado local si fuera necesario o mostrar un Snackbar
    }

    SettingsScreen(
        onBack = { navController.popBackStack() },
        onNotificationsClick = {
            if (!hasPermission) {
                requestPermission()
            } else {
                // Opcional: Abrir ajustes del sistema si ya las tiene y quiere quitarlas
                // context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS...))
            }
        },
        currentCurrency = currentCurrency,
        onDefaultCurrencyClick = {
            navController.navigate(Routes.SETTINGS_DEFAULT_CURRENCY)
        },
        onLogoutClick = {
            settingsViewModel.signOut {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(Routes.MAIN) { inclusive = true }
                }
            }
        },
    )

}
