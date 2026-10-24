package pe.edu.upc.viora.features.subscription.infrastructure.repository

import java.util.UUID
import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import pe.edu.upc.viora.core.datastore.SessionStore
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.network.ApiCaller
import pe.edu.upc.viora.features.subscription.domain.entity.*
import pe.edu.upc.viora.features.subscription.domain.repository.IndividualPurchaseRepository
import pe.edu.upc.viora.features.subscription.domain.valueobject.*
import pe.edu.upc.viora.features.subscription.infrastructure.local.*
import pe.edu.upc.viora.features.subscription.infrastructure.mapper.toDomain
import pe.edu.upc.viora.features.subscription.infrastructure.remote.*

/** Writes durable operation identity first and never clears it for a transient failure. */
@Singleton
class IndividualPurchaseRepositoryImpl @Inject constructor(
    private val service: IndividualPurchaseService,
    private val dao: IndividualPurchaseDao,
    private val caller: ApiCaller,
    private val sessions: SessionStore,
    private val json: Json,
    private val clock: Clock,
) : IndividualPurchaseRepository {
    private val mutex = Mutex()

    override suspend fun prepare(plan: ProducerPlanCode, capacity: PlanCapacity): AppResult<IndividualPurchase> = guarded {
        val owner = owner() ?: return@guarded AppResult.Failure(AppError.Unauthorized)
        dao.insert(IndividualPurchaseEntity(owner, plan.value, capacity.hectares.toPlainString(),
            UUID.randomUUID().toString(), UUID.randomUUID().toString(), null, null, null, null))
        val row = requireNotNull(dao.find(owner))
        if (row.plan != plan.value || row.quota.toBigDecimal().compareTo(capacity.hectares) != 0) {
            return@guarded AppResult.Failure(AppError.Conflict(code = "subscription.purchase.selection_conflict"))
        }
        contract(row)
    }

    override suspend fun resume(): AppResult<IndividualPurchase?> = guarded {
        val owner = owner() ?: return@guarded AppResult.Failure(AppError.Unauthorized)
        val row = dao.find(owner) ?: return@guarded AppResult.Success(null)
        when (val result = contract(row)) {
            is AppResult.Success -> AppResult.Success(result.value)
            is AppResult.Failure -> result
        }
    }

    private suspend fun contract(row: IndividualPurchaseEntity): AppResult<IndividualPurchase> {
        if (owner() != row.owner) return AppResult.Failure(AppError.Unauthorized)
        val result = if (row.contractJson == null) {
            caller.call { service.create(row.createKey, IndividualPurchaseRequest(row.plan, row.quota.toBigDecimal())) }
        } else {
            caller.call { service.get(json.decodeFromString<IndividualPurchaseDto>(row.contractJson).id) }
        }
        if (owner() != row.owner) return AppResult.Failure(AppError.Unauthorized)
        val missingHistorical = result is AppResult.Failure && result.error is AppError.NotFound && row.contractJson != null
        val existingPurchase = result is AppResult.Failure && result.error is AppError.Conflict && row.contractJson == null
        if (missingHistorical || existingPurchase) {
            // A missing historical ID is not permission to create another purchase.
            // Only adopt an existing contract returned for the authenticated account.
            val current = caller.callOptional { service.current() }
            if (owner() != row.owner) return AppResult.Failure(AppError.Unauthorized)
            if (current is AppResult.Failure) return current
            val recovered = (current as AppResult.Success).value ?: return result
            if (recovered.mode != "PLAN_PRODUCTOR_INDIVIDUAL") return result
            val purchase = recovered.toDomain()
            val updated = row.copy(plan = purchase.planCode.value,
                quota = purchase.capacity.hectares.toPlainString(),
                contractJson = json.encodeToString(recovered), checkoutKey = UUID.randomUUID().toString(),
                intentId = null, checkoutUrl = null, expiresAt = null)
            if (dao.update(updated) == 0) return AppResult.Failure(AppError.Unauthorized)
            return AppResult.Success(purchase)
        }
        if (result is AppResult.Failure) return result
        val dto = (result as AppResult.Success).value
        val purchase = dto.toDomain()
        require(purchase.planCode.value == row.plan && purchase.capacity.hectares.compareTo(row.quota.toBigDecimal()) == 0)
        if (dao.update(row.copy(contractJson = json.encodeToString(dto))) == 0) return AppResult.Failure(AppError.Unauthorized)
        return AppResult.Success(purchase)
    }

    override suspend fun checkout(subscriptionId: String): AppResult<IndividualCheckout> = guarded {
        val owner = owner() ?: return@guarded AppResult.Failure(AppError.Unauthorized)
        var row = dao.find(owner) ?: return@guarded AppResult.Failure(AppError.Validation(code = "subscription.purchase.missing"))
        val purchase = row.contractJson?.let { json.decodeFromString<IndividualPurchaseDto>(it).toDomain() }
        if (purchase?.id != subscriptionId) return@guarded AppResult.Failure(AppError.Conflict())
        // Local expiry only requests backend reconciliation. It never authorizes another payment:
        // the server checks approved and unresolved evidence before creating a replacement.
        if (row.expiresAt?.let { !clock.instant().isBefore(Instant.parse(it)) } == true) {
            row = row.copy(checkoutKey = UUID.randomUUID().toString(), intentId = null, checkoutUrl = null, expiresAt = null)
            if (dao.update(row) == 0) return@guarded AppResult.Failure(AppError.Unauthorized)
        }
        if (owner() != owner) return@guarded AppResult.Failure(AppError.Unauthorized)
        var result = caller.call { service.checkout(subscriptionId, row.checkoutKey) }
        if (owner() != owner) return@guarded AppResult.Failure(AppError.Unauthorized)
        val conflict = (result as? AppResult.Failure)?.error as? AppError.Conflict
        if (conflict?.code == "SUBSCRIPTION_CONFLICT" &&
            conflict.properties?.get("reason")?.jsonPrimitive?.contentOrNull == "subscription.checkout.expired") {
            row = row.copy(checkoutKey = UUID.randomUUID().toString(), intentId = null, checkoutUrl = null, expiresAt = null)
            if (dao.update(row) == 0 || owner() != owner) return@guarded AppResult.Failure(AppError.Unauthorized)
            result = caller.call { service.checkout(subscriptionId, row.checkoutKey) }
        }
        if (owner() != owner) return@guarded AppResult.Failure(AppError.Unauthorized)
        if (result is AppResult.Failure) return@guarded result
        val link = (result as AppResult.Success).value.toDomain()
        if (dao.update(row.copy(intentId = link.intentId, checkoutUrl = link.url, expiresAt = link.expiresAt.toString())) == 0) {
            return@guarded AppResult.Failure(AppError.Unauthorized)
        }
        AppResult.Success(link)
    }

    private suspend fun owner() = sessions.session()?.userId?.takeIf { it.isNotBlank() }

    private suspend fun <T> guarded(block: suspend () -> AppResult<T>): AppResult<T> = mutex.withLock {
        try { block() }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (exception: Exception) { AppResult.Failure(AppError.Unknown(exception)) }
    }
}
