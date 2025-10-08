package es.pedrazamiguez.expenseshareapp.ui.main.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.Routes
import es.pedrazamiguez.expenseshareapp.core.ui.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.ui.main.presentation.screen.MainScreen

fun NavGraphBuilder.mainGraph(
    navigationProviders: List<NavigationProvider>,
    screenUiProviders: List<ScreenUiProvider>
) {
    composable(Routes.MAIN) {
        MainScreen(
            navigationProviders = navigationProviders,
            screenUiProviders = screenUiProviders
        )
    }
}
