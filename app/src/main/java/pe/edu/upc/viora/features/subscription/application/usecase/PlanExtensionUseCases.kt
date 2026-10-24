package pe.edu.upc.viora.features.subscription.application.usecase

import java.math.BigDecimal
import javax.inject.Inject
import pe.edu.upc.viora.features.subscription.domain.entity.QuotaExtension
import pe.edu.upc.viora.features.subscription.domain.repository.PlanExtensionRepository
import pe.edu.upc.viora.features.subscription.domain.valueobject.ExtensionCode

class GetPlanExtensionTermsUseCase @Inject constructor(private val repository: PlanExtensionRepository) {
    suspend operator fun invoke(subscriptionId: String) = repository.terms(subscriptionId)
}

class RequestPaidExtensionUseCase @Inject constructor(private val repository: PlanExtensionRepository) {
    suspend operator fun invoke(subscriptionId: String, addedHectares: BigDecimal, operationKey: String) =
        repository.request(subscriptionId, addedHectares, operationKey)
}

class GetQuotaExtensionUseCase @Inject constructor(private val repository: PlanExtensionRepository) {
    suspend operator fun invoke(subscriptionId: String, extensionId: String) = repository.get(subscriptionId, extensionId)
}

class CreateExtensionCheckoutUseCase @Inject constructor(private val repository: PlanExtensionRepository) {
    suspend operator fun invoke(extension: QuotaExtension, operationKey: String) = repository.checkout(extension, operationKey)
}

class PendingExtensionPaymentUseCase @Inject constructor(private val repository: PlanExtensionRepository) {
    suspend fun find() = repository.pendingPayment()
    suspend fun forget() = repository.forgetPendingPayment()
}

class RedeemExtensionCodeUseCase @Inject constructor(private val repository: PlanExtensionRepository) {
    suspend operator fun invoke(code: ExtensionCode) = repository.redeem(code)
}
