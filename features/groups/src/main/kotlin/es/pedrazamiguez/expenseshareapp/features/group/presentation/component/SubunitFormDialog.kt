package es.pedrazamiguez.expenseshareapp.features.group.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.common.presentation.UiText
import es.pedrazamiguez.expenseshareapp.core.designsystem.extension.asString
import es.pedrazamiguez.expenseshareapp.features.group.R
import es.pedrazamiguez.expenseshareapp.features.group.presentation.model.MemberUiModel
import es.pedrazamiguez.expenseshareapp.features.group.presentation.model.SubunitFormState
import kotlinx.collections.immutable.ImmutableList

@Composable
fun SubunitFormDialog(
    formState: SubunitFormState,
    nameError: UiText? = null,
    membersError: UiText? = null,
    onNameChanged: (String) -> Unit = {},
    onToggleMember: (String) -> Unit = {},
    onShareChanged: (String, String) -> Unit = { _, _ -> },
    onSave: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val title = if (formState.isEditing) {
        stringResource(R.string.subunit_edit)
    } else {
        stringResource(R.string.subunit_create)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            SubunitFormContent(
                formState = formState,
                nameError = nameError,
                membersError = membersError,
                onNameChanged = onNameChanged,
                onToggleMember = onToggleMember,
                onShareChanged = onShareChanged
            )
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text(stringResource(R.string.subunit_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}

@Composable
private fun SubunitFormContent(
    formState: SubunitFormState,
    nameError: UiText?,
    membersError: UiText?,
    onNameChanged: (String) -> Unit,
    onToggleMember: (String) -> Unit,
    onShareChanged: (String, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Name field
        OutlinedTextField(
            value = formState.name,
            onValueChange = onNameChanged,
            label = { Text(stringResource(R.string.subunit_field_name)) },
            placeholder = { Text(stringResource(R.string.subunit_field_name_hint)) },
            isError = nameError != null,
            supportingText = nameError?.let { { Text(it.asString()) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Members section
        Text(
            text = stringResource(R.string.subunit_field_members),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (membersError != null) {
            Text(
                text = membersError.asString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        MemberSelectionList(
            members = formState.availableMembers,
            selectedMemberIds = formState.selectedMemberIds,
            memberShares = formState.memberShares,
            onToggleMember = onToggleMember,
            onShareChanged = onShareChanged
        )
    }
}

@Composable
private fun MemberSelectionList(
    members: ImmutableList<MemberUiModel>,
    selectedMemberIds: ImmutableList<String>,
    memberShares: Map<String, String>,
    onToggleMember: (String) -> Unit,
    onShareChanged: (String, String) -> Unit
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
                onToggle = { onToggleMember(member.userId) },
                onShareChanged = { onShareChanged(member.userId, it) }
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
    onToggle: () -> Unit,
    onShareChanged: (String) -> Unit
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

        // Share input for selected members
        if (isSelected) {
            OutlinedTextField(
                value = shareText,
                onValueChange = onShareChanged,
                label = { Text("%") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 48.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}


