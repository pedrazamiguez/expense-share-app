package es.pedrazamiguez.expenseshareapp.core.ui.screen

import androidx.compose.runtime.Composable

interface ScreenUiProvider {

    val route: String

    val topBar: (@Composable () -> Unit)?
        get() = null

    val fab: (@Composable () -> Unit)?
        get() = null

}
