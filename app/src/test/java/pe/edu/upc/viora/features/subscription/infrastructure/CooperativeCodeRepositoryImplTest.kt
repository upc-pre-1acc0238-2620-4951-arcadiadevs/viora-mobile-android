package pe.edu.upc.viora.features.subscription.infrastructure

import java.io.IOException
import java.time.Instant
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
import pe.edu.upc.viora.features.subscription.domain.entity.CodePreview
import pe.edu.upc.viora.features.subscription.domain.entity.CodePreviewOutcome
import pe.edu.upc.viora.features.subscription.domain.entity.CooperativeMembership
import pe.edu.upc.viora.features.subscription.domain.entity.RedemptionOutcome
import pe.edu.upc.viora.features.subscription.domain.valueobject.InvitationCode
import pe.edu.upc.viora.features.subscription.infrastructure.remote.CooperativeCodePreviewDto
import pe.edu.upc.viora.features.subscription.infrastructure.remote.CooperativeCodeRedemptionService
import pe.edu.upc.viora.features.subscription.infrastructure.remote.PreviewCooperativeCodeRequestDto
import pe.edu.upc.viora.features.subscription.infrastructure.remote.RedeemCooperativeCodeRequestDto
import pe.edu.upc.viora.features.subscription.infrastructure.remote.SubscriptionDto
import pe.edu.upc.viora.features.subscription.infrastructure.repository.CooperativeCodeRepositoryImpl
import retrofit2.Response

private class FakeRedemptionService : CooperativeCodeRedemptionService {
    var previewResponse: () -> Response<CooperativeCodePreviewDto> = { Response.success(previewDto()) }
    var response: () -> Response<SubscriptionDto> = { Response.success(201, subscriptionDto()) }
    val previews = mutableListOf<PreviewCooperativeCodeRequestDto>()
    val requests = mutableListOf<RedeemCooperativeCodeRequestDto>()

    override suspend fun preview(request: PreviewCooperativeCodeRequestDto): Response<CooperativeCodePreviewDto> {
        previews += request
        return previewResponse()
    }

    override suspend fun redeem(request: RedeemCooperativeCodeRequestDto): Response<SubscriptionDto> {
        requests += request
        return response()
    }
}

private fun previewDto() = CooperativeCodePreviewDto(
    organizationName = "Coop. La Yarada",
    managerName = "Rubén Ticona",
    quotaHa = 5.0,
    planEndsAt = "2027-12-31T05:00:00Z",
    registeredAreaHa = 8.0,
    exceedsQuota = true,
    consentTextVersion = "2026-11-v1",
)

private fun subscriptionDto() = SubscriptionDto(
    id = "s1",
    mode = "PLAN_COOPERATIVO_PATROCINADO",
    status = "ACTIVE",
    quotaHa = 12.0,
    endsAt = "2027-12-31T05:00:00Z",
    cooperativeName = "Coop. Olivarera La Yarada",
)

private fun <T> problem(status: Int, extra: String = "") =
    Response.error<T>(status, "{\"status\":$status$extra}".toResponseBody("application/problem+json".toMediaType()))

@OptIn(ExperimentalCoroutinesApi::class)
class CooperativeCodeRepositoryImplTest {

    private val service = FakeRedemptionService()
    private val repository = CooperativeCodeRepositoryImpl(
        service = service,
        apiCaller = ApiCaller(ApiErrorMapper(Json), UnconfinedTestDispatcher()),
    )
    private val code = checkNotNull(InvitationCode.of("coop-7k2q"))

    @Test
    fun `a preview says what the code offers, including a quota below the registered hectares`() = runTest {
        val outcome = repository.preview(code)

        assertEquals(
            CodePreviewOutcome.Previewed(
                CodePreview(
                    organizationName = "Coop. La Yarada",
                    managerName = "Rubén Ticona",
                    quotaHectares = 5.0,
                    planEndsAt = Instant.parse("2027-12-31T05:00:00Z"),
                    registeredHectares = 8.0,
                    exceedsQuota = true,
                    consentTextVersion = "2026-11-v1",
                ),
            ),
            outcome,
        )
        assertEquals(listOf(PreviewCooperativeCodeRequestDto("COOP-7K2Q")), service.previews)
    }

    @Test
    fun `a license without a manager previews without a name`() = runTest {
        service.previewResponse = { Response.success(previewDto().copy(managerName = null)) }

        val preview = (repository.preview(code) as CodePreviewOutcome.Previewed).preview

        assertEquals(null, preview.managerName)
    }

    @Test
    fun `the preview failures say why the code cannot be used`() = runTest {
        val cases = mapOf(
            400 to CodePreviewOutcome.NotRedeemable,
            409 to CodePreviewOutcome.AlreadySubscribed,
            429 to CodePreviewOutcome.TooManyAttempts,
        )
        cases.forEach { (status, expected) ->
            service.previewResponse = { problem(status) }
            assertEquals(expected, repository.preview(code))
        }
    }

    @Test
    fun `a redeemed code sends the accepted consent text and returns the cooperative, its hectares and its end`() = runTest {
        val outcome = repository.redeem(code, "2026-11-v1")

        assertEquals(
            RedemptionOutcome.Redeemed(
                CooperativeMembership("Coop. Olivarera La Yarada", 12.0, Instant.parse("2027-12-31T05:00:00Z")),
            ),
            outcome,
        )
        assertEquals(listOf(RedeemCooperativeCodeRequestDto("COOP-7K2Q", "2026-11-v1")), service.requests)
    }

    @Test
    fun `a 400 means the code cannot be redeemed`() = runTest {
        service.response = { problem(400) }

        assertEquals(RedemptionOutcome.NotRedeemable, repository.redeem(code, "v1"))
    }

    @Test
    fun `a 409 means the producer already has a subscription`() = runTest {
        service.response = { problem(409, ",\"reason\":\"subscription.producer.already_active\"") }

        assertEquals(RedemptionOutcome.AlreadySubscribed, repository.redeem(code, "v1"))
    }

    @Test
    fun `a 409 about the consent text asks to accept the new one`() = runTest {
        service.response = { problem(409, ",\"reason\":\"subscription.consent.text_outdated\"") }

        assertEquals(RedemptionOutcome.ConsentOutdated, repository.redeem(code, "v1"))
    }

    @Test
    fun `a 429 means too many attempts`() = runTest {
        service.response = { problem(429) }

        assertEquals(RedemptionOutcome.TooManyAttempts, repository.redeem(code, "v1"))
    }

    @Test
    fun `any other failure is kept as it came`() = runTest {
        service.response = { throw IOException("no route") }

        assertEquals(RedemptionOutcome.Failed(AppError.Offline), repository.redeem(code, "v1"))
    }
}
