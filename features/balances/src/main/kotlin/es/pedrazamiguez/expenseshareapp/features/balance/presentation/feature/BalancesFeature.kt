package es.pedrazamiguez.expenseshareapp.features.balance.presentation.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.model.BalanceUiAction
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen.BalancesScreen
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.BalanceViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun BalancesFeature(
    balanceViewModel: BalanceViewModel = koinViewModel<BalanceViewModel>(),
    onNavigateToGroup: (String) -> Unit
) {

    val state by balanceViewModel.uiState.collectAsStateWithLifecycle()
    val actions = balanceViewModel.actions.collectAsStateWithLifecycle(BalanceUiAction.None)

    LaunchedEffect(actions.value) {
        when (val action = actions.value) {
            is BalanceUiAction.NavigateToGroup -> onNavigateToGroup(action.groupId)
            is BalanceUiAction.ShowError -> {
                // Show snackbar / toast
            }

            BalanceUiAction.None -> {
                // Noop
            }
        }
    }

    BalancesScreen(
        uiState = state,
        onEvent = balanceViewModel::onEvent,
        onNavigateToGroup = onNavigateToGroup
    )

}
