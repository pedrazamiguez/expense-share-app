package es.pedrazamiguez.expenseshareapp.features.main.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.Routes
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.features.main.presentation.screen.MainScreen

fun NavGraphBuilder.mainGraph(
    navigationProviders: List<NavigationProvider>,
    screenUiProviders: List<ScreenUiProvider>,
    visibleProviders: List<NavigationProvider>
) {
    composable(Routes.MAIN) {
        MainScreen(
            navigationProviders = navigationProviders,
            screenUiProviders = screenUiProviders,
            visibleProviders = visibleProviders
        )
    }
}
