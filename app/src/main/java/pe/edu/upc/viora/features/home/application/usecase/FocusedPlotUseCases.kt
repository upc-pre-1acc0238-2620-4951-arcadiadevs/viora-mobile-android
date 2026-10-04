package pe.edu.upc.viora.features.home.application.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.features.home.domain.repository.FocusedPlotRepository

/** The id of the plot the producer chose to see in the Home, if any. */
class ObserveChosenPlotUseCase @Inject constructor(private val repository: FocusedPlotRepository) {
    operator fun invoke(): Flow<String?> = repository.chosenPlotId
}

/** The producer picked the plot the Home talks about. */
class ChoosePlotUseCase @Inject constructor(private val repository: FocusedPlotRepository) {
    suspend operator fun invoke(plotId: String) = repository.choose(plotId)
}
