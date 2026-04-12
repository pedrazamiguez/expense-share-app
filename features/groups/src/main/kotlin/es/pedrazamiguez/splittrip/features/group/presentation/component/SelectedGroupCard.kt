package es.pedrazamiguez.splittrip.features.group.presentation.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Calendar
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.FlatCard
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.SyncStatusBadge
import es.pedrazamiguez.splittrip.features.group.R
import es.pedrazamiguez.splittrip.features.group.presentation.model.GroupUiModel
import kotlinx.collections.immutable.ImmutableList

private val COVER_IMAGE_HEIGHT = 160.dp
private val AVATAR_SIZE = 36.dp
private val AVATAR_OVERLAP_OFFSET = 20.dp
private val CURRENCY_HORIZONTAL_PADDING = 14.dp
private val CURRENCY_VERTICAL_PADDING = 8.dp
private val COVER_CORNER_RADIUS = 12.dp

/**
 * Hero card for the currently selected/active group.
 *
 * Renders a large cover image (or gradient placeholder), an "Active Now" badge,
 * member avatar stack with overflow count, and richer metadata compared to the
 * compact [GroupItem] used for unselected groups.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SelectedGroupCard(
    groupUiModel: GroupUiModel,
    modifier: Modifier = Modifier,
    onClick: (groupId: String, groupName: String, currency: String) -> Unit = { _, _, _ -> },
    onLongClick: () -> Unit = {}
) {
    val haptics = LocalHapticFeedback.current

    Box(modifier = modifier) {
        FlatCard(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .combinedClickable(
                    onClick = { onClick(groupUiModel.id, groupUiModel.name, groupUiModel.currency) },
                    onLongClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongClick()
                    }
                ),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                SelectedGroupCoverImage(
                    imageUrl = groupUiModel.imageUrl,
                    groupName = groupUiModel.name
                )
                SelectedGroupCardContent(groupUiModel = groupUiModel)
            }
        }
        SyncStatusBadge(syncStatus = groupUiModel.syncStatus)
    }
}

@Composable
private fun SelectedGroupCoverImage(imageUrl: String?, groupName: String) {
    val coverShape = RoundedCornerShape(topStart = COVER_CORNER_RADIUS, topEnd = COVER_CORNER_RADIUS)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(COVER_IMAGE_HEIGHT)
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(
                    R.string.group_cover_image_description,
                    groupName
                ),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(COVER_IMAGE_HEIGHT)
                    .clip(coverShape)
            )
        } else {
            GroupCoverGradientPlaceholder(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(COVER_IMAGE_HEIGHT)
                    .clip(coverShape)
            )
        }
        ActiveNowBadge(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )
    }
}

@Composable
private fun GroupCoverGradientPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(
            brush = Brush.linearGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.secondary
                ),
                start = Offset.Zero,
                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
            )
        )
    )
}

@Composable
private fun ActiveNowBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = stringResource(R.string.group_active_badge),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Suppress("LongMethod")
@Composable
private fun SelectedGroupCardContent(groupUiModel: GroupUiModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Name row with currency chip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = groupUiModel.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = groupUiModel.currency,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(
                        horizontal = CURRENCY_HORIZONTAL_PADDING,
                        vertical = CURRENCY_VERTICAL_PADDING
                    )
                )
            }
        }

        // Date row
        if (groupUiModel.dateText.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = TablerIcons.Outline.Calendar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = groupUiModel.dateText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }

        // Avatar stack + member count
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (groupUiModel.memberAvatarUrls.isNotEmpty() || groupUiModel.memberOverflowCount > 0) {
                MemberAvatarStack(
                    avatarUrls = groupUiModel.memberAvatarUrls,
                    overflowCount = groupUiModel.memberOverflowCount
                )
            }
            if (groupUiModel.membersCountText.isNotEmpty()) {
                Text(
                    text = groupUiModel.membersCountText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Renders a horizontal stack of overlapping member avatar circles with an optional overflow badge.
 *
 * Each circle shows a profile image loaded via Coil when a URL is available.
 * An overflow circle "+N" is appended when [overflowCount] > 0.
 */
@Composable
internal fun MemberAvatarStack(
    avatarUrls: ImmutableList<String>,
    overflowCount: Int,
    modifier: Modifier = Modifier
) {
    val totalCircles = avatarUrls.size + if (overflowCount > 0) 1 else 0
    val totalWidth = AVATAR_SIZE + AVATAR_OVERLAP_OFFSET * (totalCircles - 1).coerceAtLeast(0)

    Box(
        modifier = modifier
            .width(totalWidth)
            .height(AVATAR_SIZE)
    ) {
        avatarUrls.forEachIndexed { index, url ->
            SingleMemberAvatar(
                avatarUrl = url,
                modifier = Modifier
                    .size(AVATAR_SIZE)
                    .offset(x = AVATAR_OVERLAP_OFFSET * index)
                    .clip(CircleShape)
            )
        }
        if (overflowCount > 0) {
            val overflowIndex = avatarUrls.size
            Surface(
                modifier = Modifier
                    .size(AVATAR_SIZE)
                    .offset(x = AVATAR_OVERLAP_OFFSET * overflowIndex),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.group_member_avatar_overflow, overflowCount),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SingleMemberAvatar(avatarUrl: String, modifier: Modifier = Modifier) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(avatarUrl)
            .crossfade(true)
            .build(),
        contentDescription = stringResource(R.string.group_member_avatar_description),
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}
