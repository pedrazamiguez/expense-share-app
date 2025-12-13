package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.screen

import androidx.compose.runtime.Composable

interface ScreenUiProvider {

    val route: String

    val topBar: (@Composable () -> Unit)?
        get() = null

    val fab: (@Composable () -> Unit)?
        get() = null

}
