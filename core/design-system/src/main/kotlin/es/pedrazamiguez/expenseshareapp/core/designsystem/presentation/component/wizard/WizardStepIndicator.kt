package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.wizard

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

private const val STEP_CIRCLE_SIZE = 28
private const val CONNECTOR_HEIGHT = 3

// Vertical offset so connectors visually centre on the step circles:
// (circle_size - connector_height) / 2
private const val CONNECTOR_TOP_OFFSET = (STEP_CIRCLE_SIZE - CONNECTOR_HEIGHT) / 2

/**
 * Horizontal step indicator for a multi-step wizard.
 *
 * Accepts a plain [List] of already-localised [stepLabels] — one per step — so
 * this component is completely domain-agnostic and can be reused across any
 * feature that implements a step-by-step flow (e.g. AddExpense, AddCashWithdrawal).
 *
 * Completed steps show a ✓ checkmark, the current step is highlighted in primary
 * colour, and upcoming steps are dimmed.  Connector lines are vertically centred
 * with the step circles (not the full row height).
 *
 * @param stepLabels   Ordered list of localised step labels.
 * @param currentStepIndex Zero-based index of the currently active step.
 */
@Composable
fun WizardStepIndicator(
    stepLabels: List<String>,
    currentStepIndex: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        stepLabels.forEachIndexed { index, label ->
            val isCompleted = index < currentStepIndex
            val isCurrent = index == currentStepIndex

            WizardStepItem(
                stepNumber = index + 1,
                label = label,
                isCompleted = isCompleted,
                isCurrent = isCurrent
            )

            if (index < stepLabels.lastIndex) {
                WizardStepConnector(
                    isCompleted = isCompleted,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun WizardStepItem(
    stepNumber: Int,
    label: String,
    isCompleted: Boolean,
    isCurrent: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        StepCircle(
            stepNumber = stepNumber,
            isCompleted = isCompleted,
            isCurrent = isCurrent
        )
        Text(
            text = label,
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
}

@Composable
private fun WizardStepConnector(isCompleted: Boolean, modifier: Modifier = Modifier) {
    val connectorColor by animateColorAsState(
        targetValue = if (isCompleted) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outlineVariant
        },
        label = "wizardConnector"
    )
    Box(
        modifier = modifier
            .padding(top = CONNECTOR_TOP_OFFSET.dp, start = 4.dp, end = 4.dp)
            .height(CONNECTOR_HEIGHT.dp)
            .clip(CircleShape)
            .background(connectorColor)
    )
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
        targetValue = if (isCurrent || isCompleted) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
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
        Text(
            text = if (isCompleted) "✓" else stepNumber.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}
