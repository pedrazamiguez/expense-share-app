// ChartIcons
// Auto-generated from Tabler Icons (https://tabler.io/icons)
// Do not edit manually
@file:Suppress("MagicNumber", "LongMethod", "MaxLineLength")

package es.pedrazamiguez.splittrip.core.designsystem.icon.outline

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp
import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons

val TablerIcons.Outline.ChartDonut: ImageVector
    get() = _ChartDonut ?: ImageVector.Builder(
        name = "Outline.ChartDonut",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).addPath(
        pathData = addPathNodes(
            "M10 3.2a9 9 0 1 0 10.8 10.8a1 1 0 0 0 -1 -1h-3.8a4.1 4.1 0 1 1 -5 -5v-4a.9 .9 0 0 0 -1 -.8"
        ),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ).addPath(
        pathData = addPathNodes("M15 3.5a9 9 0 0 1 5.5 5.5h-4.5a9 9 0 0 0 -1 -1v-4.5"),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ).build().also { _ChartDonut = it }

private var _ChartDonut: ImageVector? = null
