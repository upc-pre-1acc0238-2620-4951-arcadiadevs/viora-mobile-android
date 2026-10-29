package pe.edu.upc.viora.features.phenology.application.usecase

import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import pe.edu.upc.viora.features.phenology.domain.CurrentPhase
import pe.edu.upc.viora.features.phenology.domain.OlivePhases
import pe.edu.upc.viora.features.phenology.domain.PlotPhaseDates
import pe.edu.upc.viora.features.phenology.domain.repository.ChillRepository

/**
 * The phase of the olive year a plot is in today, with the window of each phase and its winter chill. The plot's
 * recorded full bloom and the window of its thinning plan ([dates], which other contexts know) replace the usual
 * dates; without a plot the usual calendar is used.
 */
class ObserveCurrentPhaseUseCase @Inject constructor(
    private val chillRepository: ChillRepository,
    private val clock: Clock,
) {
    operator fun invoke(plotId: String?, dates: PlotPhaseDates = PlotPhaseDates()): Flow<CurrentPhase> {
        val chill = if (plotId == null) flowOf(null) else chillRepository.observeChillTracker(plotId)
        return chill.map { OlivePhases.of(LocalDate.now(clock), dates, it) }.distinctUntilChanged()
    }
}
