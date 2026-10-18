package pe.edu.upc.viora.features.croploadregulation.application.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.features.croploadregulation.domain.entity.CachedPrescription
import pe.edu.upc.viora.features.croploadregulation.domain.repository.ThinningPrescriptionRepository

/** The cached thinning plan of a plot and campaign; null until it was downloaded once. */
class ObserveThinningPrescriptionUseCase @Inject constructor(private val repository: ThinningPrescriptionRepository) {
    operator fun invoke(plotId: String, campaignYear: Int): Flow<CachedPrescription?> =
        repository.observePrescription(plotId, campaignYear)
}
