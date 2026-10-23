package pe.edu.upc.viora.features.subscription.infrastructure.repository

import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.map
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.network.ApiCaller
import pe.edu.upc.viora.features.subscription.domain.entity.ProducerPlanCatalog
import pe.edu.upc.viora.features.subscription.domain.repository.ProducerPlanRepository
import pe.edu.upc.viora.features.subscription.infrastructure.local.PRODUCER_PLAN_CATALOG_KEY
import pe.edu.upc.viora.features.subscription.infrastructure.local.ProducerPlanDao
import pe.edu.upc.viora.features.subscription.infrastructure.mapper.toDomain
import pe.edu.upc.viora.features.subscription.infrastructure.mapper.toEntity
import pe.edu.upc.viora.features.subscription.infrastructure.remote.ProducerPlanService

/** All offers are validated before the old cache is replaced; observation is always backed by Room. */
class ProducerPlanRepositoryImpl @Inject constructor(
    private val service: ProducerPlanService,
    private val dao: ProducerPlanDao,
    private val caller: ApiCaller,
    private val clock: Clock,
) : ProducerPlanRepository {
    override fun observeCatalog() = dao.observeCatalog(PRODUCER_PLAN_CATALOG_KEY).map { snapshot ->
        ProducerPlanCatalog(
            snapshot?.offers.orEmpty().map { it.toDomain() }.sortedBy { it.code.value },
            snapshot?.metadata?.fetchedAtEpochMs?.let(Instant::ofEpochMilli),
        )
    }

    override suspend fun refresh(): AppResult<Unit> {
        val result = caller.call { service.getOffers() }
        if (result is AppResult.Failure) return result
        return try {
            val offers = (result as AppResult.Success).value.map { it.toDomain() }
            require(offers.map { it.code }.distinct().size == offers.size) { "subscription.catalog.duplicate_code" }
            dao.replaceCatalog(offers.map { it.toEntity() }, clock.millis())
            AppResult.Success(Unit)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (exception: Exception) {
            AppResult.Failure(AppError.Unknown(exception))
        }
    }
}
