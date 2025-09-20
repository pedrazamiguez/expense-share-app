package es.pedrazamiguez.expenseshareapp.ui.balance.presentation.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pedrazamiguez.expenseshareapp.ui.balance.presentation.model.BalanceUiAction
import es.pedrazamiguez.expenseshareapp.ui.balance.presentation.screen.BalancesScreen
import es.pedrazamiguez.expenseshareapp.ui.balance.presentation.viewmodel.BalanceViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun BalancesFeature(
    viewModel: BalanceViewModel = koinViewModel(), onNavigateToGroup: (String) -> Unit
) {

    val state by viewModel.uiState.collectAsState()
    val actions = viewModel.actions.collectAsStateWithLifecycle(BalanceUiAction.None)

    LaunchedEffect(actions.value) {
        when (val action = actions.value) {
            is BalanceUiAction.NavigateToGroup -> onNavigateToGroup(action.groupId)
            is BalanceUiAction.ShowError -> {
                // Show snackbar / toast
            }

            BalanceUiAction.None -> {}
        }
    }

    BalancesScreen(
        state = state, onEvent = viewModel::onEvent, onNavigateToGroup = onNavigateToGroup
    )

}
