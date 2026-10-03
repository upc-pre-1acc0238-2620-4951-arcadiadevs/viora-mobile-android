package pe.edu.upc.viora.features.plotmanagement.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.entity.PlotChanges
import pe.edu.upc.viora.features.plotmanagement.domain.repository.PlotRepository
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId

/** Edits the name, planting frame and/or outline of a registered plot. */
class UpdatePlotUseCase @Inject constructor(private val repository: PlotRepository) {
    suspend operator fun invoke(id: PlotId, changes: PlotChanges): AppResult<Plot> = repository.update(id, changes)
}
