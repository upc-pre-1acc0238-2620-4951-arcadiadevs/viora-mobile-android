package pe.edu.upc.viora.features.croploadregulation.domain.entity

import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.SamplingStatus

/**
 * Overview of a plot's sampling state for the plot selection screen.
 */
data class PlotSamplingOverview(
    val plotId: String,
    val plotName: String,
    val variety: String,
    val areaHectares: Double,
    val campaignYear: Int,
    val samplingStatus: SamplingStatus,
    val sampledTreesCount: Int,
    val treesNeeded: Int,
    val isRepresentative: Boolean,
)
