package es.pedrazamiguez.expenseshareapp.features.group.presentation.component.step.subunit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.form.FormErrorBanner
import es.pedrazamiguez.expenseshareapp.features.group.R
import es.pedrazamiguez.expenseshareapp.features.group.presentation.model.MemberUiModel
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.event.CreateEditSubunitUiEvent
import es.pedrazamiguez.expenseshareapp.features.group.presentation.viewmodel.state.CreateEditSubunitUiState
import kotlinx.collections.immutable.ImmutableList

/**
 * Step 2: Select members from the group.
 */
@Composable
fun SubunitMembersStep(
    uiState: CreateEditSubunitUiState,
    onEvent: (CreateEditSubunitUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FormErrorBanner(error = uiState.membersError)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            shape = MaterialTheme.shapes.large
        ) {
            MemberCheckboxList(
                members = uiState.availableMembers,
                selectedMemberIds = uiState.selectedMemberIds,
                onToggleMember = { onEvent(CreateEditSubunitUiEvent.ToggleMember(it)) },
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun MemberCheckboxList(
    members: ImmutableList<MemberUiModel>,
    selectedMemberIds: ImmutableList<String>,
    onToggleMember: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        members.forEach { member ->
            MemberCheckboxRow(
                member = member,
                isSelected = member.userId in selectedMemberIds,
                onToggle = { onToggleMember(member.userId) }
            )
        }
    }
}

@Composable
private fun MemberCheckboxRow(
    member: MemberUiModel,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val isEnabled = !member.isAssigned

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
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
                    text = stringResource(
                        R.string.subunit_member_assigned_hint,
                        member.assignedSubunitName
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
