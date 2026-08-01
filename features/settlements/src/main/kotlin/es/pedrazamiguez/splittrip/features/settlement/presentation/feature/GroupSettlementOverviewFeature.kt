package es.pedrazamiguez.splittrip.features.settlement.presentation.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pedrazamiguez.splittrip.core.common.presentation.asString
import es.pedrazamiguez.splittrip.core.designsystem.navigation.LocalTabNavController
import es.pedrazamiguez.splittrip.core.designsystem.presentation.notification.LocalTopPillController
import es.pedrazamiguez.splittrip.features.settlement.presentation.screen.GroupSettlementOverviewScreen
import es.pedrazamiguez.splittrip.features.settlement.presentation.viewmodel.GroupSettlementOverviewViewModel
import es.pedrazamiguez.splittrip.features.settlement.presentation.viewmodel.action.GroupSettlementOverviewUiAction
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun GroupSettlementOverviewFeature(
    groupId: String,
    groupSettlementOverviewViewModel: GroupSettlementOverviewViewModel = koinViewModel()
) {
    val navController = LocalTabNavController.current
    val pillController = LocalTopPillController.current
    val context = LocalContext.current

    val uiState by groupSettlementOverviewViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(groupId) {
        groupSettlementOverviewViewModel.setGroupId(groupId)
    }

    LaunchedEffect(Unit) {
        groupSettlementOverviewViewModel.actions.collectLatest { action ->
            when (action) {
                is GroupSettlementOverviewUiAction.ShowError -> {
                    pillController.showPill(message = action.message.asString(context))
                }
                is GroupSettlementOverviewUiAction.ShowSuccess -> {
                    pillController.showPill(message = action.message.asString(context))
                }
                GroupSettlementOverviewUiAction.NavigateBack -> {
                    navController.popBackStack()
                }
            }
        }
    }

    GroupSettlementOverviewScreen(
        uiState = uiState,
        onEvent = groupSettlementOverviewViewModel::onEvent
    )
}
