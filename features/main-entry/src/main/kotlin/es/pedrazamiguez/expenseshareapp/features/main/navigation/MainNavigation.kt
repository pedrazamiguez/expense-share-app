package es.pedrazamiguez.expenseshareapp.features.main.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.Routes
import es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.screen.ScreenUiProvider
import es.pedrazamiguez.expenseshareapp.features.main.presentation.screen.MainScreen

fun NavGraphBuilder.mainGraph(
    navigationProviders: List<NavigationProvider>,
    screenUiProviders: List<ScreenUiProvider>
) {
    composable(
        route = Routes.MAIN,
        deepLinks = listOf(
            navDeepLink { uriPattern = "expenseshareapp://groups/{groupId}" },
            navDeepLink { uriPattern = "expenseshareapp://groups/{groupId}/expenses" },
            navDeepLink { uriPattern = "expenseshareapp://groups/{groupId}/expenses/{expenseId}" }
        )
    ) {
        MainScreen(
            navigationProviders = navigationProviders,
            screenUiProviders = screenUiProviders
        )
    }
}
