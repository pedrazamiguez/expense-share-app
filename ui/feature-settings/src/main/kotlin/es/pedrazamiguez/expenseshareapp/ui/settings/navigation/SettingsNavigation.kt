package es.pedrazamiguez.expenseshareapp.ui.settings.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import es.pedrazamiguez.expenseshareapp.ui.settings.presentation.feature.SettingsFeature

const val SETTINGS_ROUTE = "settings"

fun NavGraphBuilder.settingsGraph() {
    composable(SETTINGS_ROUTE) {
        SettingsFeature()
    }
}
