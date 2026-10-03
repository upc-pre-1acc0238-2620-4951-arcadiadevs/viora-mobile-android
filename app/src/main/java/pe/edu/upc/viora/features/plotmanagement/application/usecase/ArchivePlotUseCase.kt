package pe.edu.upc.viora.features.plotmanagement.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.plotmanagement.domain.repository.PlotRepository
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId

/** Archives a plot: it leaves the active inventory and frees its place in the plan. */
class ArchivePlotUseCase @Inject constructor(private val repository: PlotRepository) {
    suspend operator fun invoke(id: PlotId): AppResult<Unit> = repository.archive(id)
}
