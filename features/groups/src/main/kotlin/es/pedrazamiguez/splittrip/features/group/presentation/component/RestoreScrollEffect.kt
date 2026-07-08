package es.pedrazamiguez.splittrip.features.group.presentation.component

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import es.pedrazamiguez.splittrip.features.group.presentation.viewmodel.state.GroupsUiState

@Composable
internal fun RestoreScrollEffect(listState: LazyListState, uiState: GroupsUiState) {
    var hasRestoredScroll by remember { mutableStateOf(false) }
    val groups = uiState.groups
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading && !hasRestoredScroll && groups.isNotEmpty()) {
            if (uiState.scrollPosition > 0 || uiState.scrollOffset > 0) {
                listState.scrollToItem(uiState.scrollPosition, uiState.scrollOffset)
            }
            hasRestoredScroll = true
        }
    }
}
