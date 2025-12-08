package es.pedrazamiguez.expenseshareapp.ui.settings.presentation.feature

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.LocalRootNavController
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.Routes
import es.pedrazamiguez.expenseshareapp.core.ui.permission.checkNotificationPermission
import es.pedrazamiguez.expenseshareapp.core.ui.permission.rememberRequestNotificationPermission
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.screen.SettingsScreen
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsFeature(
    navController: NavHostController = LocalRootNavController.current,
    settingsViewModel: SettingsViewModel = koinViewModel<SettingsViewModel>(),
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    val currentCurrency by settingsViewModel.currentCurrency.collectAsState()

    // Track permission state changes
    var permissionCheckTrigger by remember { mutableIntStateOf(0) }
    var hasPermission by remember { mutableStateOf(checkNotificationPermission(context)) }

    // Update hasPermission when trigger changes
    LaunchedEffect(permissionCheckTrigger) {
        hasPermission = checkNotificationPermission(context)
    }

    // Update permission state when returning to this screen
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            permissionCheckTrigger++
        }
    }

    val requestPermission = rememberRequestNotificationPermission { isGranted ->
        // Trigger recomposition by updating the trigger
        permissionCheckTrigger++
    }

    SettingsScreen(
        onBack = { navController.popBackStack() },
        onNotificationsClick = {
            if (!hasPermission) {
                requestPermission()
            } else {
                // Open system settings to allow user to disable notifications
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        },
        hasNotificationPermission = hasPermission,
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
