package es.pedrazamiguez.splittrip.core.designsystem.presentation.component.chart

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.delay

private const val ANIMATION_DELAY_MS = 300L

@Composable
fun DonutChart(
    data: ImmutableList<DonutChartData>,
    modifier: Modifier = Modifier,
    strokeWidthDp: Dp = 64.dp,
    centerContent: @Composable () -> Unit = {}
) {
    var isAnimated by remember { mutableStateOf(false) }
    LaunchedEffect(data) {
        delay(ANIMATION_DELAY_MS.milliseconds)
        isAnimated = true
    }

    val total = remember(data) { data.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(1f) }

    val bouncySpringSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = 100f
    )

    val animatedSweepAngles = data.mapIndexed { index, item ->
        val targetAngle = if (isAnimated) (item.value / total) * 360f else 0f
        animateFloatAsState(
            targetValue = targetAngle,
            animationSpec = bouncySpringSpec,
            label = "slice_angle_$index"
        )
    }

    Box(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokeWidth = strokeWidthDp.toPx()

            var currentStartAngle = -90f // Start at top

            data.forEachIndexed { index, item ->
                val sweepAngle = animatedSweepAngles[index].value
                if (sweepAngle > 0f) {
                    drawArc(
                        color = item.color,
                        startAngle = currentStartAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )
                    currentStartAngle += sweepAngle
                }
            }
        }

        centerContent()
    }
}
