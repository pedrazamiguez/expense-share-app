package es.pedrazamiguez.expenseshareapp.features.balance.presentation.feature

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pedrazamiguez.expenseshareapp.core.common.presentation.asString
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.LocalTabNavController
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.Routes
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.snackbar.LocalSnackbarController
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.viewmodel.SharedViewModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen.BalancesScreen
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.BalancesViewModel
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.action.BalancesUiAction
import kotlinx.collections.immutable.persistentListOf
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
    val snackbarController = LocalSnackbarController.current
    val context = LocalContext.current

    val uiState by balancesViewModel.uiState.collectAsStateWithLifecycle()
    val selectedGroupId by sharedViewModel.selectedGroupId.collectAsStateWithLifecycle()

    LaunchedEffect(selectedGroupId) {
        balancesViewModel.setSelectedGroup(selectedGroupId)
    }

    // Collect and handle UiActions
    LaunchedEffect(Unit) {
        balancesViewModel.actions.collectLatest { action ->
            when (action) {
                is BalancesUiAction.ShowLoadError -> {
                    snackbarController.showSnackbar(
                        message = action.message.asString(context),
                        duration = SnackbarDuration.Long
                    )
                }

                is BalancesUiAction.ShowContributionSuccess -> {
                    snackbarController.showSnackbar(
                        message = action.message.asString(context),
                        duration = SnackbarDuration.Short
                    )
                }

                is BalancesUiAction.ShowContributionError -> {
                    snackbarController.showSnackbar(
                        message = action.message.asString(context),
                        duration = SnackbarDuration.Long
                    )
                }
            }
        }
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
        onNavigateToContribution = {
            navController.navigate(Routes.ADD_CONTRIBUTION)
        },
        onNavigateToWithdrawal = {
            navController.navigate(Routes.ADD_CASH_WITHDRAWAL)
        }
    )
}
