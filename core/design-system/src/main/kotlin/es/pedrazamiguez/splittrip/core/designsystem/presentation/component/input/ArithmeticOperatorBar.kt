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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.domain.service.calculator.ExpressionResult

@Composable
fun ArithmeticOperatorBar(
    state: ArithmeticKeyboardState,
    modifier: Modifier = Modifier
) {
    if (!state.isVisible) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val resultText = when (val res = state.evaluationResult) {
            is ExpressionResult.Success -> {
                if (state.expressionBuffer.any { it in listOf('+', '−', '×', '÷') }) {
                    "= ${res.value.stripTrailingZeros().toPlainString()}"
                } else {
                    ""
                }
            }
            else -> ""
        }

        Text(
            text = resultText,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )

        if (state.expressionBuffer.isNotEmpty()) {
            OperatorButton(
                text = "✕",
                onClick = state.onClear
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(start = 8.dp)) {
            OperatorButton(text = "÷", onClick = { state.onOperatorClick("÷") })
            OperatorButton(text = "×", onClick = { state.onOperatorClick("×") })
            OperatorButton(text = "−", onClick = { state.onOperatorClick("−") })
            OperatorButton(text = "+", onClick = { state.onOperatorClick("+") })
        }
    }
}

@Composable
private fun OperatorButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
