package pe.edu.upc.viora.features.harvestsettlement.presentation.state

import java.time.Instant
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettlementStatus

/** The chips under the logbook title (Figma P50). Only [ALL] and [HARVESTS] have entries so far. */
enum class LogbookFilter { ALL, SAMPLINGS, THINNINGS, HARVESTS }

/** How recent a group of entries is: the calendar week, the rest of the month, anything older. */
enum class LogbookPeriod { THIS_WEEK, THIS_MONTH, EARLIER }

enum class LogbookRowType { HARVEST, SAMPLING, THINNING }

/** A logbook row (settled campaign, sampling or thinning). [plotName] is null when the plot is not in the cache. */
data class SettledHarvestEntry(
    val id: String,
    val plotId: String,
    val plotName: String?,
    val campaignYear: Int,
    val totalYieldKg: Double = 0.0,
    val status: SettlementStatus = SettlementStatus.SETTLED,
    val settledAt: Instant,
    val type: LogbookRowType = LogbookRowType.HARVEST,
    val evaluatedTreesCount: Int? = null,
    val meanFruitsPerShoot: Double? = null,
    val removalPercentage: Double? = null,
    val timeliness: String? = null,
)

data class ActiveSamplingUiModel(
    val plotId: String,
    val plotName: String,
    val completedTrees: Int,
    val targetTrees: Int,
)

data class LogbookGroup(val period: LogbookPeriod, val entries: List<SettledHarvestEntry>)

/** What the logbook (P50) shows. */
sealed interface LogbookUiState {

    /** First download in progress and nothing cached. */
    data object Loading : LogbookUiState

    /**
     * The cached entries of the selected [filter], grouped newest first. [refreshError] is the
     * last refresh failure, shown as a non-blocking message over the cached list; [offline] is
     * true when it was a connectivity problem. [lastPlotRefresh] feeds the header's sync text.
     */
    data class Content(
        val filter: LogbookFilter,
        val groups: List<LogbookGroup>,
        val activeSampling: ActiveSamplingUiModel? = null,
        val pendingLocalCount: Int = 0,
        val refreshError: AppError?,
        val lastPlotRefresh: Instant?,
        val isRefreshing: Boolean,
    ) : LogbookUiState {
        val offline: Boolean get() = refreshError is AppError.Offline || refreshError is AppError.Timeout
        val isEmpty: Boolean get() = groups.isEmpty() && activeSampling == null
    }
}
