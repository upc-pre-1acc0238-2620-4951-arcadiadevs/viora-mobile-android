package pe.edu.upc.viora.features.croploadregulation.domain.entity

/**
 * Detailed report containing overall statistics and individual tree measurements for a plot sampling.
 */
data class SamplingDetailedReport(
    val summary: SamplingSummary,
    val trees: List<TreeSample> = emptyList(),
)
