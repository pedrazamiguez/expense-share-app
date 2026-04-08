package es.pedrazamiguez.splittrip.core.designsystem.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@PreviewThemes
@Composable
private fun HorizonPalettePreview() {
    PreviewThemeWrapper {
        val scheme = MaterialTheme.colorScheme
        Column(modifier = Modifier.fillMaxWidth()) {
            ColorSwatch("primary", scheme.primary, scheme.onPrimary)
            ColorSwatch("primaryContainer", scheme.primaryContainer, scheme.onPrimaryContainer)
            ColorSwatch("secondary", scheme.secondary, scheme.onSecondary)
            ColorSwatch("secondaryContainer", scheme.secondaryContainer, scheme.onSecondaryContainer)
            ColorSwatch("tertiary", scheme.tertiary, scheme.onTertiary)
            ColorSwatch("tertiaryContainer", scheme.tertiaryContainer, scheme.onTertiaryContainer)
            ColorSwatch("error", scheme.error, scheme.onError)
            ColorSwatch("errorContainer", scheme.errorContainer, scheme.onErrorContainer)
        }
    }
}

@PreviewThemes
@Composable
private fun HorizonSurfaceHierarchyPreview() {
    PreviewThemeWrapper {
        val scheme = MaterialTheme.colorScheme
        Column(modifier = Modifier.fillMaxWidth()) {
            ColorSwatch("surface / background", scheme.surface, scheme.onSurface)
            ColorSwatch("surfaceContainerLowest", scheme.surfaceContainerLowest, scheme.onSurface)
            ColorSwatch("surfaceContainerLow", scheme.surfaceContainerLow, scheme.onSurface)
            ColorSwatch("surfaceContainer", scheme.surfaceContainer, scheme.onSurface)
            ColorSwatch("surfaceContainerHigh", scheme.surfaceContainerHigh, scheme.onSurface)
            ColorSwatch("surfaceContainerHighest", scheme.surfaceContainerHighest, scheme.onSurface)
            ColorSwatch("outline", scheme.outline, scheme.surface)
            ColorSwatch("outlineVariant", scheme.outlineVariant, scheme.onSurface)
        }
    }
}

@Composable
private fun ColorSwatch(label: String, background: Color, contentColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(background)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = label,
            color = contentColor,
            style = MaterialTheme.typography.labelMedium
        )
    }
}
