package es.pedrazamiguez.expenseshareapp.features.settings.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.Routes
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.feature.DefaultCurrencyFeature
import es.pedrazamiguez.expenseshareapp.features.settings.presentation.feature.SettingsFeature

fun NavGraphBuilder.settingsGraph() {
    composable(Routes.SETTINGS) {
        SettingsFeature()
    }
    composable(Routes.SETTINGS_DEFAULT_CURRENCY) {
        DefaultCurrencyFeature()
    }
}
