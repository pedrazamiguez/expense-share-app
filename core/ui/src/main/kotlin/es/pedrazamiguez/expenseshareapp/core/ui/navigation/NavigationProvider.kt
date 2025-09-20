package es.pedrazamiguez.expenseshareapp.core.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraphBuilder

interface NavigationProvider {
    val route: String
    val label: String
    val icon: ImageVector
    fun buildGraph(builder: NavGraphBuilder)
}
