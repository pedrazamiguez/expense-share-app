package es.pedrazamiguez.expenseshareapp.features.main.presentation.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.expenseshareapp.core.designsystem.navigation.NavigationProvider

private val NAV_ITEM_WIDTH = 72.dp
private val NAV_BAR_HEIGHT = 64.dp

@Composable
fun BottomNavigationBar(
    selectedRoute: String,
    onTabSelected: (String) -> Unit,
    items: List<NavigationProvider>
) {
    val selectedIndex = items.indexOfFirst { it.route == selectedRoute }.coerceAtLeast(0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 2.dp,
            shadowElevation = 12.dp
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(NAV_BAR_HEIGHT)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                val containerWidth = maxWidth

                // Sliding indicator
                SlidingIndicator(
                    selectedIndex = selectedIndex,
                    itemCount = items.size,
                    itemWidth = NAV_ITEM_WIDTH,
                    containerWidth = containerWidth
                )

                // Navigation items
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEachIndexed { index, item ->
                        FloatingNavItem(
                            item = item,
                            isSelected = index == selectedIndex,
                            onClick = { onTabSelected(item.route) },
                            modifier = Modifier.width(NAV_ITEM_WIDTH)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SlidingIndicator(
    selectedIndex: Int,
    itemCount: Int,
    itemWidth: Dp,
    containerWidth: Dp
) {
    // Calculate spacing between items based on SpaceEvenly arrangement
    val totalItemsWidth = itemWidth * itemCount
    val totalSpacing = containerWidth - totalItemsWidth
    val spacingPerGap = totalSpacing / (itemCount + 1)

    // Calculate the actual position of each item center
    val itemOffset = spacingPerGap + (itemWidth + spacingPerGap) * selectedIndex

    val indicatorOffset by animateDpAsState(
        targetValue = itemOffset,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "indicatorOffset"
    )

    val indicatorWidth = itemWidth - 4.dp
    val indicatorHeight = NAV_BAR_HEIGHT - 16.dp

    Box(
        modifier = Modifier
            .offset(x = indicatorOffset + 2.dp)
            .width(indicatorWidth)
            .height(indicatorHeight)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.primaryContainer)
    )
}

@Composable
private fun FloatingNavItem(
    item: NavigationProvider,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Bouncy scale animation
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scaleAnimation"
    )

    // Icon vertical bounce
    val iconOffsetY by animateFloatAsState(
        targetValue = if (isSelected) -2f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "iconBounce"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "contentColorAnimation"
    )

    // Label alpha animation
    val labelAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.7f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "labelAlpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .scale(scale)
                    .graphicsLayer {
                        translationY = iconOffsetY
                    },
                contentAlignment = Alignment.Center
            ) {
                item.Icon(isSelected = isSelected, tint = contentColor)
            }

            Text(
                text = item.getLabel(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = contentColor,
                modifier = Modifier.graphicsLayer { alpha = labelAlpha }
            )
        }
    }
}
