package pe.edu.upc.viora.features.subscription.infrastructure.repository

import javax.inject.Inject
import javax.inject.Singleton
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.network.ApiCaller
import pe.edu.upc.viora.features.subscription.domain.entity.ProducerSubscription
import pe.edu.upc.viora.features.subscription.domain.repository.ProducerSubscriptionRepository
import pe.edu.upc.viora.features.subscription.infrastructure.mapper.toDomain
import pe.edu.upc.viora.features.subscription.infrastructure.remote.ProducerSubscriptionService

@Singleton
class ProducerSubscriptionRepositoryImpl @Inject constructor(
    private val service: ProducerSubscriptionService,
    private val caller: ApiCaller,
) : ProducerSubscriptionRepository {

    override suspend fun getCurrent(): AppResult<ProducerSubscription?> =
        when (val result = caller.callOptional { service.getCurrent() }) {
            is AppResult.Success -> runCatching { result.value?.toDomain() }
                .fold(
                    onSuccess = { AppResult.Success(it) },
                    onFailure = { AppResult.Failure(AppError.Unknown(it)) },
                )
            is AppResult.Failure -> result
        }

    override suspend fun getById(id: String): AppResult<ProducerSubscription> =
        when (val result = caller.call { service.getById(id) }) {
            is AppResult.Success -> runCatching { result.value.toDomain() }
                .fold(
                    onSuccess = { AppResult.Success(it) },
                    onFailure = { AppResult.Failure(AppError.Unknown(it)) },
                )
            is AppResult.Failure -> result
        }
}
