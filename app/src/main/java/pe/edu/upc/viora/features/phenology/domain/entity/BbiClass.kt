package pe.edu.upc.viora.features.phenology.domain.entity

/**
 * Severity of the alternation, using the Figma thresholds (the backend's are different on purpose):
 * LOW below 0.20, MODERATE from 0.20 to 0.40 inclusive, SEVERE above 0.40.
 */
enum class BbiClass {
    LOW,
    MODERATE,
    SEVERE,
    ;

    companion object {
        private const val LOW_BELOW = 0.20
        private const val MODERATE_UP_TO = 0.40

        fun of(index: Double): BbiClass = when {
            index < LOW_BELOW -> LOW
            index <= MODERATE_UP_TO -> MODERATE
            else -> SEVERE
        }
    }
}
