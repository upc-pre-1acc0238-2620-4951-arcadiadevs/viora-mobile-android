package pe.edu.upc.viora.features.phenology.presentation.state

import java.time.Instant
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.features.phenology.domain.entity.BbiClass
import pe.edu.upc.viora.features.phenology.domain.entity.BbiInterval
import pe.edu.upc.viora.features.phenology.domain.entity.HarvestRecord
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety

/** The plot facts the header shows next to "Alternancia". */
data class PlotSubtitle(val variety: OliveVariety, val areaHectares: Double)

/** What happened to a campaign, to confirm it to the producer. */
enum class CampaignChangeKind { ADDED, CORRECTED, DELETED }

/** A confirmation shown once at the top of the screen. */
data class HarvestNotice(val kind: CampaignChangeKind, val year: Int)

/** What the alternation screen (US20) shows. */
sealed interface HarvestHistoryUiState {

    /** First download in progress and nothing cached. */
    data object Loading : HarvestHistoryUiState

    /** Download failed and there is nothing cached. */
    data class Error(val error: AppError) : HarvestHistoryUiState

    /**
     * The cached history. [records] are newest first. [index] and [bbiClass] are null while there
     * are fewer than three campaigns. [offline] is true when the last refresh could not reach the
     * server: the cache is shown and nothing can be changed.
     */
    data class Content(
        val plotName: String,
        val plotSubtitle: PlotSubtitle?,
        val records: List<HarvestRecord>,
        val index: Double?,
        val bbiClass: BbiClass?,
        val intervals: List<BbiInterval>,
        val averageKg: Double,
        val summary: HarvestSummary,
        val suggestedYear: Int,
        val offline: Boolean,
        val lastRefresh: Instant?,
        val isRefreshing: Boolean,
        val newRecordId: String?,
        val notice: HarvestNotice?,
    ) : HarvestHistoryUiState {
        val hasIndex: Boolean get() = index != null && bbiClass != null
        val canEdit: Boolean get() = !offline
    }
}
