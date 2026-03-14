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
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.constant.UiConstants
import es.pedrazamiguez.expenseshareapp.core.designsystem.extension.sharedElementAnimation
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.LocalBottomPadding
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.dialog.DestructiveConfirmationDialog
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.layout.DeferredLoadingContainer
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.layout.EmptyStateView
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.layout.ShimmerLoadingList
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.scaffold.ExpressiveFab
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.sheet.ActionBottomSheet
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.sheet.SheetAction
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.topbar.rememberConnectedScrollBehavior
import es.pedrazamiguez.expenseshareapp.core.designsystem.transition.LocalAnimatedVisibilityScope
import es.pedrazamiguez.expenseshareapp.core.designsystem.transition.LocalSharedTransitionScope
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.component.DateHeaderItem
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.component.ExpenseItem
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.model.ExpenseUiModel
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.viewmodel.state.ExpensesUiState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun ExpensesScreen(
    uiState: ExpensesUiState = ExpensesUiState(),
    onExpenseClicked: (String) -> Unit = { _ -> },
    onAddExpenseClick: () -> Unit = {},
    onScrollPositionChanged: (Int, Int) -> Unit = { _, _ -> },
    onDeleteExpense: (expenseId: String) -> Unit = {}
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current
    val bottomPadding = LocalBottomPadding.current
    val scrollBehavior = rememberConnectedScrollBehavior()

    // Local UI State for overlays (Action Sheet & Confirmation Dialog)
    var selectedExpenseForMenu by remember { mutableStateOf<ExpenseUiModel?>(null) }
    var expenseToDelete by remember { mutableStateOf<ExpenseUiModel?>(null) }

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
    val totalExpenseCount = uiState.expenseGroups.sumOf { it.expenses.size }
    LaunchedEffect(totalExpenseCount) {
        if (totalExpenseCount > 0 && !uiState.isLoading) {
            // Only scroll if we're not already at the top
            if (listState.firstVisibleItemIndex > 0) {
                listState.animateScrollToItem(0)
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            DeferredLoadingContainer(
                isLoading = uiState.isLoading,
                loadingContent = { ShimmerLoadingList() }
            ) {
                when {
                    uiState.errorMessage != null -> {
                        Text(
                            text = uiState.errorMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    uiState.isEmpty -> {
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
                            uiState.expenseGroups.forEach { dateGroup ->
                                stickyHeader(key = "header-${dateGroup.dateText}") {
                                    DateHeaderItem(
                                        dateText = dateGroup.dateText,
                                        formattedDayTotal = dateGroup.formattedDayTotal
                                    )
                                }

                                items(
                                    items = dateGroup.expenses,
                                    key = { it.id }
                                ) { expense ->
                                    ExpenseItem(
                                        expenseUiModel = expense,
                                        modifier = Modifier
                                            .animateItem()
                                            .sharedElementAnimation(
                                                key = "expense-${expense.id}",
                                                sharedTransitionScope = sharedTransitionScope,
                                                animatedVisibilityScope = animatedVisibilityScope
                                            ),
                                        onClick = onExpenseClicked,
                                        onLongClick = { selectedExpenseForMenu = expense }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(bottom = bottomPadding),
                contentAlignment = Alignment.BottomEnd
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

    // 1. Action Sheet (Edit/Delete)
    selectedExpenseForMenu?.let { expense ->
        ActionBottomSheet(
            title = stringResource(R.string.expense_actions_title, expense.title),
            icon = Icons.Outlined.Receipt,
            actions = listOf(
                SheetAction(
                    text = stringResource(R.string.action_edit_expense),
                    icon = Icons.Outlined.Edit,
                    onClick = {
                        // TODO: Navigate to edit screen
                        selectedExpenseForMenu = null
                    }
                ),
                SheetAction(
                    text = stringResource(R.string.action_delete_expense),
                    icon = Icons.Outlined.Delete,
                    onClick = {
                        // Close sheet, prepare for confirmation dialog
                        expenseToDelete = expense
                        selectedExpenseForMenu = null
                    },
                    isDestructive = true
                )
            ),
            onDismiss = { selectedExpenseForMenu = null }
        )
    }

    // 2. Confirmation Dialog
    expenseToDelete?.let { expense ->
        DestructiveConfirmationDialog(
            title = stringResource(R.string.expense_delete_title),
            text = stringResource(R.string.expense_delete_warning, expense.title),
            onDismiss = { expenseToDelete = null },
            onConfirm = {
                onDeleteExpense(expense.id)
                expenseToDelete = null
            }
        )
    }
}
