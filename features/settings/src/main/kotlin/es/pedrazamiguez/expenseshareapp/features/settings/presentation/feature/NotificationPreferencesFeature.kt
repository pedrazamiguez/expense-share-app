package es.pedrazamiguez.expenseshareapp.features.settings.presentation.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.Routes
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.scaffold.FeatureScaffold
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.screen.NotificationPreferencesScreen
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.viewmodel.NotificationPreferencesViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun NotificationPreferencesFeature(viewModel: NotificationPreferencesViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FeatureScaffold(currentRoute = Routes.SETTINGS_NOTIFICATIONS) {
        NotificationPreferencesScreen(
            uiState = uiState,
            onEvent = viewModel::onEvent
        )
    }
}
