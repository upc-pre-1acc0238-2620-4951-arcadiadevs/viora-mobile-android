package pe.edu.upc.viora.features.subscription.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.features.subscription.domain.repository.ProducerPlanRepository

class ObserveProducerPlanCatalogUseCase @Inject constructor(private val repository: ProducerPlanRepository) {
    operator fun invoke() = repository.observeCatalog()
}
