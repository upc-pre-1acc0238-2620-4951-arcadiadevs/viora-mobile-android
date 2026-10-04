package pe.edu.upc.viora.features.phenology.domain.entity

import java.time.Instant

/**
 * The biennial bearing index computed by the server for a plot. [value] is null when the server
 * could not compute it (not enough campaigns); [evaluatedYears] is how many campaigns it used.
 */
data class BearingIndex(
    val value: Double?,
    val evaluatedYears: Int,
    val evaluatedAt: Instant?,
)
