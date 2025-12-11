package es.pedrazamiguez.expenseshareapp.core.ui.navigation

import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavHostController

val LocalRootNavController = compositionLocalOf<NavHostController> {
    error("No LocalRootNavController provided")
}
