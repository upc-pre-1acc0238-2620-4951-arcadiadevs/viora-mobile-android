package pe.edu.upc.viora.features.home.presentation.state

import java.time.Instant
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot

/**
 * What the Home (Figma P10 "Inicio") shows. The states are mutually exclusive.
 *
 * Only the parts of the Home that already have data behind them live here (the plots). The
 * other sections (phase card, weather, alerts, alternation) are plugged in by their own features.
 */
sealed interface HomeUiState {

    /** Whether the last download failed for lack of connection (shows the offline notice). */
    val isOffline: Boolean

    /** When the data shown was last synchronised with the server, if ever. */
    val lastRefresh: Instant?

    /** Number of active agroclimatic alerts. */
    val activeAlertsCount: Long get() = 0

    /** Nothing cached yet and the first download is in progress. */
    data object Loading : HomeUiState {
        override val isOffline = false
        override val lastRefresh: Instant? = null
    }

    /** Nothing cached and the download failed: offer a retry. */
    data class Error(val error: AppError) : HomeUiState {
        override val isOffline get() = error == AppError.Offline
        override val lastRefresh: Instant? = null
    }

    /** The producer has no active plots yet: the Home invites them to register the first one. */
    data class NoPlots(
        override val isOffline: Boolean,
        override val lastRefresh: Instant?,
        override val activeAlertsCount: Long = 0,
    ) : HomeUiState

    /**
     * Cached plots, possibly stale. [focusedPlot] is the one the cards of the Home talk about and
     * [alternation] what is known about its harvests (null until they are downloaded).
     */
    data class Content(
        val plots: List<Plot>,
        override val isOffline: Boolean,
        override val lastRefresh: Instant?,
        val isRefreshing: Boolean,
        override val activeAlertsCount: Long = 0,
        val focusedPlot: Plot? = plots.firstOrNull(),
        val alternation: HomeAlternation? = null,
    ) : HomeUiState
}
