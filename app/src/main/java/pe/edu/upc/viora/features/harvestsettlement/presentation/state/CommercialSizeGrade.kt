package pe.edu.upc.viora.features.harvestsettlement.presentation.state

/**
 * A commercial size grade of table olives in fruits per kilogram, e.g. "101/110". The scale copies
 * the backend `CommercialSizeScale` (IOC trade standard COI/OT/NC no. 1, section 2.5): 60/70 up
 * to 381/410. Counts above 410 are not offered; the producer can type them instead.
 */
data class CommercialSizeGrade(val lower: Int, val upper: Int) {

    /** "101/110", the label the backend returns as `commercialSizeGrade`. */
    val label: String get() = "$lower/$upper"

    /** The representative count sent as `commercialFruitsPerKg` when this grade is chosen. */
    val midpoint: Double get() = (lower + upper) / 2.0

    companion object {
        private const val SCALE_START = 60
        private val UPPER_BOUNDS = listOf(
            70, 80, 90, 100, 110, 120,
            140, 160, 180, 200,
            230, 260, 290, 320, 350, 380, 410,
        )

        val SCALE: List<CommercialSizeGrade> = buildList {
            var lower = SCALE_START
            for (upper in UPPER_BOUNDS) {
                add(CommercialSizeGrade(lower, upper))
                lower = upper + 1
            }
        }
    }
}
