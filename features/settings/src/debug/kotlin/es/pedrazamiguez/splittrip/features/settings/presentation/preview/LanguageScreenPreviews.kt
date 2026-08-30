package es.pedrazamiguez.splittrip.features.settings.presentation.preview

import androidx.compose.runtime.Composable
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewComplete
import es.pedrazamiguez.splittrip.core.designsystem.preview.PreviewThemeWrapper
import es.pedrazamiguez.splittrip.domain.enums.AppLanguage
import es.pedrazamiguez.splittrip.features.settings.presentation.screen.LanguageScreen

@PreviewComplete
@Composable
private fun LanguageScreenPreview() {
    PreviewThemeWrapper {
        LanguageScreen(
            availableLanguages = AppLanguage.entries,
            selectedLanguageCode = AppLanguage.EN.code,
            onLanguageSelected = {}
        )
    }
}
