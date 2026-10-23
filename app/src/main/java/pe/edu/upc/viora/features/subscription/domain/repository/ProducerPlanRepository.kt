package pe.edu.upc.viora.features.subscription.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.subscription.domain.entity.ProducerPlanCatalog

/** Cached offer observation and explicit refresh, independent of cooperative redemption. */
interface ProducerPlanRepository {
    fun observeCatalog(): Flow<ProducerPlanCatalog>
    suspend fun refresh(): AppResult<Unit>
}
