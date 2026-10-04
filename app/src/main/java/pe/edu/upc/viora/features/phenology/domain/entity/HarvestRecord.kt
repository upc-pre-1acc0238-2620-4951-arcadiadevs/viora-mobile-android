package pe.edu.upc.viora.features.phenology.domain.entity

import java.time.Instant

/**
 * The yield of one past campaign of a plot (US20). [greenKg] and [blackKg] are the optional
 * split by fruit maturity; [recordedAt] is when the server stored the record.
 */
data class HarvestRecord(
    val id: String,
    val plotId: String,
    val campaignYear: Int,
    val totalYieldKg: Double,
    val greenKg: Double?,
    val blackKg: Double?,
    val bearing: BearingYear,
    val recordedAt: Instant,
)
