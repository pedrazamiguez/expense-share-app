package es.pedrazamiguez.expenseshareapp.features.main.presentation.component.navbar

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A sliding indicator that animates between navigation items.
 * Uses a bouncy spring animation for a playful, expressive feel.
 */
@Composable
internal fun SlidingIndicator(
    selectedIndex: Int,
    itemCount: Int,
    itemWidth: Dp,
    containerWidth: Dp,
    modifier: Modifier = Modifier
) {
    // Calculate spacing between items based on SpaceEvenly arrangement
    val totalItemsWidth = itemWidth * itemCount
    val totalSpacing = containerWidth - totalItemsWidth
    val spacingPerGap = totalSpacing / (itemCount + 1)

    // Calculate the actual position of each item
    val itemOffset = spacingPerGap + (itemWidth + spacingPerGap) * selectedIndex

    val indicatorOffset by animateDpAsState(
        targetValue = itemOffset,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "indicatorOffset"
    )

    val indicatorHeight = NavBarDefaults.BarHeight - 16.dp

    Box(
        modifier = modifier
            .offset(x = indicatorOffset)
            .width(itemWidth)
            .height(indicatorHeight)
            .clip(RoundedCornerShape(NavBarDefaults.IndicatorCornerRadius))
            .background(MaterialTheme.colorScheme.primaryContainer)
    )
}

