package es.pedrazamiguez.expenseshareapp.features.group.presentation.screen

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.LocalBottomPadding
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.EmptyStateView
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.ExpressiveFab
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.ShimmerLoadingList
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.topbar.rememberConnectedScrollBehavior
import es.pedrazamiguez.expenseshareapp.core.designsystem.transition.LocalAnimatedVisibilityScope
import es.pedrazamiguez.expenseshareapp.core.designsystem.transition.LocalSharedTransitionScope
import es.pedrazamiguez.expenseshareapp.features.group.R
import es.pedrazamiguez.expenseshareapp.features.group.presentation.component.GroupItem
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.state.ListUserGroupsUiState

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun GroupsScreen(
    uiState: ListUserGroupsUiState = ListUserGroupsUiState(),
    selectedGroupId: String? = null,
    onGroupClicked: (String) -> Unit = { _ -> },
    onCreateGroupClick: () -> Unit = {},
    onScrollPositionChanged: (Int, Int) -> Unit = { _, _ -> }
) {

    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current
    val bottomPadding = LocalBottomPadding.current
    val scrollBehavior = rememberConnectedScrollBehavior()
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = uiState.scrollPosition,
        initialFirstVisibleItemScrollOffset = uiState.scrollOffset
    )

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .debounce(300)
            .collect { (index, offset) ->
                onScrollPositionChanged(index, offset)
            }
    }

    Surface(
        modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
    ) {

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    ShimmerLoadingList()
                }

                uiState.errorMessage != null -> {
                    Text(
                        text = uiState.errorMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.groups.isEmpty() -> {
                    EmptyStateView(
                        title = stringResource(R.string.groups_not_found),
                        icon = Icons.Outlined.Groups
                    )
                }

                else -> {
                    val fabExtraPadding = 80.dp
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(scrollBehavior.nestedScrollConnection),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            top = 16.dp,
                            end = 16.dp,
                            bottom = 16.dp + bottomPadding + fabExtraPadding
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        items(items = uiState.groups, key = { it.id }) { group ->
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
                                groupUiModel = group,
                                isSelected = group.id == selectedGroupId,
                                onClick = onGroupClicked
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(bottom = bottomPadding), contentAlignment = Alignment.BottomEnd
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
