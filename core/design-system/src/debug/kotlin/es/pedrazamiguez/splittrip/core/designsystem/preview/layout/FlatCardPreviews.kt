package es.pedrazamiguez.splittrip.core.designsystem.preview.layout

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.FlatCard
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.SectionCard
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewThemeWrapper
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewThemes

@PreviewThemes
@Composable
private fun FlatCardDefaultPreview() {
    PreviewThemeWrapper {
        FlatCard(modifier = Modifier.padding(16.dp)) {
            Text(
                modifier = Modifier.padding(20.dp),
                text = "Tonal layering — no border needed",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@PreviewThemes
@Composable
private fun FlatCardGhostBorderPreview() {
    PreviewThemeWrapper {
        FlatCard(
            modifier = Modifier.padding(16.dp),
            ghostBorder = true
        ) {
            Text(
                modifier = Modifier.padding(20.dp),
                text = "Ghost border — opt-in for dark-mode edge cases",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@PreviewThemes
@Composable
private fun SectionCardPreview() {
    PreviewThemeWrapper {
        SectionCard(
            modifier = Modifier.padding(16.dp),
            title = "Section Title"
        ) {
            Text(
                text = "Section content — surfaceContainerLow on surface (off-white)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Second row of content",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
