package es.pedrazamiguez.expenseshareapp.features.group.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.LocalBottomPadding
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.dialog.DestructiveConfirmationDialog
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.layout.EmptyStateView
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.layout.ShimmerLoadingList
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.scaffold.ExpressiveFab
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.sheet.ActionBottomSheet
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.sheet.SheetAction
import es.pedrazamiguez.expenseshareapp.features.group.R
import es.pedrazamiguez.expenseshareapp.features.group.presentation.component.SubunitItem
import es.pedrazamiguez.expenseshareapp.features.group.presentation.model.SubunitUiModel
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.event.SubunitManagementUiEvent
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.state.SubunitManagementUiState

@Composable
fun SubunitManagementScreen(
    uiState: SubunitManagementUiState = SubunitManagementUiState(),
    onEvent: (SubunitManagementUiEvent) -> Unit = {}
) {
    // Local UI state for overlays (Action Sheet & Confirmation Dialog)
    var selectedSubunitForMenu by remember { mutableStateOf<SubunitUiModel?>(null) }
    var subunitToDelete by remember { mutableStateOf<SubunitUiModel?>(null) }
    val bottomPadding = LocalBottomPadding.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    ShimmerLoadingList()
                }

                uiState.subunits.isEmpty() -> {
                    EmptyStateView(
                        title = stringResource(R.string.subunit_empty_state),
                        icon = Icons.Outlined.Groups
                    )
                }

                else -> {
                    val fabExtraPadding = 80.dp
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            top = 16.dp,
                            end = 16.dp,
                            bottom = 16.dp + bottomPadding + fabExtraPadding
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.subunits,
                            key = { it.id }
                        ) { subunit ->
                            SubunitItem(
                                subunitUiModel = subunit,
                                modifier = Modifier.animateItem(),
                                onLongClick = { selectedSubunitForMenu = subunit }
                            )
                        }
                    }
                }
            }

            // FAB — only show when data is loaded
            if (!uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .padding(bottom = bottomPadding),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    ExpressiveFab(
                        onClick = { onEvent(SubunitManagementUiEvent.CreateSubunit) },
                        icon = Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.subunit_create),
                        sharedTransitionKey = CREATE_EDIT_SUBUNIT_SHARED_ELEMENT_KEY
                    )
                }
            }
        }
    }

    // 1. Action Sheet (Edit/Delete)
    selectedSubunitForMenu?.let { subunit ->
        ActionBottomSheet(
            title = stringResource(R.string.subunit_actions_title, subunit.name),
            icon = Icons.Outlined.Groups,
            actions = listOf(
                SheetAction(
                    text = stringResource(R.string.action_edit_subunit),
                    icon = Icons.Outlined.Edit,
                    onClick = {
                        onEvent(SubunitManagementUiEvent.EditSubunit(subunit.id))
                        selectedSubunitForMenu = null
                    }
                ),
                SheetAction(
                    text = stringResource(R.string.action_delete_subunit),
                    icon = Icons.Outlined.Delete,
                    onClick = {
                        subunitToDelete = subunit
                        selectedSubunitForMenu = null
                    },
                    isDestructive = true
                )
            ),
            onDismiss = { selectedSubunitForMenu = null }
        )
    }

    // 2. Confirmation Dialog
    subunitToDelete?.let { subunit ->
        DestructiveConfirmationDialog(
            title = stringResource(R.string.subunit_delete_title),
            text = stringResource(R.string.subunit_delete_warning, subunit.name),
            onDismiss = { subunitToDelete = null },
            onConfirm = {
                onEvent(SubunitManagementUiEvent.ConfirmDeleteSubunit(subunit.id))
                subunitToDelete = null
            }
        )
    }
}
