package es.pedrazamiguez.splittrip.features.settings.presentation.feature

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import es.pedrazamiguez.splittrip.core.designsystem.navigation.Routes
import es.pedrazamiguez.splittrip.core.designsystem.presentation.component.scaffold.FeatureScaffold
import es.pedrazamiguez.splittrip.features.settings.presentation.screen.OpenSourceScreen

@Composable
fun OpenSourceFeature() {
    val context = LocalContext.current
    FeatureScaffold(currentRoute = Routes.SETTINGS_OPEN_SOURCE) {
        OpenSourceScreen(onLibraryUrlClick = { url ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        })
    }
}
