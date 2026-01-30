package es.pedrazamiguez.expenseshareapp.features.balance.presentation.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.BalanceUiAction
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen.BalancesScreen
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.BalanceViewModel
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun BalancesFeature(
    balanceViewModel: BalanceViewModel = koinViewModel<BalanceViewModel>(),
    onNavigateToGroup: (String) -> Unit
) {

    val state by balanceViewModel.uiState.collectAsStateWithLifecycle()

    // Collect actions as events (not state) to avoid re-triggering on config changes
    LaunchedEffect(Unit) {
        balanceViewModel.actions.collectLatest { action ->
            when (action) {
                is BalanceUiAction.NavigateToGroup -> onNavigateToGroup(action.groupId)
                is BalanceUiAction.ShowError -> {
                    // Show snackbar / toast
                }
            }
        }
    }

    BalancesScreen(
        uiState = state,
        onEvent = balanceViewModel::onEvent,
        onNavigateToGroup = onNavigateToGroup
    )

}
