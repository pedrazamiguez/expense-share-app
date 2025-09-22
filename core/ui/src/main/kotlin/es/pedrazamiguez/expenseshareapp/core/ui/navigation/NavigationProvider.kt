package es.pedrazamiguez.expenseshareapp.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder

interface NavigationProvider {
    val route: String
    val label: String

    @Composable
    fun Icon(isSelected: Boolean)

    fun buildGraph(builder: NavGraphBuilder)
}
