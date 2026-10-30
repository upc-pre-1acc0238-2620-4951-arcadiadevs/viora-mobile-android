package pe.edu.upc.viora.features.subscription.infrastructure.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.network.ApiCaller
import pe.edu.upc.viora.features.subscription.domain.entity.IndividualCheckout
import pe.edu.upc.viora.features.subscription.domain.entity.PlanExtensionTerms
import pe.edu.upc.viora.features.subscription.domain.entity.QuotaExtension
import pe.edu.upc.viora.features.subscription.domain.repository.PlanExtensionRepository
import pe.edu.upc.viora.features.subscription.domain.valueobject.ExtensionCode
import pe.edu.upc.viora.features.subscription.infrastructure.mapper.toDomain
import pe.edu.upc.viora.features.subscription.infrastructure.remote.PlanExtensionService
import pe.edu.upc.viora.features.subscription.infrastructure.remote.RedeemExtensionCodeDto
import pe.edu.upc.viora.features.subscription.infrastructure.remote.RequestQuotaExtensionDto

/**
 * Plan extensions against the backend. The extension the producer goes to pay is kept in the
 * preferences, because Mercado Pago brings them back through a link that carries nothing of ours.
 */
@Singleton
class PlanExtensionRepositoryImpl @Inject constructor(
    private val service: PlanExtensionService,
    private val caller: ApiCaller,
    private val dataStore: DataStore<Preferences>,
) : PlanExtensionRepository {

    override suspend fun terms(subscriptionId: String): AppResult<PlanExtensionTerms> =
        mapped(caller.call { service.terms(subscriptionId) }) { it.toDomain() }

    override suspend fun request(subscriptionId: String, addedHectares: BigDecimal, operationKey: String) =
        mapped(caller.call { service.request(subscriptionId, operationKey, RequestQuotaExtensionDto(addedHectares)) }) {
            it.toDomain()
        }

    override suspend fun get(subscriptionId: String, extensionId: String): AppResult<QuotaExtension> =
        mapped(caller.call { service.get(subscriptionId, extensionId) }) { it.toDomain() }

    override suspend fun checkout(extension: QuotaExtension, operationKey: String): AppResult<IndividualCheckout> {
        val result = mapped(caller.call { service.checkout(extension.subscriptionId, extension.id, operationKey) }) {
            it.toDomain()
        }
        if (result is AppResult.Success) {
            dataStore.edit {
                it[PENDING_SUBSCRIPTION] = extension.subscriptionId
                it[PENDING_EXTENSION] = extension.id
            }
        }
        return result
    }

    override suspend fun pendingPayment(): Pair<String, String>? {
        val prefs = dataStore.data.first()
        val subscriptionId = prefs[PENDING_SUBSCRIPTION] ?: return null
        val extensionId = prefs[PENDING_EXTENSION] ?: return null
        return subscriptionId to extensionId
    }

    override suspend fun forgetPendingPayment() {
        dataStore.edit {
            it.remove(PENDING_SUBSCRIPTION)
            it.remove(PENDING_EXTENSION)
        }
    }

    override suspend fun redeem(code: ExtensionCode): AppResult<QuotaExtension> =
        mapped(caller.call { service.redeem(RedeemExtensionCodeDto(code.value)) }) { it.toDomain() }

    private inline fun <T, R> mapped(result: AppResult<T>, transform: (T) -> R): AppResult<R> = when (result) {
        is AppResult.Success -> runCatching { transform(result.value) }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { AppResult.Failure(AppError.Unknown(it)) },
        )
        is AppResult.Failure -> result
    }

    internal companion object {
        val PENDING_SUBSCRIPTION = stringPreferencesKey("plan_extension_pending_subscription")
        val PENDING_EXTENSION = stringPreferencesKey("plan_extension_pending_id")
    }
}
