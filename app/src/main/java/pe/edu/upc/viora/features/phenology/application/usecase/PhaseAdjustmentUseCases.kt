package pe.edu.upc.viora.features.phenology.application.usecase

import java.time.LocalDate
import java.time.MonthDay
import javax.inject.Inject
import pe.edu.upc.viora.features.phenology.domain.OlivePhase
import pe.edu.upc.viora.features.phenology.domain.repository.PhaseAdjustmentsRepository

/** The producer says that [phase] starts on [on] in their plot, every campaign. */
class AdjustPhaseStartUseCase @Inject constructor(private val repository: PhaseAdjustmentsRepository) {
    suspend operator fun invoke(plotId: String, phase: OlivePhase, on: LocalDate) =
        repository.adjust(plotId, phase, MonthDay.from(on))
}

/** The producer goes back to the usual date for the start of [phase]. */
class ResetPhaseStartUseCase @Inject constructor(private val repository: PhaseAdjustmentsRepository) {
    suspend operator fun invoke(plotId: String, phase: OlivePhase) = repository.reset(plotId, phase)
}
