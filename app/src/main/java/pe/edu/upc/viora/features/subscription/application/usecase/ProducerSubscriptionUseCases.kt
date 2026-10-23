package pe.edu.upc.viora.features.subscription.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.subscription.domain.entity.ProducerSubscription
import pe.edu.upc.viora.features.subscription.domain.repository.ProducerSubscriptionRepository

class GetCurrentSubscriptionUseCase @Inject constructor(
    private val repository: ProducerSubscriptionRepository,
) {
    suspend operator fun invoke(): AppResult<ProducerSubscription?> = repository.getCurrent()
}

class GetSubscriptionByIdUseCase @Inject constructor(
    private val repository: ProducerSubscriptionRepository,
) {
    suspend operator fun invoke(id: String): AppResult<ProducerSubscription> = repository.getById(id)
}
