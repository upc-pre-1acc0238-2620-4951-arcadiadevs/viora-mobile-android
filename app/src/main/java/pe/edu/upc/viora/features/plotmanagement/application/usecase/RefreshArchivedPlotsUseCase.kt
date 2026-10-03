package pe.edu.upc.viora.features.plotmanagement.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.plotmanagement.domain.repository.PlotRepository

/** Reads the archived plots from the server into the local cache. */
class RefreshArchivedPlotsUseCase @Inject constructor(private val repository: PlotRepository) {
    suspend operator fun invoke(): AppResult<Unit> = repository.refreshArchived()
}
