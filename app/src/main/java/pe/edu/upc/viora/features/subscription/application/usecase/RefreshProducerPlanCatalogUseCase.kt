package pe.edu.upc.viora.features.subscription.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.features.subscription.domain.repository.ProducerPlanRepository

class RefreshProducerPlanCatalogUseCase @Inject constructor(private val repository: ProducerPlanRepository) {
    suspend operator fun invoke() = repository.refresh()
}
