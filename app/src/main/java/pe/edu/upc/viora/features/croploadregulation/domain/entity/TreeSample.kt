package pe.edu.upc.viora.features.croploadregulation.domain.entity

import java.time.LocalDate

/**
 * An individual tree sample recorded in the field.
 * In the UI, the user enters trunk circumference in cm (e.g. 92 cm).
 * When converting to backend DTO, diameter in mm is calculated as D = (C * 10) / π.
 */
data class TreeSample(
    val treeIdentifier: String,
    val shootsCount: Int,
    val fruitSetCount: Int,
    val trunkCircumferenceCm: Double? = null,
    val trunkDiameterMm: Double? = null,
    val observedOn: LocalDate = LocalDate.now(),
    val isSynced: Boolean = false,
) {
    val fruitsPerShoot: Double
        get() = if (shootsCount > 0) fruitSetCount.toDouble() / shootsCount else 0.0
}
