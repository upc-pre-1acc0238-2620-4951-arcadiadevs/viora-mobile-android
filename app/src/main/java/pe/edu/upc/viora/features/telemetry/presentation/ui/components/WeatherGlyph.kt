package pe.edu.upc.viora.features.telemetry.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.features.telemetry.domain.valueobject.SkyCondition

private const val RAY_COUNT = 8
private const val DEGREES_PER_RAY = 360.0 / RAY_COUNT

/** A sun: a disc with eight rays. [filled] is the hero version; the outline one goes in the list. */
@Composable
internal fun SunGlyph(
    modifier: Modifier = Modifier,
    glyphSize: Dp = 48.dp,
    color: Color = Harvest300,
    filled: Boolean = true,
) {
    Canvas(modifier.size(glyphSize)) {
        val side = size.minDimension
        val radius = side * 0.24f
        val stroke = side * 0.07f
        if (filled) {
            drawCircle(color, radius, center)
        } else {
            drawCircle(color, radius, center, style = Stroke(stroke))
        }
        val inner = radius + side * 0.10f
        val outer = radius + side * 0.22f
        repeat(RAY_COUNT) { index ->
            val angle = Math.toRadians(index * DEGREES_PER_RAY)
            val dx = cos(angle).toFloat()
            val dy = sin(angle).toFloat()
            drawLine(
                color = color,
                start = Offset(center.x + dx * inner, center.y + dy * inner),
                end = Offset(center.x + dx * outer, center.y + dy * outer),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
        }
    }
}

/** The small outline icon of a forecast row: sun, sun behind a cloud, or cloud with a drop. */
@Composable
internal fun WeatherGlyph(
    sky: SkyCondition,
    modifier: Modifier = Modifier,
    glyphSize: Dp = 24.dp,
    tint: Color = Neutral900,
) {
    Box(modifier.size(glyphSize)) {
        when (sky) {
            SkyCondition.SUNNY -> SunGlyph(glyphSize = glyphSize, color = tint, filled = false)
            SkyCondition.PARTLY_CLOUDY -> {
                SunGlyph(
                    modifier = Modifier.align(Alignment.TopStart),
                    glyphSize = glyphSize * 0.62f,
                    color = tint,
                    filled = false,
                )
                Icon(
                    painter = painterResource(R.drawable.ic_cloud),
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.align(Alignment.BottomEnd).size(glyphSize * 0.74f),
                )
            }
            SkyCondition.RAINY -> {
                Icon(
                    painter = painterResource(R.drawable.ic_cloud),
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.align(Alignment.TopCenter).size(glyphSize * 0.82f),
                )
                Icon(
                    painter = painterResource(R.drawable.ic_water_drop),
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.align(Alignment.BottomCenter).size(glyphSize * 0.46f),
                )
            }
        }
    }
}
