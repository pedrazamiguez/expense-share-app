package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * An expressive Floating Action Button with Material 3 styling and animations.
 *
 * Features:
 * - Morphing corner radius on press (rounded square → more circular)
 * - Scale animation on press for tactile feedback
 * - Subtle rotation animation on press
 * - Customizable colors and elevation
 *
 * @param onClick Callback when the FAB is clicked
 * @param icon The icon to display
 * @param contentDescription Accessibility description for the icon
 * @param modifier Modifier for the FAB
 * @param containerColor Background color of the FAB
 * @param contentColor Color of the icon
 */
@Composable
fun ExpressiveFab(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Animate corner radius - becomes more circular when pressed
    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 22.dp else 16.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cornerRadius"
    )

    // Scale down slightly when pressed for tactile feedback
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    // Subtle rotation on press
    val rotation by animateFloatAsState(
        targetValue = if (isPressed) -8f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "rotation"
    )

    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .scale(scale)
            .rotate(rotation),
        shape = RoundedCornerShape(cornerRadius),
        containerColor = containerColor,
        contentColor = contentColor,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp,
            hoveredElevation = 8.dp
        ),
        interactionSource = interactionSource
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * A large expressive FAB for primary actions.
 * Uses larger dimensions and more dramatic animations.
 */
@Composable
fun LargeExpressiveFab(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 32.dp else 24.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cornerRadius"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    val rotation by animateFloatAsState(
        targetValue = if (isPressed) -12f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "rotation"
    )

    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .size(72.dp)
            .scale(scale)
            .rotate(rotation),
        shape = RoundedCornerShape(cornerRadius),
        containerColor = containerColor,
        contentColor = contentColor,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 8.dp,
            pressedElevation = 2.dp,
            hoveredElevation = 12.dp
        ),
        interactionSource = interactionSource
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(32.dp)
        )
    }
}

