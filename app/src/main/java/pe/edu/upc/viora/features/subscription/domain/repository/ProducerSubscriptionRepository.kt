package pe.edu.upc.viora.features.subscription.domain.repository

import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.subscription.domain.entity.ProducerSubscription

interface ProducerSubscriptionRepository {
    /**
     * Reads the current subscription of the authenticated producer.
     * Answers [AppResult.Success] with null when the backend returns 204 No Content.
     */
    suspend fun getCurrent(): AppResult<ProducerSubscription?>

    /**
     * Reads a specific subscription contract owned by the authenticated producer.
     */
    suspend fun getById(id: String): AppResult<ProducerSubscription>
}
