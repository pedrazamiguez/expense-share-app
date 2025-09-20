package es.pedrazamiguez.expenseshareapp.ui.main.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import es.pedrazamiguez.expenseshareapp.core.ui.navigation.NavigationProvider
import es.pedrazamiguez.expenseshareapp.ui.main.presentation.screen.MainScreen

const val MAIN_ROUTE = "main"

fun NavGraphBuilder.mainGraph(navigationProviders: List<NavigationProvider>) {
    composable(MAIN_ROUTE) {
        MainScreen(navigationProviders = navigationProviders)
    }
}
