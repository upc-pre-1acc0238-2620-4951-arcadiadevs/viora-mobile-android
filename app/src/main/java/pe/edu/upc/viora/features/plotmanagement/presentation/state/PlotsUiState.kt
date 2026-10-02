package pe.edu.upc.viora.features.plotmanagement.presentation.state

import java.time.Instant
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot

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
    ) : PlotsUiState
}
