package pe.edu.upc.viora.features.plotmanagement.application.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.first
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.entity.PlotSector
import pe.edu.upc.viora.features.plotmanagement.domain.entity.SectorChoice
import pe.edu.upc.viora.features.plotmanagement.domain.entity.SectorZone
import pe.edu.upc.viora.features.plotmanagement.domain.repository.PlotRepository
import pe.edu.upc.viora.features.plotmanagement.domain.repository.PlotSectorRepository
import pe.edu.upc.viora.features.plotmanagement.domain.repository.SectorPromptRepository

/** P24 · Elegir sector: the list of the license that sponsors the member. */
class GetLicenseSectorsUseCase @Inject constructor(private val repository: PlotSectorRepository) {
    suspend operator fun invoke(): AppResult<List<SectorZone>> = repository.sectors()
}

/** Gives a plot its sector. A written name outside the length the server takes is refused before sending. */
class AssignPlotSectorUseCase @Inject constructor(private val repository: PlotSectorRepository) {
    suspend operator fun invoke(plotId: String, choice: SectorChoice): AppResult<PlotSector> {
        if (choice is SectorChoice.Written && !isValidName(choice.name)) {
            return AppResult.Failure(AppError.Validation(code = INVALID_NAME))
        }
        return repository.assign(plotId, choice)
    }

    companion object {
        const val INVALID_NAME = "territory.sector_zone.name.length"

        fun isValidName(name: String): Boolean =
            name.trim().length in SectorChoice.MIN_NAME_LENGTH..SectorChoice.MAX_NAME_LENGTH
    }
}

/**
 * P06b · Sector de tus lotes: the member's active plots that have no sector yet, the ones they had before joining. It
 * is empty for a producer without a sponsored plan (the server refuses them the sectors) and when it cannot be told.
 * With [onlyIfUnanswered] it is also empty once the member saved or put off P06b, since it is asked only once.
 */
class GetPlotsWithoutSectorUseCase @Inject constructor(
    private val plots: PlotRepository,
    private val sectors: PlotSectorRepository,
    private val prompt: SectorPromptRepository,
) {
    suspend operator fun invoke(onlyIfUnanswered: Boolean): List<Plot> {
        if (onlyIfUnanswered && prompt.answered.first()) return emptyList()
        val active = plots.observePlots().first()
        if (active.isEmpty()) return emptyList()
        val placed = (sectors.plotSectors() as? AppResult.Success)?.value ?: return emptyList()
        val placedIds = placed.mapTo(mutableSetOf()) { it.plotId }
        return active.filter { it.id.value !in placedIds }
    }
}

/** «Guardar» or «Más tarde» of P06b: it is not asked again on this account. */
class MarkSectorPromptAnsweredUseCase @Inject constructor(private val prompt: SectorPromptRepository) {
    suspend operator fun invoke() = prompt.markAnswered()
}
