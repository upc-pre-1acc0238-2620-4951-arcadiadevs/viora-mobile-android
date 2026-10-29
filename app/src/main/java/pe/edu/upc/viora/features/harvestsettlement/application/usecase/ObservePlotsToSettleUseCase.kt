package pe.edu.upc.viora.features.harvestsettlement.application.usecase

import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import pe.edu.upc.viora.features.harvestsettlement.domain.PlotToSettle
import pe.edu.upc.viora.features.harvestsettlement.domain.PlotsToSettleRule

/**
 * The plots still to register for this year's harvest, for the Home card and the picker (P70). The caller
 * passes its active plots as [candidates], so this context never reads another context's plots itself.
 */
class ObservePlotsToSettleUseCase @Inject constructor(
    private val observeSettlements: ObserveSettledHarvestsUseCase,
    private val observePending: ObservePendingSettlementsUseCase,
    private val clock: Clock,
) {
    operator fun invoke(candidates: Flow<List<PlotToSettle>>): Flow<List<PlotToSettle>> =
        combine(candidates, observeSettlements(), observePending()) { plots, settlements, pending ->
            PlotsToSettleRule.remaining(plots, settlements, pending, LocalDate.now(clock).year)
        }
}
