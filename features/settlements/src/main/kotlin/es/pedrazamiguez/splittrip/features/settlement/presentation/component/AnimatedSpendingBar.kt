package es.pedrazamiguez.splittrip.features.settlement.presentation.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.features.settlement.presentation.model.MemberSpendingBarUiModel

val MemberSpendingColors = listOf(
    Color(0xFF3A7BD5), // Horizon Blue tint
    Color(0xFFE05555), // Warm Red
    Color(0xFFE8A838), // Amber
    Color(0xFF3DAA70), // Teal Green
    Color(0xFF9B59B6), // Violet
    Color(0xFFE67E22), // Orange
    Color(0xFF1ABC9C), // Emerald
    Color(0xFF2C3E50) // Dark Slate
)

@Composable
internal fun AnimatedSpendingBar(
    bar: MemberSpendingBarUiModel,
    globalMaxCents: Long,
    modifier: Modifier = Modifier
) {
    var isAnimated by remember { mutableStateOf(false) }
    LaunchedEffect(bar.userId) {
        isAnimated = true
    }

    val progress by animateFloatAsState(
        targetValue = if (isAnimated) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "spending_bar_progress"
    )

    val surfaceHighest = MaterialTheme.colorScheme.surfaceContainerHighest

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(RoundedCornerShape(6.dp))
    ) {
        val totalWidth = size.width
        val barHeight = size.height

        drawRect(
            color = surfaceHighest,
            size = Size(totalWidth, barHeight)
        )

        if (globalMaxCents <= 0 || progress <= 0f) return@Canvas

        var currentX = 0f

        val ownFraction = (bar.ownSpendingCents.toFloat() / globalMaxCents.toFloat()) * progress
        val ownWidth = ownFraction * totalWidth

        if (ownWidth > 0f) {
            val ownColor = MemberSpendingColors[bar.memberColorIndex % MemberSpendingColors.size]
            drawRect(
                color = ownColor,
                topLeft = Offset(currentX, 0f),
                size = Size(ownWidth, barHeight)
            )
            currentX += ownWidth
        }

        bar.spilloverSegments.forEach { segment ->
            val segmentFraction = (segment.amountCents.toFloat() / globalMaxCents.toFloat()) * progress
            val segmentWidth = segmentFraction * totalWidth
            if (segmentWidth > 0f) {
                val color = MemberSpendingColors[segment.ownerColorIndex % MemberSpendingColors.size]
                drawRect(
                    color = color,
                    topLeft = Offset(currentX, 0f),
                    size = Size(segmentWidth, barHeight)
                )
                currentX += segmentWidth
            }
        }
    }
}
