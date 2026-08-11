package es.pedrazamiguez.splittrip.features.balance.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.navigation.LocalBottomPadding
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.DeferredLoadingContainer
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.FlatCard
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.ShimmerLoadingList
import es.pedrazamiguez.splittrip.core.designsystem.presentation.topbar.rememberConnectedScrollBehavior
import es.pedrazamiguez.splittrip.features.balance.presentation.component.CategorySpendingItemRow
import es.pedrazamiguez.splittrip.features.balance.presentation.viewmodel.state.CategorySpendingUiState

@Composable
fun CategorySpendingScreen(
    uiState: CategorySpendingUiState,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = rememberConnectedScrollBehavior()
    val bottomPadding = LocalBottomPadding.current

    DeferredLoadingContainer(
        isLoading = uiState.isLoading,
        loadingContent = {
            ShimmerLoadingList(
                modifier = modifier.fillMaxSize(),
                itemCount = 5
            )
        }
    ) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(bottom = bottomPadding)
        ) {
            item {
                CategorySpendingChart(uiState = uiState)
            }

            item {
                FlatCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.spacing.Medium)
                ) {
                    Column {
                        uiState.items.forEach { item ->
                            CategorySpendingItemRow(
                                item = item,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = MaterialTheme.spacing.Default,
                                        vertical = MaterialTheme.spacing.Medium
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}
