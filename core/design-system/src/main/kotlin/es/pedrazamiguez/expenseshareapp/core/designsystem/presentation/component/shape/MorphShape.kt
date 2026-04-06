package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.component.shape

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.toPath

/**
 * A custom [Shape] that renders a [Morph] at a given [progress] between two
 * `RoundedPolygon`s.
 *
 * Shared across composables that use Material 3 Expressive shape morphing
 * (e.g., `ExpressiveFab`).
 *
 * @param morph The [Morph] instance describing the shape transition.
 * @param progress A value in `[0f, 1f]` indicating how far the morph has progressed.
 */
internal class MorphShape(
    private val morph: Morph,
    private val progress: Float
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = morph.toPath(progress).asComposePath()

        val matrix = Matrix()
        matrix.scale(size.width / 2f, size.height / 2f)
        matrix.translate(1f, 1f)
        path.transform(matrix)

        return Outline.Generic(path)
    }
}
