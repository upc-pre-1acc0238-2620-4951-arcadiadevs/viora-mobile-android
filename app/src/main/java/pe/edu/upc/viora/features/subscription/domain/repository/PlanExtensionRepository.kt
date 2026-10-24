package pe.edu.upc.viora.features.subscription.domain.repository

import java.math.BigDecimal
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.subscription.domain.entity.IndividualCheckout
import pe.edu.upc.viora.features.subscription.domain.entity.PlanExtensionTerms
import pe.edu.upc.viora.features.subscription.domain.entity.QuotaExtension
import pe.edu.upc.viora.features.subscription.domain.valueobject.ExtensionCode

/** Growing the plan in force: P102 (paid) and P103 (code). */
interface PlanExtensionRepository {
    suspend fun terms(subscriptionId: String): AppResult<PlanExtensionTerms>

    /** Requests [addedHectares] pending payment; [operationKey] makes a retry return the same extension. */
    suspend fun request(subscriptionId: String, addedHectares: BigDecimal, operationKey: String): AppResult<QuotaExtension>

    suspend fun get(subscriptionId: String, extensionId: String): AppResult<QuotaExtension>

    /** The Mercado Pago link of a pending extension; it also remembers it for the return from the browser. */
    suspend fun checkout(extension: QuotaExtension, operationKey: String): AppResult<IndividualCheckout>

    /** The extension the producer went to pay in the browser, so the return link can show its result. */
    suspend fun pendingPayment(): Pair<String, String>?

    suspend fun forgetPendingPayment()

    suspend fun redeem(code: ExtensionCode): AppResult<QuotaExtension>
}
