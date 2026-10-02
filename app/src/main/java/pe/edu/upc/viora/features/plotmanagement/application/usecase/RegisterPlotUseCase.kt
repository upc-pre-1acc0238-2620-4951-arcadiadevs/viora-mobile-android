package pe.edu.upc.viora.features.plotmanagement.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.plotmanagement.domain.entity.NewPlot
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.repository.PlotRepository

/** Registers a validated plot on the server and in the local cache. */
class RegisterPlotUseCase @Inject constructor(private val repository: PlotRepository) {
    suspend operator fun invoke(newPlot: NewPlot): AppResult<Plot> = repository.register(newPlot)
}
