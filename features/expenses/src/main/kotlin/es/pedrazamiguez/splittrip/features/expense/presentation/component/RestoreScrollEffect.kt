package es.pedrazamiguez.splittrip.features.expense.presentation.component

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import es.pedrazamiguez.splittrip.features.expense.presentation.viewmodel.state.ExpensesUiState

@Composable
internal fun RestoreScrollEffect(listState: LazyListState, uiState: ExpensesUiState) {
    var hasRestoredScroll by remember { mutableStateOf(false) }
    val expenseGroups = uiState.expenseGroups
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading && !hasRestoredScroll && expenseGroups.isNotEmpty()) {
            if (uiState.scrollPosition > 0 || uiState.scrollOffset > 0) {
                listState.scrollToItem(uiState.scrollPosition, uiState.scrollOffset)
            }
            hasRestoredScroll = true
        }
    }
}
