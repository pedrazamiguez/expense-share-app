@file:Suppress("MatchingDeclarationName")

package es.pedrazamiguez.splittrip.core.designsystem.presentation.component.input

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import es.pedrazamiguez.splittrip.domain.service.calculator.ExpressionResult

data class ArithmeticKeyboardState(
    val isVisible: Boolean = false,
    val expressionBuffer: String = "",
    val evaluationResult: ExpressionResult? = null,
    val onCommit: () -> Unit = {},
    val onClear: () -> Unit = {},
    val onOperatorClick: (String) -> Unit = {}
)

val globalArithmeticState = mutableStateOf(ArithmeticKeyboardState())
val LocalArithmeticKeyboardState = compositionLocalOf { globalArithmeticState }
