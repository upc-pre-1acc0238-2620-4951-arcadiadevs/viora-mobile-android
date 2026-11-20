package pe.edu.upc.viora.features.phenology.application.usecase

import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import pe.edu.upc.viora.core.domain.OliveVariety
import pe.edu.upc.viora.features.phenology.domain.CurrentPhase
import pe.edu.upc.viora.features.phenology.domain.OlivePhases
import pe.edu.upc.viora.features.phenology.domain.PhaseAdjustments
import pe.edu.upc.viora.features.phenology.domain.PhaseProfiles
import pe.edu.upc.viora.features.phenology.domain.PlotPhaseDates
import pe.edu.upc.viora.features.phenology.domain.repository.ChillRepository
import pe.edu.upc.viora.features.phenology.domain.repository.PhaseAdjustmentsRepository

/**
 * The phase of the olive year a plot is in today, with the window of each phase and its winter chill. The plot's
 * recorded full bloom and the window of its thinning plan ([dates], which other contexts know) replace the usual
 * dates of its [variety], and so do the start dates its producer corrected; without a plot the usual calendar is used.
 */
class ObserveCurrentPhaseUseCase @Inject constructor(
    private val chillRepository: ChillRepository,
    private val adjustmentsRepository: PhaseAdjustmentsRepository,
    private val clock: Clock,
) {
    operator fun invoke(
        plotId: String?,
        dates: PlotPhaseDates = PlotPhaseDates(),
        variety: OliveVariety? = null,
    ): Flow<CurrentPhase> {
        val chill = if (plotId == null) flowOf(null) else chillRepository.observeChillTracker(plotId)
        val adjustments = if (plotId == null) flowOf(PhaseAdjustments.NONE) else adjustmentsRepository.observe(plotId)
        val profile = PhaseProfiles.forVariety(variety)
        return combine(chill, adjustments) { tracker, corrected ->
            OlivePhases.of(LocalDate.now(clock), dates, tracker, corrected, profile)
        }.distinctUntilChanged()
    }
}
