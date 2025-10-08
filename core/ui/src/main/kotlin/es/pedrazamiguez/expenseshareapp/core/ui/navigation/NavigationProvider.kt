package es.pedrazamiguez.expenseshareapp.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder

interface NavigationProvider {

    val route: String

    @Composable
    fun Icon(isSelected: Boolean)

    val label: String

    val order: Int

    val requiresSelectedGroup: Boolean

    fun buildGraph(builder: NavGraphBuilder)

}
