package pe.edu.upc.viora.features.plotmanagement.presentation.state

import java.time.Instant
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId

/** What the plots list screen shows. The states are mutually exclusive. */
sealed interface PlotsUiState {

    /** Nothing cached yet and the first download is in progress. */
    data object Loading : PlotsUiState

    /** The producer has no active plots. */
    data object Empty : PlotsUiState

    /** Nothing cached and the download failed: offer a retry. */
    data class Error(val error: AppError) : PlotsUiState

    /**
     * Cached plots, possibly stale. [refreshError] is the last download failure while the
     * cache is still shown; [lastRefresh] lets the UI say how old the data is.
     */
    data class Content(
        val plots: List<Plot>,
        val isRefreshing: Boolean,
        val refreshError: AppError?,
        val lastRefresh: Instant?,
        val archivedPlots: List<Plot> = emptyList(),
        val filter: PlotsFilter = PlotsFilter.ACTIVE,
        /** The archived plot being restored right now, if any. */
        val restoringId: PlotId? = null,
        val restoreError: AppError? = null,
    ) : PlotsUiState
}

/** Which plots the list shows: the ones being farmed or the archived ones. */
enum class PlotsFilter { ACTIVE, ARCHIVED }
