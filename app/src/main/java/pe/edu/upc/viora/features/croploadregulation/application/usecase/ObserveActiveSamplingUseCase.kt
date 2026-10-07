package pe.edu.upc.viora.features.croploadregulation.application.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.features.croploadregulation.domain.entity.PlotSamplingOverview
import pe.edu.upc.viora.features.croploadregulation.domain.repository.ThinningRepository

class ObserveActiveSamplingUseCase @Inject constructor(
    private val repository: ThinningRepository,
) {
    operator fun invoke(): Flow<PlotSamplingOverview?> = repository.observeActiveSampling()
}
