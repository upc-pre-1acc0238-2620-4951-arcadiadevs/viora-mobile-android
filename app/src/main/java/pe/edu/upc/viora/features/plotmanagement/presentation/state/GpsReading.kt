package pe.edu.upc.viora.features.plotmanagement.presentation.state

import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GpsFix

/** What the GPS is telling the producer while they walk the outline of a plot. */
sealed interface GpsReading {

    /** Waiting for the first fix. */
    data object Searching : GpsReading

    /** A usable position; its [GpsFix.signal] says whether a corner can be marked with it. */
    data class Located(val fix: GpsFix) : GpsReading

    /**
     * No GPS: the fixes stopped coming, or they are too imprecise to count as a position.
     * [lastFix] is the last position known, to show where the producer was.
     */
    data class Lost(val lastFix: GpsFix?) : GpsReading
}
