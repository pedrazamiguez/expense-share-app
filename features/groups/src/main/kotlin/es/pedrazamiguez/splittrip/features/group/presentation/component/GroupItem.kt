package es.pedrazamiguez.splittrip.features.group.presentation.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.R as DesignR
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.FlatCard
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.SyncStatusBadge
import es.pedrazamiguez.splittrip.features.group.presentation.model.GroupUiModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GroupItem(
    groupUiModel: GroupUiModel,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
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
                    onClick = {
                        onClick(groupUiModel.id, groupUiModel.name, groupUiModel.currency)
                    },
                    onLongClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongClick()
                    }
                ),
            color = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GroupItemNameRow(groupUiModel = groupUiModel, isSelected = isSelected)

                if (groupUiModel.description.isNotEmpty()) {
                    Text(
                        text = groupUiModel.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                GroupItemMetaRow(groupUiModel = groupUiModel, isSelected = isSelected)
            }
        }
        SyncStatusBadge(syncStatus = groupUiModel.syncStatus)
    }
}

@Composable
private fun GroupItemNameRow(groupUiModel: GroupUiModel, isSelected: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = groupUiModel.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Surface(
            shape = MaterialTheme.shapes.large,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.primaryContainer
            }
        ) {
            Text(
                text = groupUiModel.currency,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                },
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun GroupItemMetaRow(groupUiModel: GroupUiModel, isSelected: Boolean) {
    val metaColor = resolveMetaColor(isSelected)
    val metaParts = buildList {
        if (groupUiModel.dateText.isNotEmpty()) add(groupUiModel.dateText)
        if (groupUiModel.membersCountText.isNotEmpty()) add(groupUiModel.membersCountText)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        metaParts.forEachIndexed { index, part ->
            if (index > 0) {
                Text(
                    text = stringResource(DesignR.string.metadata_separator),
                    style = MaterialTheme.typography.bodyMedium,
                    color = metaColor
                )
            }
            Text(
                text = part,
                style = MaterialTheme.typography.bodyMedium,
                color = metaColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun resolveMetaColor(isSelected: Boolean) =
    if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
