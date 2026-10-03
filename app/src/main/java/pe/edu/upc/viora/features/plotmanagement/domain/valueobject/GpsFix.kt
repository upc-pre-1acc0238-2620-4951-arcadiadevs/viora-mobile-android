package pe.edu.upc.viora.features.plotmanagement.domain.valueobject

/**
 * A position reported by the phone's GPS. [accuracyMeters] is the radius of the circle the real
 * position lies in (68 % of the time): the smaller, the better.
 */
data class GpsFix(val point: GeoPoint, val accuracyMeters: Double) {
    init {
        require(accuracyMeters >= 0.0) { "Accuracy must not be negative: $accuracyMeters" }
    }

    val signal: GpsSignal get() = GpsSignal.of(accuracyMeters)
}

/**
 * How trustworthy a GPS fix is for marking the corner of a plot. A corner is only accepted
 * when the signal [allowsMarking]: beyond [MARKING_MAX_METERS] the outline would be too imprecise
 * to be worth the producer's time.
 */
enum class GpsSignal {
    /** Up to [GOOD_MAX_METERS]: as good as a phone gets. */
    GOOD,

    /** Up to [MARKING_MAX_METERS]: usable, but the area will be less exact. */
    FAIR,

    /** Up to [LOST_ABOVE_METERS]: too imprecise to mark a corner; it usually recovers by itself. */
    WEAK,

    /** Beyond that (or no fix for a while): treated as no GPS at all. */
    LOST,
    ;

    val allowsMarking: Boolean get() = this == GOOD || this == FAIR

    companion object {
        const val GOOD_MAX_METERS = 5.0
        const val MARKING_MAX_METERS = 15.0
        const val LOST_ABOVE_METERS = 30.0

        fun of(accuracyMeters: Double): GpsSignal = when {
            accuracyMeters <= GOOD_MAX_METERS -> GOOD
            accuracyMeters <= MARKING_MAX_METERS -> FAIR
            accuracyMeters <= LOST_ABOVE_METERS -> WEAK
            else -> LOST
        }
    }
}
