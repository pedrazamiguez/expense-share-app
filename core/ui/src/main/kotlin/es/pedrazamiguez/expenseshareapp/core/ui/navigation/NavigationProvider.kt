package es.pedrazamiguez.expenseshareapp.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder

interface NavigationProvider {

    val route: String

    val order: Int

    val requiresSelectedGroup: Boolean

    @Composable
    fun Icon(isSelected: Boolean)

    @Composable
    fun getLabel(): String

    fun buildGraph(builder: NavGraphBuilder)

}
