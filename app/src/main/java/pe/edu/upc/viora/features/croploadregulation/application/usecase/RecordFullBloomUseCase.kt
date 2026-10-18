package pe.edu.upc.viora.features.croploadregulation.application.usecase

import java.time.LocalDate
import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.croploadregulation.domain.entity.ThinningPrescription
import pe.edu.upc.viora.features.croploadregulation.domain.repository.ThinningPrescriptionRepository

/** Records the full bloom of the campaign (P64); the plan comes back with its window. */
class RecordFullBloomUseCase @Inject constructor(private val repository: ThinningPrescriptionRepository) {
    suspend operator fun invoke(plotId: String, campaignYear: Int, observedOn: LocalDate): AppResult<ThinningPrescription> =
        repository.recordFullBloom(plotId, campaignYear, observedOn)
}
