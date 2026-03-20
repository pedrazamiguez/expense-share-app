package es.pedrazamiguez.expenseshareapp.features.group.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.extension.asString
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.LocalBottomPadding
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.layout.ShimmerLoadingList
import es.pedrazamiguez.expenseshareapp.core.designsystem.transition.SharedTransitionSurface
import es.pedrazamiguez.expenseshareapp.features.group.R
import es.pedrazamiguez.expenseshareapp.features.group.presentation.model.MemberUiModel
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.event.CreateEditSubunitUiEvent
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.state.CreateEditSubunitUiState
import kotlinx.collections.immutable.ImmutableList

/**
 * Shared element transition key for the Create Sub-unit FAB -> Screen transition.
 */
const val CREATE_EDIT_SUBUNIT_SHARED_ELEMENT_KEY = "create_edit_subunit_container"

@Composable
fun CreateEditSubunitScreen(
    uiState: CreateEditSubunitUiState = CreateEditSubunitUiState(),
    onEvent: (CreateEditSubunitUiEvent) -> Unit = {}
) {
    SharedTransitionSurface(sharedElementKey = CREATE_EDIT_SUBUNIT_SHARED_ELEMENT_KEY) {
        if (uiState.isLoading) {
            ShimmerLoadingList()
        } else {
            val bottomPadding = LocalBottomPadding.current

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name field
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { onEvent(CreateEditSubunitUiEvent.UpdateName(it)) },
                    label = { Text(stringResource(R.string.subunit_field_name)) },
                    placeholder = { Text(stringResource(R.string.subunit_field_name_hint)) },
                    isError = uiState.nameError != null,
                    supportingText = uiState.nameError?.let { { Text(it.asString()) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Members section
                Text(
                    text = stringResource(R.string.subunit_field_members),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (uiState.membersError != null) {
                    Text(
                        text = uiState.membersError.asString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (uiState.sharesError != null) {
                    Text(
                        text = uiState.sharesError.asString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                MemberSelectionList(
                    members = uiState.availableMembers,
                    selectedMemberIds = uiState.selectedMemberIds,
                    memberShares = uiState.memberShares,
                    lockedMemberIds = uiState.lockedMemberIds,
                    onToggleMember = { onEvent(CreateEditSubunitUiEvent.ToggleMember(it)) },
                    onShareChanged = { userId, share ->
                        onEvent(CreateEditSubunitUiEvent.UpdateMemberShare(userId, share))
                    },
                    onShareLockToggled = { userId ->
                        onEvent(CreateEditSubunitUiEvent.ToggleShareLock(userId))
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Save button
                Button(
                    onClick = { onEvent(CreateEditSubunitUiEvent.Save) },
                    enabled = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.subunit_save))
                }

                // Account for floating bottom navigation bar
                Spacer(modifier = Modifier.height(bottomPadding))
            }
        }
    }
}

@Composable
private fun MemberSelectionList(
    members: ImmutableList<MemberUiModel>,
    selectedMemberIds: ImmutableList<String>,
    memberShares: Map<String, String>,
    lockedMemberIds: ImmutableList<String>,
    onToggleMember: (String) -> Unit,
    onShareChanged: (String, String) -> Unit,
    onShareLockToggled: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        members.forEach { member ->
            val isSelected = member.userId in selectedMemberIds
            val isEnabled = !member.isAssigned

            MemberSelectionRow(
                member = member,
                isSelected = isSelected,
                isEnabled = isEnabled,
                shareText = if (isSelected) memberShares[member.userId] ?: "" else "",
                isShareLocked = member.userId in lockedMemberIds,
                onToggle = { onToggleMember(member.userId) },
                onShareChanged = { onShareChanged(member.userId, it) },
                onShareLockToggled = { onShareLockToggled(member.userId) }
            )
        }
    }
}

@Composable
private fun MemberSelectionRow(
    member: MemberUiModel,
    isSelected: Boolean,
    isEnabled: Boolean,
    shareText: String,
    isShareLocked: Boolean,
    onToggle: () -> Unit,
    onShareChanged: (String) -> Unit,
    onShareLockToggled: () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { if (isEnabled) onToggle() },
                enabled = isEnabled
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isEnabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                if (member.isAssigned) {
                    Text(
                        text = stringResource(R.string.subunit_member_assigned_hint, member.assignedSubunitName),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Share input for selected members with lock toggle
        if (isSelected) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OutlinedTextField(
                    value = shareText,
                    onValueChange = onShareChanged,
                    label = { Text("%") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onShareLockToggled,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isShareLocked) Icons.Filled.Lock else Icons.Outlined.LockOpen,
                        contentDescription = stringResource(
                            if (isShareLocked) {
                                R.string.subunit_share_unlock
                            } else {
                                R.string.subunit_share_lock
                            }
                        ),
                        tint = if (isShareLocked) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
