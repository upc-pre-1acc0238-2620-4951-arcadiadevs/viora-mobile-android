package pe.edu.upc.viora.features.croploadregulation.application.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.features.croploadregulation.domain.entity.ThinningExecution
import pe.edu.upc.viora.features.croploadregulation.domain.repository.ThinningPrescriptionRepository

/** The recorded thinning of a plot and campaign as cached on the phone (P63). */
class ObserveThinningExecutionUseCase @Inject constructor(private val repository: ThinningPrescriptionRepository) {
    operator fun invoke(plotId: String, campaignYear: Int): Flow<ThinningExecution?> =
        repository.observeExecution(plotId, campaignYear)
}
