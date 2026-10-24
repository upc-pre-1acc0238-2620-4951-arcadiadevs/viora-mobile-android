package pe.edu.upc.viora.features.subscription.domain.repository

import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.subscription.domain.entity.IndividualPurchase
import pe.edu.upc.viora.features.subscription.domain.entity.IndividualCheckout
import pe.edu.upc.viora.features.subscription.domain.valueobject.PlanCapacity
import pe.edu.upc.viora.features.subscription.domain.valueobject.ProducerPlanCode

interface IndividualPurchaseRepository {
    suspend fun prepare(plan: ProducerPlanCode, capacity: PlanCapacity): AppResult<IndividualPurchase>

    /**
     * Like [prepare] for the year after the plan in force: a journal whose purchase is already paid is
     * closed first, so the renewal becomes a contract of its own.
     */
    suspend fun prepareRenewal(plan: ProducerPlanCode, capacity: PlanCapacity): AppResult<IndividualPurchase>
    suspend fun resume(): AppResult<IndividualPurchase?>
    suspend fun checkout(subscriptionId: String): AppResult<IndividualCheckout>
}
