package es.pedrazamiguez.splittrip.features.settings.presentation.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pedrazamiguez.splittrip.core.common.presentation.asString
import es.pedrazamiguez.splittrip.core.designsystem.navigation.LocalRootNavController
import es.pedrazamiguez.splittrip.core.designsystem.navigation.Routes
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.scaffold.FeatureScaffold
import es.pedrazamiguez.splittrip.core.designsystem.presentation.notification.LocalTopPillController
import es.pedrazamiguez.splittrip.features.settings.presentation.mapper.AccountSecurityUiMapper
import es.pedrazamiguez.splittrip.features.settings.presentation.screen.AccountSecurityScreen
import es.pedrazamiguez.splittrip.features.settings.presentation.viewmodel.AccountSecurityViewModel
import es.pedrazamiguez.splittrip.features.settings.presentation.viewmodel.action.AccountSecurityUiAction
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun AccountSecurityFeature(
    viewModel: AccountSecurityViewModel = koinViewModel<AccountSecurityViewModel>(),
    accountSecurityUiMapper: AccountSecurityUiMapper = koinInject<AccountSecurityUiMapper>()
) {
    val pillController = LocalTopPillController.current
    val context = LocalContext.current
    val navController = LocalRootNavController.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.actions.collectLatest { action ->
            when (action) {
                is AccountSecurityUiAction.ShowTopPill -> {
                    pillController.showPill(message = action.message.asString(context))
                }
                is AccountSecurityUiAction.NavigateToRoute -> {
                    navController.navigate(action.route)
                }
                AccountSecurityUiAction.NavigateBack -> {
                    navController.popBackStack()
                }
            }
        }
    }

    FeatureScaffold(currentRoute = Routes.SETTINGS_SECURITY) {
        AccountSecurityScreen(
            uiState = uiState,
            uiMapper = accountSecurityUiMapper,
            onEvent = viewModel::onEvent
        )
    }
}
