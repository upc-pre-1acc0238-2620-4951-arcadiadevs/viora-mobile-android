package pe.edu.upc.viora.features.phenology.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pe.edu.upc.viora.features.phenology.application.usecase.ObserveAlternationSummaryUseCase
import pe.edu.upc.viora.features.phenology.application.usecase.RefreshHarvestHistoryUseCase
import pe.edu.upc.viora.features.phenology.domain.AlternationSummary
import pe.edu.upc.viora.features.phenology.presentation.state.HarvestPresenter
import pe.edu.upc.viora.features.phenology.presentation.state.PlotAlternation

/**
 * Feeds the "Tu alternancia" card the Home shows for the plot in focus. The Home says which plot ([show]); the
 * harvests of that plot are brought up to date (best effort) whenever it changes.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlotAlternationViewModel @Inject constructor(
    private val observeAlternation: ObserveAlternationSummaryUseCase,
    private val refreshHarvests: RefreshHarvestHistoryUseCase,
    private val clock: Clock,
) : ViewModel() {

    private data class Shown(val plotId: String, val areaHectares: Double)

    private val shown = MutableStateFlow<Shown?>(null)

    /** What the card says; null until the plot's harvests are known (then the card is not drawn). */
    val alternation: StateFlow<PlotAlternation?> = shown.flatMapLatest { plot ->
        if (plot == null) flowOf(null) else observeAlternation(plot.plotId).map { alternationOf(plot.areaHectares, it) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun show(plotId: String, areaHectares: Double) {
        val previous = shown.value?.plotId
        shown.value = Shown(plotId, areaHectares)
        if (previous != plotId) viewModelScope.launch { refreshHarvests(plotId) }
    }

    private fun alternationOf(areaHectares: Double, summary: AlternationSummary?): PlotAlternation? {
        // Nothing cached and never downloaded: say nothing instead of a wrong "you miss 3 campaigns".
        if (summary == null) return null
        if (summary.missingCampaigns > 0) return PlotAlternation.Insufficient(summary.missingCampaigns)
        if (summary.index == null) return null
        val voice = HarvestPresenter.summarize(summary.records, summary.bbiClass, LocalDate.now(clock).year).voice
        return PlotAlternation.Ready(
            records = summary.records.sortedBy { it.campaignYear }.takeLast(LAST_CAMPAIGNS),
            areaHectares = areaHectares,
            voice = voice,
        )
    }

    private companion object {
        const val LAST_CAMPAIGNS = 5
    }
}
