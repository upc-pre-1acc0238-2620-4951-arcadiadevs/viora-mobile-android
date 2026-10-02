package pe.edu.upc.viora.features.plotmanagement.domain.valueobject

import kotlin.math.roundToInt

/**
 * Planting frame: distance between rows and between trees, in metres. From it the density
 * (trees per hectare) is derived with the same formula as the backend: 10 000 / (row x tree).
 */
data class PlantationFrame(val rowSpacingMeters: Double, val treeSpacingMeters: Double) {

    enum class Error {
        /** A spacing is zero, negative or not a number. */
        NOT_POSITIVE,

        /** The resulting density is above what traditional olive groves admit. */
        TOO_DENSE,
    }

    init {
        require(check(rowSpacingMeters, treeSpacingMeters) == null) {
            "Invalid planting frame: $rowSpacingMeters x $treeSpacingMeters"
        }
    }

    val treesPerHectare: Int
        get() = density(rowSpacingMeters, treeSpacingMeters)

    companion object {
        /** Upper bound the design applies on the client (the backend only requires >= 50). */
        const val MAX_TREES_PER_HECTARE = 500

        fun density(rowSpacingMeters: Double, treeSpacingMeters: Double): Int =
            (10_000.0 / (rowSpacingMeters * treeSpacingMeters)).roundToInt()

        /** Why these spacings cannot form a frame, or `null` when they are valid. */
        fun check(rowSpacingMeters: Double, treeSpacingMeters: Double): Error? = when {
            !(rowSpacingMeters > 0.0) || !(treeSpacingMeters > 0.0) -> Error.NOT_POSITIVE
            density(rowSpacingMeters, treeSpacingMeters) > MAX_TREES_PER_HECTARE -> Error.TOO_DENSE
            else -> null
        }
    }
}
