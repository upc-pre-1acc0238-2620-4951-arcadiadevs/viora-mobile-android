package pe.edu.upc.viora.features.plotmanagement.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.repository.PlotRepository
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId

/** Brings an archived plot back to the active ones. */
class RestorePlotUseCase @Inject constructor(private val repository: PlotRepository) {
    suspend operator fun invoke(id: PlotId): AppResult<Plot> = repository.restore(id)
}
