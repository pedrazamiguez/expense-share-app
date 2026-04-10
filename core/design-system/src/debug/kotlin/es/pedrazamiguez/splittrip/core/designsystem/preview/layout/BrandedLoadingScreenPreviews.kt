package es.pedrazamiguez.splittrip.core.designsystem.preview.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Cash
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.BrandedLoadingScreen
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewThemeWrapper
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewThemes

@PreviewThemes
@Composable
private fun BrandedLoadingScreenPreview() {
    PreviewThemeWrapper {
        BrandedLoadingScreen(
            painter = rememberVectorPainter(TablerIcons.Outline.Cash)
        )
    }
}
