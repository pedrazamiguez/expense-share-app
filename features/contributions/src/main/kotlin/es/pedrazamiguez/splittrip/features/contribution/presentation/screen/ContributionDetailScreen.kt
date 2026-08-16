package es.pedrazamiguez.splittrip.features.contribution.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Wallet
import es.pedrazamiguez.splittrip.core.designsystem.navigation.LocalBottomPadding
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.EmptyStateView
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.ShimmerLoadingList
import es.pedrazamiguez.splittrip.core.designsystem.presentation.topbar.rememberConnectedScrollBehavior
import es.pedrazamiguez.splittrip.features.contribution.R
import es.pedrazamiguez.splittrip.features.contribution.presentation.viewmodel.state.ContributionDetailUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContributionDetailScreen(
    uiState: ContributionDetailUiState,
    modifier: Modifier = Modifier
) {
    val bottomPadding = LocalBottomPadding.current
    val scrollBehavior = rememberConnectedScrollBehavior()

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        when {
            uiState.isLoading -> {
                ShimmerLoadingList()
            }
            uiState.hasError || uiState.contribution == null -> {
                EmptyStateView(
                    title = stringResource(R.string.contribution_detail_error_loading),
                    icon = TablerIcons.Outline.Wallet
                )
            }
            else -> {
                ContributionDetailContent(
                    contribution = uiState.contribution,
                    bottomPadding = bottomPadding
                )
            }
        }
    }
}
