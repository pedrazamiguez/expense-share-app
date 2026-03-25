package es.pedrazamiguez.expenseshareapp.features.group.presentation.component.step

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.input.AsyncSearchableChipSelector
import es.pedrazamiguez.expenseshareapp.features.group.R
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.event.CreateGroupUiEvent
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.state.CreateGroupUiState

/**
 * Step 3: Invite members by searching their email address (optional).
 */
@Composable
fun GroupMembersStep(
    uiState: CreateGroupUiState,
    onEvent: (CreateGroupUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AsyncSearchableChipSelector(
                    searchResults = uiState.memberSearchResults,
                    selectedItems = uiState.selectedMembers,
                    onSearchQueryChanged = { onEvent(CreateGroupUiEvent.MemberSearchQueryChanged(it)) },
                    onItemAdded = { onEvent(CreateGroupUiEvent.MemberSelected(it)) },
                    onItemRemoved = { onEvent(CreateGroupUiEvent.MemberRemoved(it)) },
                    itemKey = { it.userId },
                    itemDisplayText = { it.displayName ?: it.email },
                    itemSecondaryText = { it.email },
                    isSearching = uiState.isSearchingMembers,
                    title = stringResource(R.string.group_field_members),
                    searchLabel = stringResource(R.string.group_member_search),
                    searchPlaceholder = stringResource(R.string.group_member_search_hint),
                    helperText = stringResource(R.string.group_member_search_helper),
                    noResultsText = stringResource(R.string.group_member_search_no_results),
                    chipRemoveContentDescription = stringResource(R.string.group_member_remove),
                    clearSearchContentDescription = stringResource(R.string.group_member_clear_search)
                )
            }
        }
    }
}
