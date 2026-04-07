package es.pedrazamiguez.splittrip.core.designsystem.presentation.component.shape

import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star

// Soft scalloped circle constants
private const val SCALLOP_VERTEX_COUNT = 8
private const val SCALLOP_INNER_RADIUS = 0.92f
private const val SCALLOP_CORNER_RADIUS = 0.3f
private const val SCALLOP_CORNER_SMOOTHING = 0.2f

/**
 * Shared shape factory for expressive, organic shapes used across the design system.
 *
 * All shapes use the `RoundedPolygon` / `RoundedPolygon.star()` API from
 * `androidx.graphics.shapes` and are designed to feel consistent with the app's
 * Material 3 Expressive aesthetic (see [ExpressiveFab][es.pedrazamiguez.splittrip.core.designsystem.presentation.component.scaffold.ExpressiveFab],
 * [BrandedLoadingScreen][es.pedrazamiguez.splittrip.core.designsystem.presentation.component.layout.BrandedLoadingScreen]).
 *
 * To convert any [RoundedPolygon] into a Compose [Shape], wrap it with
 * [RoundedPolygonShape].
 */
internal object ExpressiveShapes {

    /**
     * A circle with barely perceptible wavy edges — a very subtle bottle-cap shape.
     *
     * Uses 8 vertices per radius and a high inner radius (0.92) so the scallops
     * are just noticeable enough to feel distinct without being distracting.
     *
     * Used by:
     * - `SyncStatusIndicator` container
     * - Back button container in `DynamicTopAppBar`
     */
    fun softScallopedCircle(): RoundedPolygon = RoundedPolygon.star(
        numVerticesPerRadius = SCALLOP_VERTEX_COUNT,
        radius = 1f,
        innerRadius = SCALLOP_INNER_RADIUS,
        rounding = CornerRounding(SCALLOP_CORNER_RADIUS, SCALLOP_CORNER_SMOOTHING)
    )
}
