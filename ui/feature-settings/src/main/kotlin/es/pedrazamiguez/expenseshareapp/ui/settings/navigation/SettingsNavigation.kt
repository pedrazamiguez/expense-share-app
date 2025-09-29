package es.pedrazamiguez.expenseshareapp.ui.settings.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.Routes
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.feature.SettingsFeature

fun NavGraphBuilder.settingsGraph() {
    composable(Routes.SETTINGS) {
        SettingsFeature()
    }
}
