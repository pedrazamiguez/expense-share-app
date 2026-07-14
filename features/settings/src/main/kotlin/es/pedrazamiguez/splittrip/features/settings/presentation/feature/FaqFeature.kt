package es.pedrazamiguez.splittrip.features.settings.presentation.feature

import androidx.compose.runtime.Composable
import es.pedrazamiguez.splittrip.core.designsystem.navigation.Routes
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.scaffold.FeatureScaffold
import es.pedrazamiguez.splittrip.features.settings.presentation.screen.FaqScreen

@Composable
fun FaqFeature() {
    FeatureScaffold(currentRoute = Routes.SETTINGS_FAQ) {
        FaqScreen()
    }
}
