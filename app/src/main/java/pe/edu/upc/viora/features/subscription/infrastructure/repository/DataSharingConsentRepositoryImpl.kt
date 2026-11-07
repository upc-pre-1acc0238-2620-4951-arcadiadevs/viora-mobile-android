package pe.edu.upc.viora.features.subscription.infrastructure.repository

import java.time.Instant
import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.network.ApiCaller
import pe.edu.upc.viora.features.subscription.domain.entity.DataSharingConsent
import pe.edu.upc.viora.features.subscription.domain.entity.DataSharingStatus
import pe.edu.upc.viora.features.subscription.domain.repository.DataSharingConsentRepository
import pe.edu.upc.viora.features.subscription.infrastructure.remote.DataSharingConsentDto
import pe.edu.upc.viora.features.subscription.infrastructure.remote.DataSharingConsentService

class DataSharingConsentRepositoryImpl @Inject constructor(
    private val service: DataSharingConsentService,
    private val apiCaller: ApiCaller,
) : DataSharingConsentRepository {

    override suspend fun latest(): AppResult<DataSharingConsent?> =
        when (val result = apiCaller.call { service.list() }) {
            // The server answers newest first; a status this app does not know is skipped.
            is AppResult.Success -> AppResult.Success(result.value.firstNotNullOfOrNull { it.toDomain() })
            is AppResult.Failure -> result
        }

    override suspend fun withdraw(consentId: String): AppResult<DataSharingConsent> =
        when (val result = apiCaller.call { service.withdraw(consentId) }) {
            is AppResult.Success -> result.value.toDomain()?.let { AppResult.Success(it) }
                ?: AppResult.Failure(AppError.Unknown())
            is AppResult.Failure -> result
        }
}

internal fun DataSharingConsentDto.toDomain(): DataSharingConsent? {
    val status = DataSharingStatus.entries.firstOrNull { it.name == status } ?: return null
    return DataSharingConsent(
        id = id,
        organizationName = organizationName,
        managerName = managerName?.takeIf { it.isNotBlank() },
        status = status,
        acceptedAt = acceptedAt.toInstant(),
        withdrawnAt = withdrawnAt.toInstant(),
        planEndsAt = planEndsAt.toInstant(),
    )
}

private fun String?.toInstant(): Instant? = this?.takeIf { it.isNotBlank() }?.let(Instant::parse)
