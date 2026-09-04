package es.pedrazamiguez.splittrip.features.settings.presentation.preview

import androidx.compose.runtime.Composable
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewThemeWrapper
import es.pedrazamiguez.splittrip.domain.enums.AppTheme
import es.pedrazamiguez.splittrip.features.settings.presentation.screen.ThemeScreen

@PreviewComplete
@Composable
private fun ThemeScreenPreview() {
    PreviewThemeWrapper {
        ThemeScreen(
            availableThemes = AppTheme.entries,
            selectedThemeCode = AppTheme.SYSTEM.code,
            onThemeSelected = {}
        )
    }
}
