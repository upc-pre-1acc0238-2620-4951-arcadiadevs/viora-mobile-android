package pe.edu.upc.viora.features.subscription.infrastructure

import java.math.BigDecimal
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.network.ApiCaller
import pe.edu.upc.viora.core.network.ApiErrorMapper
import pe.edu.upc.viora.features.subscription.domain.valueobject.SubscriptionMode
import pe.edu.upc.viora.features.subscription.domain.valueobject.SubscriptionStatus
import pe.edu.upc.viora.features.subscription.infrastructure.remote.ProducerSubscriptionService
import pe.edu.upc.viora.features.subscription.infrastructure.repository.ProducerSubscriptionRepositoryImpl
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@OptIn(ExperimentalCoroutinesApi::class)
class ProducerSubscriptionRepositoryTest {

    private val server = MockWebServer()
    private val json = Json { ignoreUnknownKeys = true }
    private val subId = UUID.randomUUID().toString()

    private val activeJson get() = """{
        "id": "$subId",
        "mode": "INDIVIDUAL",
        "planCode": "PRODUCER_ANNUAL",
        "status": "ACTIVE",
        "quotaHa": 0.5,
        "currency": "PEN",
        "baseAmount": 279.66,
        "taxAmount": 50.34,
        "totalAmount": 330.00,
        "startsAt": "2026-10-22T00:00:00Z",
        "endsAt": "2027-10-22T00:00:00Z",
        "entitlementActive": true,
        "cooperativeId": null
    }"""

    private val pendingJson get() = """{
        "id": "$subId",
        "mode": "INDIVIDUAL",
        "planCode": "PRODUCER_ANNUAL",
        "status": "PENDING_PAYMENT",
        "quotaHa": 0.5,
        "currency": "PEN",
        "baseAmount": 279.66,
        "taxAmount": 50.34,
        "totalAmount": 330.00,
        "startsAt": null,
        "endsAt": null,
        "entitlementActive": false,
        "cooperativeId": null
    }"""

    @Before
    fun setUp() {
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun repository(): ProducerSubscriptionRepositoryImpl {
        val service = Retrofit.Builder()
            .baseUrl(server.url("/api/v1/"))
            .client(OkHttpClient.Builder().readTimeout(500, TimeUnit.MILLISECONDS).build())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ProducerSubscriptionService::class.java)

        return ProducerSubscriptionRepositoryImpl(
            service = service,
            caller = ApiCaller(ApiErrorMapper(json), UnconfinedTestDispatcher()),
        )
    }

    @Test
    fun `getCurrent returns subscription when active on 200 OK`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(activeJson))

        val result = repository().getCurrent()

        assertTrue(result is AppResult.Success)
        val subscription = (result as AppResult.Success).value
        assertNotNull(subscription)
        assertEquals(subId, subscription?.id)
        assertEquals(SubscriptionMode.INDIVIDUAL, subscription?.mode)
        assertEquals(SubscriptionStatus.ACTIVE, subscription?.status)
        assertTrue(subscription?.entitlementActive == true)
        assertEquals(BigDecimal("330.00"), subscription?.totalAmount)

        val recorded = server.takeRequest()
        assertEquals("/api/v1/subscriptions", recorded.path)
        assertEquals("GET", recorded.method)
    }

    @Test
    fun `getCurrent returns null when producer has no subscription on 204 No Content`() = runTest {
        server.enqueue(MockResponse().setResponseCode(204))

        val result = repository().getCurrent()

        assertTrue(result is AppResult.Success)
        val subscription = (result as AppResult.Success).value
        assertNull(subscription)

        val recorded = server.takeRequest()
        assertEquals("/api/v1/subscriptions", recorded.path)
        assertEquals("GET", recorded.method)
    }

    @Test
    fun `getCurrent returns Unauthorized on 401 response`() = runTest {
        server.enqueue(MockResponse().setResponseCode(401))

        val result = repository().getCurrent()

        assertTrue(result is AppResult.Failure)
        assertEquals(AppError.Unauthorized, (result as AppResult.Failure).error)
    }

    @Test
    fun `getById returns subscription on 200 OK`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(pendingJson))

        val result = repository().getById(subId)

        assertTrue(result is AppResult.Success)
        val subscription = (result as AppResult.Success).value
        assertEquals(subId, subscription.id)
        assertEquals(SubscriptionStatus.PENDING_PAYMENT, subscription.status)
        assertFalse(subscription.entitlementActive)

        val recorded = server.takeRequest()
        assertEquals("/api/v1/subscriptions/$subId", recorded.path)
        assertEquals("GET", recorded.method)
    }

    @Test
    fun `getById returns NotFound on 404 response`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404))

        val result = repository().getById(subId)

        assertTrue(result is AppResult.Failure)
        assertTrue((result as AppResult.Failure).error is AppError.NotFound)
    }
}
