package es.pedrazamiguez.expenseshareapp.core.designsystem.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraphBuilder

interface NavigationProvider {

    val route: String

    val order: Int

    val requiresSelectedGroup: Boolean

    @Composable
    fun Icon(isSelected: Boolean, tint: Color = Color.Unspecified)

    @Composable
    fun getLabel(): String

    fun buildGraph(builder: NavGraphBuilder)

}
