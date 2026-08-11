package es.pedrazamiguez.splittrip.features.balance.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.navigation.LocalBottomPadding
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.chart.DonutChart
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.chart.DonutChartData
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.DeferredLoadingContainer
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.FlatCard
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.ShimmerLoadingList
import es.pedrazamiguez.splittrip.core.designsystem.presentation.topbar.rememberConnectedScrollBehavior
import es.pedrazamiguez.splittrip.features.balance.R
import es.pedrazamiguez.splittrip.features.balance.presentation.component.CategorySpendingItemRow
import es.pedrazamiguez.splittrip.features.balance.presentation.viewmodel.state.CategorySpendingUiState
import kotlinx.collections.immutable.toImmutableList

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

@Composable
private fun CategorySpendingChart(uiState: CategorySpendingUiState) {
    val chartData = uiState.items.map {
        DonutChartData(
            label = it.categoryName,
            value = it.progress,
            color = it.color
        )
    }.toImmutableList()

    DonutChart(
        data = chartData,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 64.dp, vertical = MaterialTheme.spacing.Screen),
        centerContent = {
            if (uiState.totalFormattedAmount.isNotBlank()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.balances_total_spent),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = uiState.totalFormattedAmount,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    )
}
