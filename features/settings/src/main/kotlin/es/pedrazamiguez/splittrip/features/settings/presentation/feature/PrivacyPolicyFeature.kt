package es.pedrazamiguez.splittrip.features.settings.presentation.feature

import androidx.compose.runtime.Composable
import es.pedrazamiguez.splittrip.core.designsystem.navigation.Routes
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.scaffold.FeatureScaffold
import es.pedrazamiguez.splittrip.features.settings.presentation.screen.PrivacyPolicyScreen

@Composable
fun PrivacyPolicyFeature() {
    FeatureScaffold(currentRoute = Routes.SETTINGS_PRIVACY_POLICY) {
        PrivacyPolicyScreen()
    }
}
