package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.scaffold

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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import es.pedrazamiguez.expenseshareapp.core.designsystem.transition.fabSharedTransitionModifier

// Shape constants
private const val BLOB_CORNER_RADIUS = 0.5f
private const val BLOB_CORNER_SMOOTHING = 0.5f
private const val FLOWER_CORNER_RADIUS = 0.4f
private const val FLOWER_CORNER_SMOOTHING = 0.4f

// Default FAB dimensions & animation values
private val FAB_SIZE = 64.dp
private val FAB_ICON_SIZE = 24.dp
private const val FAB_PRESSED_SCALE = 0.9f
private val FAB_ELEVATION = 8.dp
private const val FAB_SHADOW_ALPHA = 0.3f

// Large FAB dimensions & animation values
private val LARGE_FAB_SIZE = 80.dp
private val LARGE_FAB_ICON_SIZE = 32.dp
private const val LARGE_FAB_PRESSED_SCALE = 0.88f
private val LARGE_FAB_ELEVATION = 12.dp
private const val LARGE_FAB_SHADOW_ALPHA = 0.4f

/**
 * Creates a soft, organic blob-like shape perfect for expressive FABs.
 */
private fun createBlobShape(): RoundedPolygon = RoundedPolygon.star(
    numVerticesPerRadius = 7,
    radius = 1f,
    innerRadius = 0.9f,
    rounding = CornerRounding(BLOB_CORNER_RADIUS, BLOB_CORNER_SMOOTHING)
)

/**
 * Creates a rounded flower/clover-like shape for pressed state.
 */
private fun createFlowerShape(): RoundedPolygon = RoundedPolygon.star(
    numVerticesPerRadius = 5,
    radius = 1f,
    innerRadius = 0.75f,
    rounding = CornerRounding(FLOWER_CORNER_RADIUS, FLOWER_CORNER_SMOOTHING)
)

/**
 * A custom Shape that morphs between two RoundedPolygons.
 */
private class MorphShape(private val morph: Morph, private val progress: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = morph.toPath(progress).asComposePath()

        val matrix = Matrix()
        matrix.scale(size.width / 2f, size.height / 2f)
        matrix.translate(1f, 1f)
        path.transform(matrix)

        return Outline.Generic(path)
    }
}

/**
 * Bundles the size and animation constants that differentiate [ExpressiveFab]
 * from [LargeExpressiveFab], keeping [ExpressiveFabBase]'s parameter count low.
 */
private data class FabStyle(
    val size: Dp,
    val iconSize: Dp,
    val pressedScale: Float,
    val elevation: Dp,
    val shadowAlpha: Float
)

/**
 * Bundles container and content colours so that [ExpressiveFabBase] stays within
 * the recommended parameter-count limit.
 */
private data class FabColors(
    val containerColor: Color,
    val contentColor: Color
)

private val DefaultFabStyle = FabStyle(
    size = FAB_SIZE,
    iconSize = FAB_ICON_SIZE,
    pressedScale = FAB_PRESSED_SCALE,
    elevation = FAB_ELEVATION,
    shadowAlpha = FAB_SHADOW_ALPHA
)

private val LargeFabStyle = FabStyle(
    size = LARGE_FAB_SIZE,
    iconSize = LARGE_FAB_ICON_SIZE,
    pressedScale = LARGE_FAB_PRESSED_SCALE,
    elevation = LARGE_FAB_ELEVATION,
    shadowAlpha = LARGE_FAB_SHADOW_ALPHA
)

/**
 * Internal base composable that handles all morph/scale/click logic shared by
 * [ExpressiveFab] and [LargeExpressiveFab]. Call-sites only need to supply the
 * [FabStyle] that differentiates the two variants.
 */
@Composable
private fun ExpressiveFabBase(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    style: FabStyle,
    colors: FabColors,
    modifier: Modifier = Modifier,
    sharedTransitionKey: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val morph = remember { Morph(createBlobShape(), createFlowerShape()) }

    val morphProgress = remember { Animatable(0f) }
    LaunchedEffect(isPressed) {
        morphProgress.animateTo(
            targetValue = if (isPressed) 1f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    val scale = remember { Animatable(1f) }
    LaunchedEffect(isPressed) {
        scale.animateTo(
            targetValue = if (isPressed) style.pressedScale else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    val fabShape = remember(morphProgress.value) { MorphShape(morph, morphProgress.value) }

    val sharedModifier = if (sharedTransitionKey != null) {
        fabSharedTransitionModifier(sharedTransitionKey)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .then(sharedModifier)
            .size(style.size)
            .scale(scale.value)
            .shadow(
                elevation = style.elevation,
                shape = fabShape,
                ambientColor = colors.containerColor.copy(alpha = style.shadowAlpha),
                spotColor = colors.containerColor.copy(alpha = style.shadowAlpha)
            )
            .clip(fabShape)
            .background(colors.containerColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = colors.contentColor,
            modifier = Modifier.size(style.iconSize)
        )
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
@Composable
fun ExpressiveFab(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.tertiary,
    contentColor: Color = MaterialTheme.colorScheme.onTertiary,
    sharedTransitionKey: String? = null
) {
    ExpressiveFabBase(
        onClick = onClick,
        icon = icon,
        contentDescription = contentDescription,
        style = DefaultFabStyle,
        colors = FabColors(containerColor = containerColor, contentColor = contentColor),
        modifier = modifier,
        sharedTransitionKey = sharedTransitionKey
    )
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
    containerColor: Color = MaterialTheme.colorScheme.tertiary,
    contentColor: Color = MaterialTheme.colorScheme.onTertiary
) {
    ExpressiveFabBase(
        onClick = onClick,
        icon = icon,
        contentDescription = contentDescription,
        style = LargeFabStyle,
        colors = FabColors(containerColor = containerColor, contentColor = contentColor),
        modifier = modifier
    )
}
