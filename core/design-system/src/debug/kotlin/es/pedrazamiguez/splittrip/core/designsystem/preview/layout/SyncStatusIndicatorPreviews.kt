package es.pedrazamiguez.splittrip.core.designsystem.preview.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.FlatCard
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.SyncStatusBadge
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.SyncStatusIndicator
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewThemeWrapper
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewThemes
import es.pedrazamiguez.splittrip.domain.enums.SyncStatus

@PreviewThemes
@Composable
private fun SyncStatusIndicatorPendingPreview() {
    PreviewThemeWrapper {
        SyncStatusIndicator(syncStatus = SyncStatus.PENDING_SYNC, showLabel = true)
    }
}

@PreviewThemes
@Composable
private fun SyncStatusIndicatorFailedPreview() {
    PreviewThemeWrapper {
        SyncStatusIndicator(syncStatus = SyncStatus.SYNC_FAILED, showLabel = true)
    }
}

@PreviewThemes
@Composable
private fun SyncStatusIndicatorSyncedPreview() {
    PreviewThemeWrapper {
        SyncStatusIndicator(syncStatus = SyncStatus.SYNCED, showLabel = true)
    }
}

@PreviewThemes
@Composable
private fun SyncStatusBadgeOverlayPreview() {
    PreviewThemeWrapper {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(16.dp)) {
            Box {
                FlatCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Pending sync card",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                SyncStatusBadge(syncStatus = SyncStatus.PENDING_SYNC)
            }
            Box {
                FlatCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Failed sync card",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                SyncStatusBadge(syncStatus = SyncStatus.SYNC_FAILED)
            }
            Box {
                FlatCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Synced card (no badge)",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                SyncStatusBadge(syncStatus = SyncStatus.SYNCED)
            }
        }
    }
}
