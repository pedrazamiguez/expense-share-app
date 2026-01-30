package es.pedrazamiguez.expenseshareapp.features.balance.presentation.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen.BalancesScreen
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.BalancesViewModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.action.BalancesUiAction
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.event.BalancesUiEvent
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun BalancesFeature(
    balancesViewModel: BalancesViewModel = koinViewModel<BalancesViewModel>(),
    onNavigateToGroup: (String) -> Unit
) {

    val state by balancesViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        balancesViewModel.onEvent(BalancesUiEvent.LoadBalances)
    }

    // Collect actions as events (not state) to avoid re-triggering on config changes
    LaunchedEffect(Unit) {
        balancesViewModel.actions.collectLatest { action ->
            when (action) {
                is BalancesUiAction.NavigateToGroup -> onNavigateToGroup(action.groupId)
                is BalancesUiAction.ShowError -> {
                    // Show snackbar / toast
                }
            }
        }
    }

    BalancesScreen(
        uiState = state,
        onEvent = balancesViewModel::onEvent,
        onNavigateToGroup = onNavigateToGroup
    )

}
