package es.pedrazamiguez.splittrip.features.settings.presentation.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import es.pedrazamiguez.splittrip.core.common.presentation.asString
import es.pedrazamiguez.splittrip.core.designsystem.navigation.LocalRootNavController
import es.pedrazamiguez.splittrip.core.designsystem.navigation.Routes
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.scaffold.FeatureScaffold
import es.pedrazamiguez.splittrip.core.designsystem.presentation.notification.LocalTopPillController
import es.pedrazamiguez.splittrip.features.settings.presentation.screen.SubscriptionsScreen
import es.pedrazamiguez.splittrip.features.settings.presentation.viewmodel.SubscriptionsViewModel
import es.pedrazamiguez.splittrip.features.settings.presentation.viewmodel.action.SubscriptionsUiAction
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun SubscriptionsFeature(
    navController: NavHostController = LocalRootNavController.current,
    viewModel: SubscriptionsViewModel = koinViewModel<SubscriptionsViewModel>()
) {
    val pillController = LocalTopPillController.current
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.actions.collectLatest { action ->
            when (action) {
                is SubscriptionsUiAction.ShowTopPill -> {
                    pillController.showPill(message = action.message.asString(context))
                }
                SubscriptionsUiAction.NavigateBack -> {
                    navController.popBackStack()
                }
            }
        }
    }

    FeatureScaffold(currentRoute = Routes.SETTINGS_SUBSCRIPTIONS) {
        SubscriptionsScreen(
            uiState = uiState,
            onEvent = viewModel::onEvent
        )
    }
}
