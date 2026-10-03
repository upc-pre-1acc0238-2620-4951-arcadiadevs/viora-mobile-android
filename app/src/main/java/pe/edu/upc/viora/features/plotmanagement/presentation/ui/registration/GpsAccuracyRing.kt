package pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration

import kotlin.math.cos
import kotlin.math.pow

/** Metres covered by one density-independent pixel at zoom 0 on the equator (Mapbox uses 512 px tiles). */
private const val METERS_PER_DP_AT_ZOOM_ZERO = 78_271.517

/**
 * The radius, in density-independent pixels, that [accuracyMeters] has on a map at [zoom]
 * around [latitude]. It draws the circle the producer's real position lies in.
 */
fun accuracyRadiusDp(accuracyMeters: Double, latitude: Double, zoom: Double): Double {
    val metersPerDp = METERS_PER_DP_AT_ZOOM_ZERO * cos(Math.toRadians(latitude)) / 2.0.pow(zoom)
    return accuracyMeters / metersPerDp
}
