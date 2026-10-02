package pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration

import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint

/** Where to point a map camera so a whole outline is in view. */
data class MapFraming(val center: GeoPoint, val zoom: Double)

private const val METERS_PER_DEGREE_LATITUDE = 110_574.0
private const val METERS_PER_DEGREE_LONGITUDE_AT_EQUATOR = 111_320.0

/** Metres covered by one density-independent pixel at zoom 0 on the equator (Mapbox uses 512 px tiles). */
private const val METERS_PER_DP_AT_ZOOM_ZERO = 78_271.517
private const val MIN_ZOOM = 1.0
private const val MAX_ZOOM = 19.0

/**
 * Centre and zoom that fit [corners] inside a map of [widthDp] x [heightDp], leaving
 * [paddingDp] free on every side. Computed by hand instead of asking the map, so it works
 * the same before the map has loaded. `null` when there are no corners.
 */
fun frameOutline(corners: List<GeoPoint>, widthDp: Float, heightDp: Float, paddingDp: Float): MapFraming? {
    if (corners.isEmpty()) return null
    val minLat = corners.minOf { it.latitude }
    val maxLat = corners.maxOf { it.latitude }
    val minLon = corners.minOf { it.longitude }
    val maxLon = corners.maxOf { it.longitude }
    val centerLat = (minLat + maxLat) / 2
    val center = GeoPoint(latitude = centerLat, longitude = (minLon + maxLon) / 2)

    val cosLat = cos(Math.toRadians(centerLat))
    val widthMeters = (maxLon - minLon) * METERS_PER_DEGREE_LONGITUDE_AT_EQUATOR * cosLat
    val heightMeters = (maxLat - minLat) * METERS_PER_DEGREE_LATITUDE
    val usableWidth = max(widthDp - 2 * paddingDp, 1f)
    val usableHeight = max(heightDp - 2 * paddingDp, 1f)

    // The tighter axis decides the zoom; a point (zero extent) gets the closest zoom.
    val metersPerDp = max(widthMeters / usableWidth, heightMeters / usableHeight)
    val zoom = if (metersPerDp <= 0.0) MAX_ZOOM else ln(METERS_PER_DP_AT_ZOOM_ZERO * cosLat / metersPerDp) / ln(2.0)
    return MapFraming(center = center, zoom = min(max(zoom, MIN_ZOOM), MAX_ZOOM))
}
