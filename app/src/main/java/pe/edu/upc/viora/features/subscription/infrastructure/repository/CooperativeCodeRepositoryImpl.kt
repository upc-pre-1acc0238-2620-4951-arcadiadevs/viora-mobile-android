package pe.edu.upc.viora.features.subscription.infrastructure.repository

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.network.ApiCaller
import pe.edu.upc.viora.features.subscription.domain.entity.CooperativeMembership
import pe.edu.upc.viora.features.subscription.domain.entity.RedemptionOutcome
import pe.edu.upc.viora.features.subscription.domain.repository.CooperativeCodeRepository
import pe.edu.upc.viora.features.subscription.domain.valueobject.InvitationCode
import pe.edu.upc.viora.features.subscription.infrastructure.remote.CooperativeCodeRedemptionService
import pe.edu.upc.viora.features.subscription.infrastructure.remote.RedeemCooperativeCodeRequestDto
import retrofit2.HttpException

class CooperativeCodeRepositoryImpl @Inject constructor(
    private val service: CooperativeCodeRedemptionService,
    private val apiCaller: ApiCaller,
) : CooperativeCodeRepository {

    override suspend fun redeem(code: InvitationCode): RedemptionOutcome =
        when (val result = apiCaller.call { service.redeem(RedeemCooperativeCodeRequestDto(code.value)) }) {
            is AppResult.Success -> RedemptionOutcome.Redeemed(
                CooperativeMembership(
                    cooperativeName = result.value.cooperativeName.orEmpty(),
                    quotaHectares = result.value.quotaHa,
                ),
            )
            is AppResult.Failure -> result.error.toOutcome()
        }

    private fun AppError.toOutcome(): RedemptionOutcome = when {
        this is AppError.Validation -> RedemptionOutcome.NotRedeemable
        this is AppError.Conflict -> RedemptionOutcome.AlreadySubscribed
        // The shared mapper has no type for 429, so it reaches here as an unknown HTTP failure.
        this is AppError.Unknown && (cause as? HttpException)?.code() == TOO_MANY_REQUESTS -> RedemptionOutcome.TooManyAttempts
        else -> RedemptionOutcome.Failed(this)
    }

    private companion object {
        const val TOO_MANY_REQUESTS = 429
    }
}
