package es.pedrazamiguez.expenseshareapp.features.group.presentation.screen

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.EmptyStateView
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.ExpressiveFab
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.ShimmerLoadingList
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.topbar.rememberConnectedScrollBehavior
import es.pedrazamiguez.expenseshareapp.core.designsystem.transition.LocalAnimatedVisibilityScope
import es.pedrazamiguez.expenseshareapp.core.designsystem.transition.LocalSharedTransitionScope
import es.pedrazamiguez.expenseshareapp.domain.model.Group
import es.pedrazamiguez.expenseshareapp.features.group.R
import es.pedrazamiguez.expenseshareapp.features.group.presentation.component.GroupItem

private sealed interface GroupsUiState {
    data object Loading : GroupsUiState
    data class Error(val message: String) : GroupsUiState
    data object Empty : GroupsUiState
    data class Content(val groups: List<Group>, val selectedGroupId: String?) : GroupsUiState
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    groups: List<Group> = emptyList(),
    loading: Boolean = false,
    errorMessage: String? = null,
    selectedGroupId: String? = null,
    onGroupClicked: (String) -> Unit = { _ -> },
    onCreateGroupClick: () -> Unit = {}
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    // Connect scroll behavior to the top app bar
    val scrollBehavior = rememberConnectedScrollBehavior()

    val uiState by remember(loading, errorMessage, groups, selectedGroupId) {
        derivedStateOf {
            when {
                loading -> GroupsUiState.Loading
                errorMessage != null -> GroupsUiState.Error(errorMessage)
                groups.isEmpty() -> GroupsUiState.Empty
                else -> GroupsUiState.Content(groups, selectedGroupId)
            }
        }
    }

    Crossfade(
        targetState = uiState, label = "GroupsStateTransition", modifier = Modifier.fillMaxSize()
    ) { state ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (state) {
                    is GroupsUiState.Loading -> {
                        ShimmerLoadingList()
                    }

                    is GroupsUiState.Error -> {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    is GroupsUiState.Empty -> {
                        EmptyStateView(
                            title = stringResource(R.string.groups_not_found),
                            icon = Icons.Outlined.Groups
                        )
                    }

                    is GroupsUiState.Content -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .nestedScroll(scrollBehavior.nestedScrollConnection),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(items = state.groups, key = { it.id }) { group ->
                                val sharedModifier =
                                    if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                                        with(sharedTransitionScope) {
                                            Modifier.sharedBounds(
                                                sharedContentState = rememberSharedContentState(
                                                    key = "group-${group.id}"
                                                ),
                                                animatedVisibilityScope = animatedVisibilityScope,
                                                resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                                            )
                                        }
                                    } else {
                                        Modifier
                                    }

                                GroupItem(
                                    modifier = Modifier
                                        .animateItem()
                                        .then(sharedModifier),
                                    group = group,
                                    isSelected = group.id == state.selectedGroupId,
                                    onClick = onGroupClicked
                                )
                            }
                        }
                    }
                }

                // FAB positioned at bottom end - inside the Box to share AnimatedVisibilityScope
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    ExpressiveFab(
                        onClick = onCreateGroupClick,
                        icon = Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.groups_create),
                        sharedTransitionKey = CREATE_GROUP_SHARED_ELEMENT_KEY
                    )
                }
            }
        }
    }

}
