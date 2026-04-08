package es.pedrazamiguez.splittrip.core.designsystem.presentation.component.shape

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath

/**
 * A custom [Shape] that renders a static [RoundedPolygon].
 *
 * Unlike [MorphShape], this does not interpolate between two shapes —
 * it simply draws the polygon as-is, scaled to fill the composable's bounds.
 *
 * Shared across composables that need an organic, non-circular container
 * (e.g., `SyncStatusIndicator`, back button in `DynamicTopAppBar`).
 *
 * @param polygon The [RoundedPolygon] to render.
 */
internal class RoundedPolygonShape(
    private val polygon: RoundedPolygon
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = polygon.toPath().asComposePath()

        val matrix = Matrix()
        matrix.scale(size.width / 2f, size.height / 2f)
        matrix.translate(1f, 1f)
        path.transform(matrix)

        return Outline.Generic(path)
    }
}
