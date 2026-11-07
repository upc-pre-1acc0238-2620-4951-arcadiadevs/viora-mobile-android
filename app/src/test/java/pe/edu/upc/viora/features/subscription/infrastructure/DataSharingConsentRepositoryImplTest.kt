package pe.edu.upc.viora.features.subscription.infrastructure

import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.network.ApiCaller
import pe.edu.upc.viora.core.network.ApiErrorMapper
import pe.edu.upc.viora.features.subscription.domain.entity.DataSharingConsent
import pe.edu.upc.viora.features.subscription.domain.entity.DataSharingStatus
import pe.edu.upc.viora.features.subscription.infrastructure.remote.DataSharingConsentDto
import pe.edu.upc.viora.features.subscription.infrastructure.remote.DataSharingConsentService
import pe.edu.upc.viora.features.subscription.infrastructure.repository.DataSharingConsentRepositoryImpl
import retrofit2.Response

private fun dto(id: String, status: String, withdrawnAt: String? = null) = DataSharingConsentDto(
    id = id,
    organizationName = "Coop. La Yarada",
    managerName = "Rubén Ticona",
    status = status,
    acceptedAt = "2026-10-21T12:00:00Z",
    withdrawnAt = withdrawnAt,
    planEndsAt = "2027-12-31T05:00:00Z",
)

private class FakeConsentService : DataSharingConsentService {
    var list: () -> Response<List<DataSharingConsentDto>> = { Response.success(emptyList()) }
    var withdrawal: () -> Response<DataSharingConsentDto> = {
        Response.success(dto("c1", "WITHDRAWN", withdrawnAt = "2026-11-07T14:00:00Z"))
    }
    val withdrawn = mutableListOf<String>()

    override suspend fun list() = list.invoke()

    override suspend fun withdraw(id: String): Response<DataSharingConsentDto> {
        withdrawn += id
        return withdrawal()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class DataSharingConsentRepositoryImplTest {

    private val service = FakeConsentService()
    private val repository = DataSharingConsentRepositoryImpl(service, ApiCaller(ApiErrorMapper(Json), UnconfinedTestDispatcher()))

    @Test
    fun `the latest consent is the first the server lists`() = runTest {
        service.list = { Response.success(listOf(dto("c2", "ACTIVE"), dto("c1", "ENDED"))) }

        val latest = (repository.latest() as AppResult.Success).value

        assertEquals(
            DataSharingConsent(
                id = "c2",
                organizationName = "Coop. La Yarada",
                managerName = "Rubén Ticona",
                status = DataSharingStatus.ACTIVE,
                acceptedAt = Instant.parse("2026-10-21T12:00:00Z"),
                withdrawnAt = null,
                planEndsAt = Instant.parse("2027-12-31T05:00:00Z"),
            ),
            latest,
        )
    }

    @Test
    fun `a producer who never accepted has no consent`() = runTest {
        assertEquals(AppResult.Success(null), repository.latest())
    }

    @Test
    fun `a status this app does not know is skipped`() = runTest {
        service.list = { Response.success(listOf(dto("c3", "PAUSED"), dto("c2", "WITHDRAWN"))) }

        assertEquals("c2", (repository.latest() as AppResult.Success).value?.id)
    }

    @Test
    fun `withdrawing answers the consent as it stands now`() = runTest {
        val result = repository.withdraw("c1")

        assertEquals(listOf("c1"), service.withdrawn)
        val consent = (result as AppResult.Success).value
        assertEquals(DataSharingStatus.WITHDRAWN, consent.status)
        assertEquals(Instant.parse("2026-11-07T14:00:00Z"), consent.withdrawnAt)
    }

    @Test
    fun `a consent already withdrawn is a conflict`() = runTest {
        service.withdrawal = {
            Response.error(409, "{\"status\":409}".toResponseBody("application/problem+json".toMediaType()))
        }

        val result = repository.withdraw("c1")

        assertTrue((result as AppResult.Failure).error is AppError.Conflict)
    }
}
