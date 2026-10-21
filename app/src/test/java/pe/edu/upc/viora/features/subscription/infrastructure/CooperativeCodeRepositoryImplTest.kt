package pe.edu.upc.viora.features.subscription.infrastructure

import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.network.ApiCaller
import pe.edu.upc.viora.core.network.ApiErrorMapper
import pe.edu.upc.viora.features.subscription.domain.entity.CooperativeMembership
import pe.edu.upc.viora.features.subscription.domain.entity.RedemptionOutcome
import pe.edu.upc.viora.features.subscription.domain.valueobject.InvitationCode
import pe.edu.upc.viora.features.subscription.infrastructure.remote.CooperativeCodeRedemptionService
import pe.edu.upc.viora.features.subscription.infrastructure.remote.RedeemCooperativeCodeRequestDto
import pe.edu.upc.viora.features.subscription.infrastructure.remote.SubscriptionDto
import pe.edu.upc.viora.features.subscription.infrastructure.repository.CooperativeCodeRepositoryImpl
import retrofit2.Response

private class FakeRedemptionService : CooperativeCodeRedemptionService {
    var response: () -> Response<SubscriptionDto> = { Response.success(201, subscriptionDto()) }
    val requests = mutableListOf<RedeemCooperativeCodeRequestDto>()

    override suspend fun redeem(request: RedeemCooperativeCodeRequestDto): Response<SubscriptionDto> {
        requests += request
        return response()
    }
}

private fun subscriptionDto() = SubscriptionDto(
    id = "s1",
    mode = "PLAN_COOPERATIVO_PATROCINADO",
    status = "ACTIVE",
    quotaHa = 12.0,
    cooperativeName = "Coop. Olivarera La Yarada",
)

private fun problem(status: Int) =
    Response.error<SubscriptionDto>(status, "{\"status\":$status}".toResponseBody("application/problem+json".toMediaType()))

@OptIn(ExperimentalCoroutinesApi::class)
class CooperativeCodeRepositoryImplTest {

    private val service = FakeRedemptionService()
    private val repository = CooperativeCodeRepositoryImpl(
        service = service,
        apiCaller = ApiCaller(ApiErrorMapper(Json), UnconfinedTestDispatcher()),
    )
    private val code = checkNotNull(InvitationCode.of("coop-7k2q"))

    @Test
    fun `a redeemed code returns the cooperative and its hectares and sends only the normalized code`() = runTest {
        val outcome = repository.redeem(code)

        assertEquals(RedemptionOutcome.Redeemed(CooperativeMembership("Coop. Olivarera La Yarada", 12.0)), outcome)
        assertEquals(listOf(RedeemCooperativeCodeRequestDto("COOP-7K2Q")), service.requests)
    }

    @Test
    fun `a 400 means the code cannot be redeemed`() = runTest {
        service.response = { problem(400) }

        assertEquals(RedemptionOutcome.NotRedeemable, repository.redeem(code))
    }

    @Test
    fun `a 409 means the producer already has a subscription`() = runTest {
        service.response = { problem(409) }

        assertEquals(RedemptionOutcome.AlreadySubscribed, repository.redeem(code))
    }

    @Test
    fun `a 429 means too many attempts`() = runTest {
        service.response = { problem(429) }

        assertEquals(RedemptionOutcome.TooManyAttempts, repository.redeem(code))
    }

    @Test
    fun `any other failure is kept as it came`() = runTest {
        service.response = { throw IOException("no route") }

        assertEquals(RedemptionOutcome.Failed(AppError.Offline), repository.redeem(code))
    }
}
