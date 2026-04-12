package es.pedrazamiguez.splittrip.features.group.presentation.screen.impl

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pedrazamiguez.splittrip.core.designsystem.navigation.LocalTabNavController
import es.pedrazamiguez.splittrip.core.designsystem.navigation.Routes
import es.pedrazamiguez.splittrip.core.designsystem.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.splittrip.core.designsystem.presentation.topbar.DynamicTopAppBar
import es.pedrazamiguez.splittrip.features.group.presentation.viewmodel.GroupDetailViewModel
import org.koin.androidx.compose.koinViewModel

class GroupDetailScreenUiProviderImpl(override val route: String = Routes.GROUP_DETAIL) :
    ScreenUiProvider {

    @OptIn(ExperimentalMaterial3Api::class)
    override val topBar: @Composable () -> Unit = {
        val navController = LocalTabNavController.current
        // Use the NavBackStackEntry as ViewModelStoreOwner so we get the SAME ViewModel
        // instance that GroupDetailFeature created inside the NavHost — not an Activity-scoped one.
        val backStackEntry = navController.currentBackStackEntry
        if (backStackEntry != null) {
            val groupDetailViewModel: GroupDetailViewModel = koinViewModel(
                viewModelStoreOwner = backStackEntry
            )
            val uiState by groupDetailViewModel.uiState.collectAsStateWithLifecycle()
            DynamicTopAppBar(
                title = uiState.group?.name ?: "",
                subtitle = uiState.group?.description?.takeIf { it.isNotEmpty() },
                onBack = { navController.popBackStack() }
            )
        } else {
            DynamicTopAppBar(
                title = "",
                onBack = { navController.popBackStack() }
            )
        }
    }
}
