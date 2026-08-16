package es.pedrazamiguez.splittrip.features.contribution.presentation.screen.impl

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Edit
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Trash
import es.pedrazamiguez.splittrip.core.designsystem.navigation.LocalTabNavController
import es.pedrazamiguez.splittrip.core.designsystem.navigation.Routes
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.dialog.DestructiveConfirmationDialog
import es.pedrazamiguez.splittrip.core.designsystem.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.splittrip.core.designsystem.presentation.topbar.DynamicTopAppBar
import es.pedrazamiguez.splittrip.features.contribution.R
import es.pedrazamiguez.splittrip.features.contribution.presentation.model.ContributionDetailUiModel
import es.pedrazamiguez.splittrip.features.contribution.presentation.viewmodel.ContributionDetailViewModel
import es.pedrazamiguez.splittrip.features.contribution.presentation.viewmodel.event.ContributionDetailUiEvent
import org.koin.androidx.compose.koinViewModel

class ContributionDetailScreenUiProviderImpl(
    override val route: String = Routes.CONTRIBUTION_DETAIL
) : ScreenUiProvider {

    @OptIn(ExperimentalMaterial3Api::class)
    override val topBar: @Composable () -> Unit = {
        val navController = LocalTabNavController.current
        val backStackEntry = navController.currentBackStackEntry
        if (backStackEntry != null) {
            val vm: ContributionDetailViewModel = koinViewModel(viewModelStoreOwner = backStackEntry)
            val uiState by vm.uiState.collectAsStateWithLifecycle()
            var showDeleteDialog by remember { mutableStateOf(false) }

            val groupId = backStackEntry.arguments?.getString(Routes.CONTRIBUTION_DETAIL_ARG_GROUP_ID)
            val contributionId = backStackEntry.arguments?.getString(Routes.CONTRIBUTION_DETAIL_ARG_CONTRIBUTION_ID)
            val contribution = uiState.contribution

            DynamicTopAppBar(
                title = stringResource(R.string.contribution_detail_title),
                subtitle = contribution?.dateText?.takeIf { it.isNotEmpty() },
                onBack = { navController.popBackStack() },
                actions = {
                    if (canModifyContribution(uiState.isGroupArchived, contribution)) {
                        IconButton(
                            onClick = {
                                if (groupId != null && contributionId != null) {
                                    navController.navigate(
                                        Routes.contributionWizardRoute(groupId, contributionId)
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = TablerIcons.Outline.Edit,
                                contentDescription = stringResource(R.string.contribution_detail_action_edit)
                            )
                        }
                        IconButton(
                            onClick = { showDeleteDialog = true }
                        ) {
                            Icon(
                                imageVector = TablerIcons.Outline.Trash,
                                contentDescription = stringResource(R.string.contribution_detail_action_delete)
                            )
                        }
                    }
                }
            )

            if (showDeleteDialog && contribution != null) {
                DestructiveConfirmationDialog(
                    title = stringResource(R.string.contribution_detail_delete_title),
                    text = stringResource(R.string.contribution_detail_delete_warning),
                    onDismiss = { showDeleteDialog = false },
                    onConfirm = {
                        vm.onEvent(ContributionDetailUiEvent.DeleteConfirmed)
                        showDeleteDialog = false
                    }
                )
            }
        } else {
            DynamicTopAppBar(
                title = "",
                onBack = { navController.popBackStack() }
            )
        }
    }
}

private fun canModifyContribution(
    isArchived: Boolean,
    contribution: ContributionDetailUiModel?
): Boolean {
    if (isArchived || contribution == null) return false
    return !contribution.isLinkedContribution && !contribution.isSettlementContribution
}
