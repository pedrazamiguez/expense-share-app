package es.pedrazamiguez.splittrip.features.balance.presentation.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pedrazamiguez.splittrip.core.common.presentation.asString
import es.pedrazamiguez.splittrip.core.designsystem.navigation.LocalTabNavController
import es.pedrazamiguez.splittrip.core.designsystem.navigation.Routes
import es.pedrazamiguez.splittrip.core.designsystem.presentation.notification.LocalTopPillController
import es.pedrazamiguez.splittrip.core.designsystem.presentation.viewmodel.SharedViewModel
import es.pedrazamiguez.splittrip.features.balance.presentation.screen.BalancesScreen
import es.pedrazamiguez.splittrip.features.balance.presentation.viewmodel.BalancesViewModel
import es.pedrazamiguez.splittrip.features.balance.presentation.viewmodel.action.BalancesUiAction
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun BalancesFeature(
    balancesViewModel: BalancesViewModel = koinViewModel<BalancesViewModel>(),
    sharedViewModel: SharedViewModel = koinViewModel(
        viewModelStoreOwner = LocalContext.current as ViewModelStoreOwner
    )
) {
    val navController = LocalTabNavController.current
    val pillController = LocalTopPillController.current
    val context = LocalContext.current

    val uiState by balancesViewModel.uiState.collectAsStateWithLifecycle()
    val selectedGroupId by sharedViewModel.selectedGroupId.collectAsStateWithLifecycle()

    LaunchedEffect(selectedGroupId) {
        balancesViewModel.setSelectedGroup(selectedGroupId)
    }

    BalancesActionsEffect(actions = balancesViewModel.actions) { action ->
        pillController.showPill(message = action.message.asString(context))
    }

    // Prevent stale data flash during group transition
    val isTransitioning = selectedGroupId != null && selectedGroupId != uiState.groupId
    val effectiveUiState = remember(uiState, isTransitioning) {
        if (isTransitioning) {
            uiState.copy(
                isLoading = true,
                contributions = persistentListOf(),
                cashWithdrawals = persistentListOf(),
                memberBalances = persistentListOf()
            )
        } else {
            uiState
        }
    }

    BalancesScreen(
        uiState = effectiveUiState,
        onEvent = balancesViewModel::onEvent,
        onNavigateToContribution = { navController.navigate(Routes.ADD_CONTRIBUTION) },
        onNavigateToWithdrawal = { navController.navigate(Routes.ADD_CASH_WITHDRAWAL) }
    )
}

/** Collects [BalancesUiAction]s and routes each to [onPillAction] for top-pill display. */
@Composable
private fun BalancesActionsEffect(
    actions: SharedFlow<BalancesUiAction>,
    onPillAction: (BalancesUiAction) -> Unit
) {
    LaunchedEffect(Unit) {
        actions.collectLatest { action ->
            when (action) {
                is BalancesUiAction.ShowLoadError,
                is BalancesUiAction.ShowContributionSuccess,
                is BalancesUiAction.ShowContributionError,
                is BalancesUiAction.ShowDeleteContributionSuccess,
                is BalancesUiAction.ShowDeleteContributionError,
                is BalancesUiAction.ShowDeleteWithdrawalSuccess,
                is BalancesUiAction.ShowDeleteWithdrawalError -> onPillAction(action)
            }
        }
    }
}
