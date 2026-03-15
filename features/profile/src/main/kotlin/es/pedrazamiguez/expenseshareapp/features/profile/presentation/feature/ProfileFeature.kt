package es.pedrazamiguez.expenseshareapp.features.profile.presentation.feature

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pedrazamiguez.expenseshareapp.core.common.presentation.asString
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.snackbar.LocalSnackbarController
import es.pedrazamiguez.expenseshareapp.features.profile.presentation.screen.ProfileScreen
import es.pedrazamiguez.expenseshareapp.features.profile.presentation.viewmodel.ProfileViewModel
import es.pedrazamiguez.expenseshareapp.features.profile.presentation.viewmodel.action.ProfileUiAction
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileFeature(profileViewModel: ProfileViewModel = koinViewModel<ProfileViewModel>()) {
    val snackbarController = LocalSnackbarController.current
    val context = LocalContext.current

    val uiState by profileViewModel.uiState.collectAsStateWithLifecycle()

    // Collect and handle UiActions
    LaunchedEffect(Unit) {
        profileViewModel.actions.collectLatest { action ->
            when (action) {
                is ProfileUiAction.ShowError -> {
                    snackbarController.showSnackbar(
                        message = action.message.asString(context),
                        duration = SnackbarDuration.Long
                    )
                }
            }
        }
    }

    ProfileScreen(
        uiState = uiState,
        onEvent = profileViewModel::onEvent
    )
}
