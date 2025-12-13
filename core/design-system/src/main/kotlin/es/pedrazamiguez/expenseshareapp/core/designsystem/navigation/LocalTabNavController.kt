package es.pedrazamiguez.expenseshareapp.core.designsystem.navigation

import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavHostController

val LocalTabNavController = compositionLocalOf<NavHostController> {
    error("No LocalTabNavController provided")
}
