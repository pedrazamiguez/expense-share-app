package es.pedrazamiguez.splittrip.features.settlement.presentation.feature

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Wallet
import es.pedrazamiguez.splittrip.core.designsystem.navigation.SharedElementKeys
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.EmptyStateView
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.ShimmerLoadingList
import es.pedrazamiguez.splittrip.core.designsystem.presentation.viewmodel.SharedViewModel
import es.pedrazamiguez.splittrip.core.designsystem.transition.SharedTransitionSurface
import es.pedrazamiguez.splittrip.features.settlement.R
import es.pedrazamiguez.splittrip.features.settlement.presentation.component.YourPositionContent
import es.pedrazamiguez.splittrip.features.settlement.presentation.viewmodel.YourPositionViewModel
import es.pedrazamiguez.splittrip.features.settlement.presentation.viewmodel.event.YourPositionUiEvent
import org.koin.androidx.compose.koinViewModel

@Composable
fun YourPositionFeature(
    yourPositionViewModel: YourPositionViewModel = koinViewModel(),
    sharedViewModel: SharedViewModel = koinViewModel(
        viewModelStoreOwner = LocalContext.current as ViewModelStoreOwner
    ),
    modifier: Modifier = Modifier
) {
    val uiState by yourPositionViewModel.uiState.collectAsStateWithLifecycle()
    val selectedGroupId by sharedViewModel.selectedGroupId.collectAsStateWithLifecycle()

    LaunchedEffect(selectedGroupId) {
        yourPositionViewModel.setSelectedGroup(selectedGroupId)
    }

    SharedTransitionSurface(
        sharedElementKey = SharedElementKeys.YOUR_POSITION,
        modifier = modifier
    ) {
        when {
            uiState.isLoading -> ShimmerLoadingList(modifier = Modifier.fillMaxSize())
            uiState.personalPosition != null -> {
                YourPositionContent(
                    personalPosition = uiState.personalPosition!!,
                    isCashBreakdownVisible = uiState.isCashBreakdownVisible,
                    onShowCashBreakdown = { yourPositionViewModel.onEvent(YourPositionUiEvent.ShowCashBreakdown) },
                    onDismissCashBreakdown = {
                        yourPositionViewModel.onEvent(YourPositionUiEvent.DismissCashBreakdown)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> EmptyStateView(
                icon = TablerIcons.Outline.Wallet,
                title = stringResource(R.string.your_position_empty_title),
                description = stringResource(R.string.your_position_empty_description),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
