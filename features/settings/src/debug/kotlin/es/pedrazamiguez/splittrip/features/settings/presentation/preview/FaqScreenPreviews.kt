package es.pedrazamiguez.splittrip.features.settings.presentation.preview

import androidx.compose.runtime.Composable
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewThemeWrapper
import es.pedrazamiguez.splittrip.features.settings.presentation.screen.FaqScreen

@PreviewComplete
@Composable
private fun FaqScreenPreview() {
    PreviewThemeWrapper {
        FaqScreen()
    }
}
