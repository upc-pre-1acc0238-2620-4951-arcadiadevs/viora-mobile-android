package pe.edu.upc.viora.features.phenology.application.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import pe.edu.upc.viora.features.phenology.domain.AlternationSummary

/** The alternate bearing of [plotId] (BBI) for the Home, the plot detail and any other screen that shows it. */
class ObserveAlternationSummaryUseCase @Inject constructor(
    private val observeHarvests: ObserveHarvestHistoryUseCase,
    private val observeBearingIndex: ObserveBearingIndexUseCase,
    private val observeHarvestRefresh: ObserveHarvestLastRefreshUseCase,
) {
    operator fun invoke(plotId: String): Flow<AlternationSummary?> = combine(
        observeHarvests(plotId),
        observeBearingIndex(plotId),
        observeHarvestRefresh(plotId),
    ) { records, index, lastRefresh -> AlternationSummary.of(records, index?.value, lastRefresh) }
}
