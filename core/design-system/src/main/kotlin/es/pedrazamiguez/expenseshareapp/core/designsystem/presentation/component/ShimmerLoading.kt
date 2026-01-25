package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewThemeWrapper
import es.pedrazamiguez.expenseshareapp.core.designsystem.preview.PreviewThemes

/**
 * Creates a shimmer brush effect for skeleton loading states.
 * Material 3 Expressive uses subtle, smooth shimmer effects.
 */
@Composable
fun shimmerBrush(
    targetValue: Float = 1000f,
    shimmerColors: List<Color> = listOf(
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f),
        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.4f),
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f),
    )
): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnimation - 200f, translateAnimation - 200f),
        end = Offset(translateAnimation, translateAnimation)
    )
}

/**
 * A shimmer placeholder box for skeleton loading.
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
    width: Dp? = null
) {
    val brush = shimmerBrush()
    Box(
        modifier = modifier
            .then(if (width != null) Modifier.width(width) else Modifier.fillMaxWidth())
            .height(height)
            .clip(MaterialTheme.shapes.small)
            .background(brush)
    )
}

/**
 * Shimmer skeleton card that mimics the ExpenseItem/GroupItem layout.
 */
@Composable
fun ShimmerItemCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerBox(
                    modifier = Modifier.weight(0.6f),
                    height = 20.dp
                )
                Spacer(modifier = Modifier.width(16.dp))
                ShimmerBox(
                    width = 80.dp,
                    height = 24.dp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ShimmerBox(
                    width = 100.dp,
                    height = 14.dp
                )
                ShimmerBox(
                    width = 60.dp,
                    height = 14.dp
                )
            }
        }
    }
}

/**
 * A list of shimmer skeleton items for loading states.
 */
@Composable
fun ShimmerLoadingList(
    modifier: Modifier = Modifier,
    itemCount: Int = 5
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(itemCount) {
            ShimmerItemCard()
        }
    }
}

@PreviewThemes
@Composable
private fun ShimmerLoadingListPreview() {
    PreviewThemeWrapper {
        ShimmerLoadingList()
    }
}

@PreviewThemes
@Composable
private fun ShimmerItemCardPreview() {
    PreviewThemeWrapper {
        ShimmerItemCard(
            modifier = Modifier.padding(16.dp)
        )
    }
}

