package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import es.pedrazamiguez.expenseshareapp.core.designsystem.transition.LocalAnimatedVisibilityScope
import es.pedrazamiguez.expenseshareapp.core.designsystem.transition.LocalSharedTransitionScope

/**
 * Creates a soft, organic blob-like shape perfect for expressive FABs.
 */
private fun createBlobShape(): RoundedPolygon {
    return RoundedPolygon.star(
        numVerticesPerRadius = 9,
        radius = 1f,
        innerRadius = 0.9f,
        rounding = CornerRounding(0.5f, 0.5f)
    )
}

/**
 * Creates a rounded flower/clover-like shape for pressed state.
 */
private fun createFlowerShape(): RoundedPolygon {
    return RoundedPolygon.star(
        numVerticesPerRadius = 5,
        radius = 1f,
        innerRadius = 0.75f,
        rounding = CornerRounding(0.4f, 0.4f)
    )
}

/**
 * A custom Shape that morphs between two RoundedPolygons.
 */
private class MorphShape(
    private val morph: Morph, private val progress: Float
) : androidx.compose.ui.graphics.Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: androidx.compose.ui.unit.Density
    ): androidx.compose.ui.graphics.Outline {
        val path = morph.toPath(progress).asComposePath()

        val matrix = Matrix()
        matrix.scale(size.width / 2f, size.height / 2f)
        matrix.translate(1f, 1f)
        path.transform(matrix)

        return androidx.compose.ui.graphics.Outline.Generic(path)
    }
}

/**
 * An expressive Floating Action Button with organic Material 3 shapes.
 *
 * Features:
 * - Organic blob-like shape (not a boring rectangle!)
 * - Morphs to a flower/clover shape on press
 * - Scale animation for tactile feedback
 * - Soft shadow for depth
 * - Optional shared element transition support
 *
 * @param onClick Callback when the FAB is clicked
 * @param icon The icon to display
 * @param contentDescription Accessibility description for the icon
 * @param modifier Modifier for the FAB
 * @param containerColor Background color of the FAB
 * @param contentColor Color of the icon
 * @param sharedTransitionKey Optional key for shared element transitions. When provided,
 *                            the FAB will participate in a container transform animation.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ExpressiveFab(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    sharedTransitionKey: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Get shared transition scope if available
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    // Create the two shapes for morphing
    val blobShape = remember { createBlobShape() }
    val flowerShape = remember { createFlowerShape() }
    val morph = remember { Morph(blobShape, flowerShape) }

    // Animate morph progress
    val morphProgress = remember { Animatable(0f) }

    LaunchedEffect(isPressed) {
        morphProgress.animateTo(
            targetValue = if (isPressed) 1f else 0f, animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium
            )
        )
    }

    // Scale animation
    val scale = remember { Animatable(1f) }

    LaunchedEffect(isPressed) {
        scale.animateTo(
            targetValue = if (isPressed) 0.9f else 1f, animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium
            )
        )
    }

    val fabShape = remember(morphProgress.value) {
        MorphShape(morph, morphProgress.value)
    }

    // Build the shared element modifier if transition is available
    val sharedModifier =
        if (sharedTransitionKey != null && sharedTransitionScope != null && animatedVisibilityScope != null) {
            with(sharedTransitionScope) {
                Modifier.sharedBounds(
                    sharedContentState = rememberSharedContentState(key = sharedTransitionKey),
                    animatedVisibilityScope = animatedVisibilityScope,
                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                )
            }
        } else {
            Modifier
        }

    Box(
        modifier = modifier
            .then(sharedModifier)
            .size(64.dp)
            .scale(scale.value)
            .shadow(
                elevation = 8.dp,
                shape = fabShape,
                ambientColor = containerColor.copy(alpha = 0.3f),
                spotColor = containerColor.copy(alpha = 0.3f)
            )
            .clip(fabShape)
            .background(containerColor)
            .clickable(
                interactionSource = interactionSource, indication = null, onClick = onClick
            ), contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * A large expressive FAB for primary actions.
 * Uses larger dimensions and the same organic shape.
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

    val blobShape = remember { createBlobShape() }
    val flowerShape = remember { createFlowerShape() }
    val morph = remember { Morph(blobShape, flowerShape) }

    val morphProgress = remember { Animatable(0f) }

    LaunchedEffect(isPressed) {
        morphProgress.animateTo(
            targetValue = if (isPressed) 1f else 0f, animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium
            )
        )
    }

    val scale = remember { Animatable(1f) }

    LaunchedEffect(isPressed) {
        scale.animateTo(
            targetValue = if (isPressed) 0.88f else 1f, animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium
            )
        )
    }

    val fabShape = remember(morphProgress.value) {
        MorphShape(morph, morphProgress.value)
    }

    Box(
        modifier = modifier
            .size(80.dp)
            .scale(scale.value)
            .shadow(
                elevation = 12.dp,
                shape = fabShape,
                ambientColor = containerColor.copy(alpha = 0.4f),
                spotColor = containerColor.copy(alpha = 0.4f)
            )
            .clip(fabShape)
            .background(containerColor)
            .clickable(
                interactionSource = interactionSource, indication = null, onClick = onClick
            ), contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentColor,
            modifier = Modifier.size(32.dp)
        )
    }
}
