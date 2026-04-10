package es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.R
import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.CloudOff
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.RefreshAlert
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.shape.ExpressiveShapes
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.shape.RoundedPolygonShape
import es.pedrazamiguez.splittrip.domain.enums.SyncStatus

// Animation constants
private const val SCALE_IN_INITIAL = 0.8f
private const val SCALE_OUT_TARGET = 0.8f

// Container constants
private val CONTAINER_PADDING = 4.dp
private val ICON_SIZE = 14.dp
private val BADGE_OFFSET = 4.dp
private const val PENDING_CONTAINER_ALPHA = 0.6f

/**
 * Compact sync-status indicator that shows an icon + optional text
 * inside a subtle expressive container when the entity has not yet
 * been synced to the cloud.
 *
 * The indicator animates in (fade + scale) when sync status changes
 * to [SyncStatus.PENDING_SYNC] or [SyncStatus.SYNC_FAILED], and
 * animates out when status becomes [SyncStatus.SYNCED].
 *
 * **Call-sites should always compose this** (no external `if` guard)
 * and let the built-in [AnimatedVisibility] handle show/hide.
 *
 * @param syncStatus Current synchronization status of the entity.
 * @param showLabel  When true, displays a short text label next to the icon.
 * @param modifier   Outer modifier applied to the animated wrapper.
 */
@Composable
fun SyncStatusIndicator(
    syncStatus: SyncStatus,
    modifier: Modifier = Modifier,
    showLabel: Boolean = false
) {
    AnimatedVisibility(
        visible = syncStatus != SyncStatus.SYNCED,
        modifier = modifier,
        enter = fadeIn() + scaleIn(initialScale = SCALE_IN_INITIAL),
        exit = fadeOut() + scaleOut(targetScale = SCALE_OUT_TARGET)
    ) {
        SyncStatusContent(syncStatus = syncStatus, showLabel = showLabel)
    }
}

/**
 * Positions a [SyncStatusIndicator] as a floating overlay badge at the
 * bottom-end corner of the parent `Box`.
 *
 * The badge is offset slightly outside the card bounds (like a notification
 * badge on an app icon) to minimise overlap with in-card content. The parent
 * `Box` does not clip by default, so the badge remains fully visible even
 * when it extends beyond the card edges.
 *
 * **Usage:** Wrap the card content and this badge in a `Box`:
 *
 * ```kotlin
 * Box {
 *     FlatCard { /* item content */ }
 *     SyncStatusBadge(syncStatus = model.syncStatus)
 * }
 * ```
 *
 * @param syncStatus Current synchronization status of the entity.
 * @param modifier   Modifier applied to the badge wrapper.
 */
@Composable
fun BoxScope.SyncStatusBadge(
    syncStatus: SyncStatus,
    modifier: Modifier = Modifier
) {
    SyncStatusIndicator(
        syncStatus = syncStatus,
        modifier = modifier
            .align(Alignment.BottomEnd)
            .offset(x = BADGE_OFFSET, y = BADGE_OFFSET)
    )
}

@Composable
private fun SyncStatusContent(syncStatus: SyncStatus, showLabel: Boolean) {
    val isPending = syncStatus == SyncStatus.PENDING_SYNC

    val icon = if (isPending) TablerIcons.Outline.CloudOff else TablerIcons.Outline.RefreshAlert
    val containerColor = if (isPending) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = PENDING_CONTAINER_ALPHA)
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    val contentColor = if (isPending) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.error
    }
    val contentDesc = if (isPending) {
        stringResource(R.string.sync_status_pending)
    } else {
        stringResource(R.string.sync_status_failed)
    }

    val shape = remember { RoundedPolygonShape(ExpressiveShapes.softScallopedCircle()) }

    Row(
        modifier = Modifier
            .clip(shape)
            .background(containerColor)
            .padding(CONTAINER_PADDING),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDesc,
            modifier = Modifier.size(ICON_SIZE),
            tint = contentColor
        )
        if (showLabel) {
            Text(
                text = contentDesc,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor
            )
        }
    }
}
