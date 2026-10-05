package pe.edu.upc.viora.features.plotmanagement.presentation.state

import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot

/** What the plot detail screen shows. The states are mutually exclusive. */
sealed interface PlotDetailUiState {

    /** The plot has not been read from the cache yet. */
    data object Loading : PlotDetailUiState

    /** The cache has no plot with the requested id (e.g. it was archived on another device). */
    data object NotFound : PlotDetailUiState

    /**
     * The plot to show. [showSavedNotice] is true for a few seconds right after the plot was
     * registered, to confirm that it was saved.
     */
    data class Content(
        val plot: Plot,
        val showSavedNotice: Boolean,
        /** Hectares of all the producer's active plots, used to show what archiving this one frees. */
        val activeHectares: Double = plot.areaHectares,
        /** What the alternation card says; null until the plot's harvest history is known. */
        val harvest: LotHarvest? = null,
    ) : PlotDetailUiState
}

/**
 * The plot's bearing index for the "Tu lote" card: [index] is null while there are not enough
 * campaigns, and then [missingCampaigns] says how many are still needed.
 */
data class LotHarvest(val index: Double?, val missingCampaigns: Int)

/** Progress of archiving the plot being shown. */
enum class ArchiveState { Idle, Working, Failed, Done }
