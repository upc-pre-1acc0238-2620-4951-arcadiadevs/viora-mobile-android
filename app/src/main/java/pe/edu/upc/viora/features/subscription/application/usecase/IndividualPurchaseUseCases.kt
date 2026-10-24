package pe.edu.upc.viora.features.subscription.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.features.subscription.domain.repository.IndividualPurchaseRepository
import pe.edu.upc.viora.features.subscription.domain.valueobject.PlanCapacity
import pe.edu.upc.viora.features.subscription.domain.valueobject.ProducerPlanCode

class PrepareIndividualPurchaseUseCase @Inject constructor(private val repository: IndividualPurchaseRepository) {
    suspend operator fun invoke(plan: ProducerPlanCode, capacity: PlanCapacity) = repository.prepare(plan, capacity)
}

class PrepareRenewalPurchaseUseCase @Inject constructor(private val repository: IndividualPurchaseRepository) {
    suspend operator fun invoke(plan: ProducerPlanCode, capacity: PlanCapacity) = repository.prepareRenewal(plan, capacity)
}

class ResumeIndividualPurchaseUseCase @Inject constructor(private val repository: IndividualPurchaseRepository) {
    suspend operator fun invoke() = repository.resume()
}

class CreateIndividualCheckoutUseCase @Inject constructor(private val repository: IndividualPurchaseRepository) {
    suspend operator fun invoke(subscriptionId: String) = repository.checkout(subscriptionId)
}
