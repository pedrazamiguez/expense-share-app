package es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.SyncProblem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.R
import es.pedrazamiguez.splittrip.domain.enums.SyncStatus

/**
 * Compact sync-status indicator that shows an icon + optional text
 * when the entity has not yet been synced to the cloud.
 *
 * Hidden when the status is [SyncStatus.SYNCED].
 *
 * Uses [LocalContentColor] for PENDING_SYNC so the indicator automatically
 * adapts to whatever surface it is placed on (e.g., surfaceContainerLow,
 * primaryContainer for selected items, etc.).
 *
 * @param syncStatus Current synchronization status of the entity.
 * @param showLabel  When true, displays a short text label next to the icon.
 * @param modifier   Outer modifier applied to the [Row].
 */
@Composable
fun SyncStatusIndicator(
    syncStatus: SyncStatus,
    modifier: Modifier = Modifier,
    showLabel: Boolean = false
) {
    if (syncStatus == SyncStatus.SYNCED) return

    val isPending = syncStatus == SyncStatus.PENDING_SYNC
    val icon = if (isPending) Icons.Outlined.CloudOff else Icons.Outlined.SyncProblem
    val tint = if (isPending) {
        LocalContentColor.current.copy(alpha = 0.7f)
    } else {
        MaterialTheme.colorScheme.error
    }
    val contentDesc = if (isPending) {
        stringResource(R.string.sync_status_pending)
    } else {
        stringResource(R.string.sync_status_failed)
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDesc,
            modifier = Modifier.size(16.dp),
            tint = tint
        )
        if (showLabel) {
            Text(
                text = contentDesc,
                style = MaterialTheme.typography.labelSmall,
                color = tint
            )
        }
    }
}
