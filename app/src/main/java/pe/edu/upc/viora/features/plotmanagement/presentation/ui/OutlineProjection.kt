package pe.edu.upc.viora.features.plotmanagement.presentation.ui

import kotlin.math.cos
import kotlin.math.max
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint

/** A point in the unit square, `y` growing downwards like a canvas. */
data class UnitPoint(val x: Float, val y: Float)

/**
 * Projects a plot outline into the unit square so it can be drawn at any size: longitude is
 * scaled by cos(latitude) so shapes are not stretched, the aspect ratio is preserved and the
 * shape is centred. North ends up at the top. Fewer than 3 points yields an empty list.
 */
fun normalizeOutline(outline: List<GeoPoint>): List<UnitPoint> {
    if (outline.size < 3) return emptyList()
    val meanLatitudeRad = Math.toRadians(outline.map { it.latitude }.average())
    val xs = outline.map { it.longitude * cos(meanLatitudeRad) }
    val ys = outline.map { it.latitude }
    val minX = xs.min()
    val minY = ys.min()
    val width = xs.max() - minX
    val height = ys.max() - minY
    val extent = max(width, height)
    if (extent == 0.0) return emptyList()
    val offsetX = (extent - width) / 2
    val offsetY = (extent - height) / 2
    return outline.indices.map { i ->
        UnitPoint(
            x = ((xs[i] - minX + offsetX) / extent).toFloat().coerceIn(0f, 1f),
            // Latitude grows northwards but canvas y grows downwards.
            y = (1.0 - (ys[i] - minY + offsetY) / extent).toFloat().coerceIn(0f, 1f),
        )
    }
}
