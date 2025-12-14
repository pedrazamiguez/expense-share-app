package es.pedrazamiguez.expenseshareapp.features.expense.presentation.screen

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.EmptyStateView
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.ShimmerLoadingList
import es.pedrazamiguez.expenseshareapp.core.designsystem.theme.LocalAnimatedVisibilityScope
import es.pedrazamiguez.expenseshareapp.core.designsystem.theme.LocalSharedTransitionScope
import es.pedrazamiguez.expenseshareapp.domain.model.Expense
import es.pedrazamiguez.expenseshareapp.features.expense.R
import es.pedrazamiguez.expenseshareapp.features.expense.presentation.component.ExpenseItem

private sealed interface ExpensesUiState {
    data object Loading : ExpensesUiState
    data class Error(val message: String) : ExpensesUiState
    data object Empty : ExpensesUiState
    data class Content(val expenses: List<Expense>) : ExpensesUiState
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ExpensesScreen(
    expenses: List<Expense> = emptyList(),
    loading: Boolean = false,
    errorMessage: String? = null,
    onExpenseClicked: (String) -> Unit = { _ -> }
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    val uiState by remember(loading, errorMessage, expenses) {
        derivedStateOf {
            when {
                loading -> ExpensesUiState.Loading
                errorMessage != null -> ExpensesUiState.Error(errorMessage)
                expenses.isEmpty() -> ExpensesUiState.Empty
                else -> ExpensesUiState.Content(expenses)
            }
        }
    }

    Crossfade(
        targetState = uiState, label = "ExpensesStateTransition", modifier = Modifier.fillMaxSize()
    ) { state ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (state) {
                is ExpensesUiState.Loading -> {
                    ShimmerLoadingList()
                }

                is ExpensesUiState.Error -> {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is ExpensesUiState.Empty -> {
                    EmptyStateView(
                        title = stringResource(R.string.expenses_not_found),
                        icon = Icons.Outlined.Receipt
                    )
                }

                is ExpensesUiState.Content -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items = state.expenses, key = { it.id }) { expense ->
                            if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                                with(sharedTransitionScope) {
                                    ExpenseItem(
                                        modifier = Modifier
                                            .animateItem()
                                            .sharedBounds(
                                                sharedContentState = rememberSharedContentState(
                                                    key = "expense-${expense.id}"
                                                ),
                                                animatedVisibilityScope = animatedVisibilityScope,
                                                resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                                            ), expense = expense, onClick = onExpenseClicked
                                    )
                                }
                            } else {
                                ExpenseItem(
                                    modifier = Modifier.animateItem(),
                                    expense = expense,
                                    onClick = onExpenseClicked
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
