package pe.edu.upc.viora.features.croploadregulation.infrastructure.remote

import kotlinx.serialization.Serializable

/**
 * Response item from `GET /api/v1/samplings?campaignYear={year}` for P51 (plot selection & status).
 */
@Serializable
data class PlotSamplingStateResponseDto(
    val plotId: String,
    val plotName: String,
    val variety: String,
    val areaHectares: Double,
    val campaignYear: Int,
    val samplingStatus: String, // "NOT_STARTED", "IN_PROGRESS", "COMPLETED"
    val sampledTreesCount: Int,
    val treesNeeded: Int,
    val isRepresentative: Boolean,
)
