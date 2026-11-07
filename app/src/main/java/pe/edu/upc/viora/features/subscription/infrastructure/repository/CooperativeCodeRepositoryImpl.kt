package pe.edu.upc.viora.features.subscription.infrastructure.repository

import java.time.Instant
import javax.inject.Inject
import kotlinx.serialization.json.JsonPrimitive
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.network.ApiCaller
import pe.edu.upc.viora.features.subscription.domain.entity.CodePreview
import pe.edu.upc.viora.features.subscription.domain.entity.CodePreviewOutcome
import pe.edu.upc.viora.features.subscription.domain.entity.CooperativeMembership
import pe.edu.upc.viora.features.subscription.domain.entity.RedemptionOutcome
import pe.edu.upc.viora.features.subscription.domain.repository.CooperativeCodeRepository
import pe.edu.upc.viora.features.subscription.domain.valueobject.InvitationCode
import pe.edu.upc.viora.features.subscription.infrastructure.remote.CooperativeCodeRedemptionService
import pe.edu.upc.viora.features.subscription.infrastructure.remote.PreviewCooperativeCodeRequestDto
import pe.edu.upc.viora.features.subscription.infrastructure.remote.RedeemCooperativeCodeRequestDto
import retrofit2.HttpException

class CooperativeCodeRepositoryImpl @Inject constructor(
    private val service: CooperativeCodeRedemptionService,
    private val apiCaller: ApiCaller,
) : CooperativeCodeRepository {

    override suspend fun preview(code: InvitationCode): CodePreviewOutcome =
        when (val result = apiCaller.call { service.preview(PreviewCooperativeCodeRequestDto(code.value)) }) {
            is AppResult.Success -> with(result.value) {
                CodePreviewOutcome.Previewed(
                    CodePreview(
                        organizationName = organizationName,
                        managerName = managerName?.takeIf { it.isNotBlank() },
                        quotaHectares = quotaHa,
                        planEndsAt = planEndsAt?.takeIf { it.isNotBlank() }?.let(Instant::parse),
                        registeredHectares = registeredAreaHa,
                        exceedsQuota = exceedsQuota,
                        consentTextVersion = consentTextVersion,
                    ),
                )
            }
            is AppResult.Failure -> when {
                result.error is AppError.Validation -> CodePreviewOutcome.NotRedeemable
                result.error is AppError.Conflict -> CodePreviewOutcome.AlreadySubscribed
                result.error.isTooManyAttempts() -> CodePreviewOutcome.TooManyAttempts
                else -> CodePreviewOutcome.Failed(result.error)
            }
        }

    override suspend fun redeem(code: InvitationCode, consentTextVersion: String): RedemptionOutcome =
        when (val result = apiCaller.call { service.redeem(RedeemCooperativeCodeRequestDto(code.value, consentTextVersion)) }) {
            is AppResult.Success -> RedemptionOutcome.Redeemed(
                CooperativeMembership(
                    cooperativeName = result.value.cooperativeName.orEmpty(),
                    quotaHectares = result.value.quotaHa,
                    endsAt = result.value.endsAt?.takeIf { it.isNotBlank() }?.let(Instant::parse),
                ),
            )
            is AppResult.Failure -> result.error.toOutcome()
        }

    private fun AppError.toOutcome(): RedemptionOutcome = when {
        this is AppError.Validation -> RedemptionOutcome.NotRedeemable
        this is AppError.Conflict && reason() == CONSENT_OUTDATED -> RedemptionOutcome.ConsentOutdated
        this is AppError.Conflict -> RedemptionOutcome.AlreadySubscribed
        isTooManyAttempts() -> RedemptionOutcome.TooManyAttempts
        else -> RedemptionOutcome.Failed(this)
    }

    private fun AppError.Conflict.reason(): String? = (properties?.get(REASON) as? JsonPrimitive)?.content

    private fun AppError.isTooManyAttempts(): Boolean =
        this is AppError.RateLimited || (this is AppError.Unknown && (cause as? HttpException)?.code() == TOO_MANY_REQUESTS)

    private companion object {
        const val TOO_MANY_REQUESTS = 429
        const val REASON = "reason"
        const val CONSENT_OUTDATED = "subscription.consent.text_outdated"
    }
}
