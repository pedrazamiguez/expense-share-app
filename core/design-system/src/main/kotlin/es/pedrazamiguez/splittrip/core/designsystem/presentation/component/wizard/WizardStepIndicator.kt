package es.pedrazamiguez.splittrip.core.designsystem.presentation.component.wizard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

private const val STEP_CIRCLE_SIZE = 28
private const val CONNECTOR_HEIGHT = 3

// Vertical offset so connectors visually centre on the step circles:
// (circle_size - connector_height) / 2
private const val CONNECTOR_TOP_OFFSET = (STEP_CIRCLE_SIZE - CONNECTOR_HEIGHT) / 2

/** Maximum number of steps visible at once before scrolling kicks in. */
private const val MAX_VISIBLE_STEPS = 5

/** Fixed connector width used in the scrollable variant. */
private val SCROLLABLE_CONNECTOR_WIDTH = 16.dp

/** Horizontal padding applied to the step row. */
private val HORIZONTAL_PADDING = 20.dp

/**
 * Horizontal step indicator for a multi-step wizard.
 *
 * When the number of steps exceeds [MAX_VISIBLE_STEPS], the indicator becomes
 * horizontally scrollable and smoothly auto-centres the current step.
 * Otherwise a static, non-scrolling row is used with weight-based connectors.
 *
 * @param stepLabels       Ordered list of localised step labels.
 * @param currentStepIndex Zero-based index of the currently active step.
 */
@Composable
fun WizardStepIndicator(
    stepLabels: List<String>,
    currentStepIndex: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        tonalElevation = 3.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        AnimatedContent(
            targetState = stepLabels,
            transitionSpec = {
                // Slide right when a step is added, slide left when one is removed.
                val direction = if (targetState.size >= initialState.size) 1 else -1
                (
                    slideInHorizontally(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        initialOffsetX = { fullWidth -> direction * fullWidth / 3 }
                    ) + fadeIn(animationSpec = tween(durationMillis = 250))
                    )
                    .togetherWith(
                        slideOutHorizontally(
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            targetOffsetX = { fullWidth -> -direction * fullWidth / 3 }
                        ) + fadeOut(animationSpec = tween(durationMillis = 200))
                    )
                    .using(
                        SizeTransform(
                            clip = false,
                            sizeAnimationSpec = { _, _ ->
                                spring(stiffness = Spring.StiffnessMediumLow)
                            }
                        )
                    )
            },
            label = "wizardStepIndicator"
        ) { labels ->
            if (labels.size > MAX_VISIBLE_STEPS) {
                ScrollableStepIndicator(labels, currentStepIndex)
            } else {
                StaticStepIndicator(labels, currentStepIndex)
            }
        }
    }
}

// ── Static (≤ MAX_VISIBLE_STEPS) ─────────────────────────────────────────

@Composable
private fun StaticStepIndicator(
    stepLabels: List<String>,
    currentStepIndex: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = HORIZONTAL_PADDING, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        stepLabels.forEachIndexed { index, label ->
            WizardStepItem(
                stepNumber = index + 1,
                label = label,
                isCompleted = index < currentStepIndex,
                isCurrent = index == currentStepIndex
            )
            if (index < stepLabels.lastIndex) {
                WizardStepConnector(
                    isCompleted = index < currentStepIndex,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ── Scrollable (> MAX_VISIBLE_STEPS) ─────────────────────────────────────

@Composable
private fun ScrollableStepIndicator(
    stepLabels: List<String>,
    currentStepIndex: Int
) {
    val density = LocalDensity.current
    val scrollState = rememberScrollState()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        val usableWidth = maxWidth - HORIZONTAL_PADDING * 2
        val totalConnectorSpace = SCROLLABLE_CONNECTOR_WIDTH * (MAX_VISIBLE_STEPS - 1)
        val stepItemWidth = (usableWidth - totalConnectorSpace) / MAX_VISIBLE_STEPS
        val unitWidth = stepItemWidth + SCROLLABLE_CONNECTOR_WIDTH

        // Smoothly centre the current step in the visible window
        LaunchedEffect(currentStepIndex) {
            val unitPx = with(density) { unitWidth.toPx() }
            val itemPx = with(density) { stepItemWidth.toPx() }
            val viewportPx = with(density) { maxWidth.toPx() }

            val stepCenterPx =
                with(density) { HORIZONTAL_PADDING.toPx() } +
                    currentStepIndex * unitPx + itemPx / 2
            val targetScroll = (stepCenterPx - viewportPx / 2)
                .coerceIn(0f, scrollState.maxValue.toFloat())
            scrollState.animateScrollTo(targetScroll.toInt())
        }

        Row(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .padding(horizontal = HORIZONTAL_PADDING),
            verticalAlignment = Alignment.Top
        ) {
            stepLabels.forEachIndexed { index, label ->
                WizardStepItem(
                    stepNumber = index + 1,
                    label = label,
                    isCompleted = index < currentStepIndex,
                    isCurrent = index == currentStepIndex,
                    modifier = Modifier.width(stepItemWidth)
                )
                if (index < stepLabels.lastIndex) {
                    WizardStepConnector(
                        isCompleted = index < currentStepIndex,
                        modifier = Modifier.width(SCROLLABLE_CONNECTOR_WIDTH)
                    )
                }
            }
        }
    }
}

// ── Shared sub-components ────────────────────────────────────────────────

@Composable
private fun WizardStepItem(
    stepNumber: Int,
    label: String,
    isCompleted: Boolean,
    isCurrent: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun WizardStepConnector(
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
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
