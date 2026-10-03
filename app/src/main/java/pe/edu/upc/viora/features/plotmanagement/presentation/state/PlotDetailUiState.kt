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
    data class Content(val plot: Plot, val showSavedNotice: Boolean) : PlotDetailUiState
}
