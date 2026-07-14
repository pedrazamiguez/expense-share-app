package es.pedrazamiguez.splittrip.core.designsystem.presentation.component.input

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.foundation.spacing
import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.ArrowBigLeftLines
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.MathDivide
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.MathMinus
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.MathMultiply
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.MathPlus
import es.pedrazamiguez.splittrip.core.designsystem.presentation.formatter.formatArithmeticPreview

@Suppress("LongMethod")
@Composable
fun ArithmeticOperatorBar(
    state: ArithmeticKeyboardState,
    modifier: Modifier = Modifier
) {
    if (!state.isVisible) return

    val context = LocalContext.current
    val locale = remember(context) { context.resources.configuration.locales[0] }

    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 3.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = MaterialTheme.spacing.Default,
                vertical = MaterialTheme.spacing.Small
            ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val resultText = formatArithmeticPreview(
                expressionBuffer = state.expressionBuffer,
                evaluationResult = state.evaluationResult,
                maxDecimalPlaces = state.maxDecimalPlaces,
                minDecimalPlaces = state.minDecimalPlaces,
                locale = locale
            )

            Text(
                text = resultText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.Small),
                modifier = Modifier.padding(start = MaterialTheme.spacing.Small)
            ) {
                val containerColor = MaterialTheme.colorScheme.surface
                val contentColor = MaterialTheme.colorScheme.onSurface
                OperatorButton(icon = TablerIcons.Outline.MathPlus, onClick = {
                    state.onOperatorClick("+")
                }, containerColor = containerColor, contentColor = contentColor)
                OperatorButton(icon = TablerIcons.Outline.MathMinus, onClick = {
                    state.onOperatorClick("−")
                }, containerColor = containerColor, contentColor = contentColor)
                OperatorButton(icon = TablerIcons.Outline.MathMultiply, onClick = {
                    state.onOperatorClick("×")
                }, containerColor = containerColor, contentColor = contentColor)
                OperatorButton(icon = TablerIcons.Outline.MathDivide, onClick = {
                    state.onOperatorClick("÷")
                }, containerColor = containerColor, contentColor = contentColor)

                if (state.expressionBuffer.isNotEmpty()) {
                    OperatorButton(
                        icon = TablerIcons.Outline.ArrowBigLeftLines,
                        onClick = state.onClear,
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                }
            }
        }
    }
}

@Composable
private fun OperatorButton(
    icon: ImageVector,
    onClick: () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(containerColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )
    }
}
