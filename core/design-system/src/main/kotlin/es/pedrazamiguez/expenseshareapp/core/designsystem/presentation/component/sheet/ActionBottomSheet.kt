package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.sheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.R

/**
 * A reusable Material 3 Action Bottom Sheet for Edit/Delete actions.
 * Standardizes the long-press context menu UX pattern.
 *
 * @param onDismiss Called when the sheet should be dismissed.
 * @param onEdit Called when the Edit action is selected.
 * @param onDelete Called when the Delete action is selected.
 * @param modifier Modifier for the bottom sheet content.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionBottomSheet(
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = 48.dp) // Essential for gesture navigation spacing
        ) {
            ListItem(
                headlineContent = { Text(stringResource(id = R.string.action_edit)) },
                leadingContent = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                modifier = Modifier.clickable { onEdit() }
            )
            HorizontalDivider()
            ListItem(
                headlineContent = {
                    Text(
                        stringResource(id = R.string.action_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                },
                leadingContent = {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                modifier = Modifier.clickable { onDelete() }
            )
        }
    }
}
