package pe.edu.upc.viora.features.plotmanagement.domain.valueobject

import kotlin.math.abs
import kotlin.math.cos

/**
 * The corners of a plot in drawing order, without repeating the first one at the end.
 * It knows how to validate itself and to estimate its area, so the form can guide the producer
 * before anything is sent to the server (which stays the authority on the final area).
 */
data class PlotOutline(val corners: List<GeoPoint>) {

    sealed interface Error {
        /** A plot needs at least [MIN_CORNERS] corners; the producer has [have]. */
        data class NotEnoughCorners(val have: Int) : Error

        /** Two edges that are not neighbours cross each other. */
        data object SelfIntersecting : Error
    }

    init {
        require(check(corners) == null) { "Invalid plot outline" }
    }

    /** Approximate area in hectares. */
    val areaHectares: Double
        get() = areaHectares(corners)

    companion object {
        const val MIN_CORNERS = 3
        private const val EARTH_RADIUS_METERS = 6_371_008.8
        private const val SQUARE_METERS_PER_HECTARE = 10_000.0

        /** Why [corners] cannot be a plot outline, or `null` when they can. */
        fun check(corners: List<GeoPoint>): Error? = when {
            corners.size < MIN_CORNERS -> Error.NotEnoughCorners(corners.size)
            hasCrossingEdges(corners) -> Error.SelfIntersecting
            else -> null
        }

        /** Area of the polygon in hectares; 0 when there are fewer than 3 corners. */
        fun areaHectares(corners: List<GeoPoint>): Double {
            if (corners.size < MIN_CORNERS) return 0.0
            val xy = project(corners)
            var twiceArea = 0.0
            for (i in xy.indices) {
                val (x1, y1) = xy[i]
                val (x2, y2) = xy[(i + 1) % xy.size]
                twiceArea += x1 * y2 - x2 * y1
            }
            return abs(twiceArea) / 2.0 / SQUARE_METERS_PER_HECTARE
        }

        /** Flat metres around the average corner: precise enough at plot scale (hundreds of metres). */
        private fun project(corners: List<GeoPoint>): List<Pair<Double, Double>> {
            val meanLatitude = corners.map { it.latitude }.average()
            val meanLongitude = corners.map { it.longitude }.average()
            val cosLatitude = cos(Math.toRadians(meanLatitude))
            return corners.map {
                Pair(
                    Math.toRadians(it.longitude - meanLongitude) * EARTH_RADIUS_METERS * cosLatitude,
                    Math.toRadians(it.latitude - meanLatitude) * EARTH_RADIUS_METERS,
                )
            }
        }

        private fun hasCrossingEdges(corners: List<GeoPoint>): Boolean {
            val p = project(corners)
            val n = p.size
            for (i in 0 until n) {
                for (j in i + 1 until n) {
                    // Edges that share a corner are neighbours and always touch: skip them.
                    val neighbours = j == i + 1 || (i == 0 && j == n - 1)
                    if (neighbours) continue
                    if (segmentsIntersect(p[i], p[(i + 1) % n], p[j], p[(j + 1) % n])) return true
                }
            }
            return false
        }

        private fun segmentsIntersect(
            a: Pair<Double, Double>,
            b: Pair<Double, Double>,
            c: Pair<Double, Double>,
            d: Pair<Double, Double>,
        ): Boolean {
            val d1 = orientation(c, d, a)
            val d2 = orientation(c, d, b)
            val d3 = orientation(a, b, c)
            val d4 = orientation(a, b, d)
            return d1 * d2 < 0 && d3 * d4 < 0
        }

        /** Cross product sign: > 0 left turn, < 0 right turn, 0 collinear. */
        private fun orientation(a: Pair<Double, Double>, b: Pair<Double, Double>, c: Pair<Double, Double>): Double =
            (b.first - a.first) * (c.second - a.second) - (b.second - a.second) * (c.first - a.first)
    }
}
