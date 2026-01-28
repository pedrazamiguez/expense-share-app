package es.pedrazamiguez.expenseshareapp.features.expense.presentation.screen

import androidx.compose.animation.ExperimentalSharedTransitionApi
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
import androidx.compose.material.icons.outlined.Receipt
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
import es.pedrazamiguez.expenseshareapp.core.designsystem.constant.UiConstants
import es.pedrazamiguez.expenseshareapp.core.designsystem.extension.sharedElementAnimation
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.LocalBottomPadding
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.EmptyStateView
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.ExpressiveFab
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.ShimmerLoadingList
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.topbar.rememberConnectedScrollBehavior
import es.pedrazamiguez.expenseshareapp.core.designsystem.transition.LocalAnimatedVisibilityScope
import es.pedrazamiguez.expenseshareapp.core.designsystem.transition.LocalSharedTransitionScope
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.component.ExpenseItem
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.ListGroupExpensesUiState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun ExpensesScreen(
    uiState: ListGroupExpensesUiState = ListGroupExpensesUiState(),
    onExpenseClicked: (String) -> Unit = { _ -> },
    onAddExpenseClick: () -> Unit = {},
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
            .debounce(UiConstants.SCROLL_POSITION_DEBOUNCE_MS)
            .collect { (index, offset) ->
                onScrollPositionChanged(index, offset)
            }
    }

    // Auto-scroll to top when a new expense is added (list size increases)
    LaunchedEffect(uiState.expenses.size) {
        if (uiState.expenses.isNotEmpty() && !uiState.isLoading) {
            // Only scroll if we're not already at the top
            if (listState.firstVisibleItemIndex > 0) {
                listState.animateScrollToItem(0)
            }
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

                uiState.expenses.isEmpty() -> {
                    EmptyStateView(
                        title = stringResource(R.string.expenses_not_found),
                        icon = Icons.Outlined.Receipt
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

                        items(items = uiState.expenses, key = { it.id }) { expense ->
                            ExpenseItem(
                                expenseUiModel = expense,
                                modifier = Modifier
                                    .animateItem()
                                    .sharedElementAnimation(
                                        key = "expense-${expense.id}",
                                        sharedTransitionScope = sharedTransitionScope,
                                        animatedVisibilityScope = animatedVisibilityScope
                                    ),
                                onClick = onExpenseClicked
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
                    onClick = onAddExpenseClick,
                    icon = Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.expenses_add),
                    sharedTransitionKey = ADD_EXPENSE_SHARED_ELEMENT_KEY
                )
            }
        }
    }
}
