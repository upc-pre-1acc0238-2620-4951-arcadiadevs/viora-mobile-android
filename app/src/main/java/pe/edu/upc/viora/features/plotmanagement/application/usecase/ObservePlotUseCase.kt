package pe.edu.upc.viora.features.plotmanagement.application.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.repository.PlotRepository
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId

/** One plot from the local cache, or `null` while it is not cached. */
class ObservePlotUseCase @Inject constructor(private val repository: PlotRepository) {
    operator fun invoke(id: PlotId): Flow<Plot?> = repository.observePlot(id)
}
