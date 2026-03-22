package es.pedrazamiguez.expenseshareapp.features.balance.presentation.screen.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.features.balance.presentation.viewmodel.state.CashWithdrawalStep

private const val STEP_CIRCLE_SIZE = 28
private const val CONNECTOR_HEIGHT = 3

/**
 * Horizontal step indicator for the cash withdrawal wizard.
 *
 * Shows circles for each step connected by lines. The current step is highlighted,
 * completed steps use a secondary color, and upcoming steps are dimmed.
 */
@Composable
fun WizardStepIndicator(
    steps: List<CashWithdrawalStep>,
    currentStepIndex: Int,
    stepLabels: Map<CashWithdrawalStep, String>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            val isCompleted = index < currentStepIndex
            val isCurrent = index == currentStepIndex

            // Step circle + label
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StepCircle(
                    stepNumber = index + 1,
                    isCompleted = isCompleted,
                    isCurrent = isCurrent
                )
                Text(
                    text = stepLabels[step] ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (isCurrent || isCompleted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Connector line between steps
            if (index < steps.lastIndex) {
                val connectorColor by animateColorAsState(
                    targetValue = if (isCompleted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    },
                    label = "connectorColor"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(CONNECTOR_HEIGHT.dp)
                        .padding(horizontal = 4.dp)
                        .clip(CircleShape)
                        .background(connectorColor)
                )
            }
        }
    }
}

@Composable
private fun StepCircle(
    stepNumber: Int,
    isCompleted: Boolean,
    isCurrent: Boolean
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isCurrent -> MaterialTheme.colorScheme.primary
            isCompleted -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            else -> MaterialTheme.colorScheme.outlineVariant
        },
        label = "stepBackground"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            isCurrent || isCompleted -> MaterialTheme.colorScheme.onPrimary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "stepContent"
    )

    Box(
        modifier = Modifier
            .size(STEP_CIRCLE_SIZE.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (isCompleted) {
            Text(
                text = "✓",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        } else {
            Text(
                text = stepNumber.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}
