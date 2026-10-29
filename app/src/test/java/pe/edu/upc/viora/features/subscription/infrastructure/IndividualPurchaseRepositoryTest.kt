package pe.edu.upc.viora.features.subscription.infrastructure

import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.*
import okhttp3.MediaType.Companion.toMediaType
import org.junit.*
import org.junit.Assert.*
import pe.edu.upc.viora.core.datastore.*
import pe.edu.upc.viora.core.domain.*
import pe.edu.upc.viora.core.network.*
import pe.edu.upc.viora.features.iam.InMemoryPreferences
import pe.edu.upc.viora.features.subscription.domain.valueobject.*
import pe.edu.upc.viora.features.subscription.infrastructure.local.*
import pe.edu.upc.viora.features.subscription.infrastructure.remote.*
import pe.edu.upc.viora.features.subscription.infrastructure.repository.IndividualPurchaseRepositoryImpl
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

private class PurchaseJournalFake : IndividualPurchaseDao {
    override suspend fun wipeAll() = Unit

    val rows = mutableMapOf<String, IndividualPurchaseEntity>()
    var failInsert = false
    override suspend fun find(owner: String) = rows[owner]
    override suspend fun insert(row: IndividualPurchaseEntity) {
        check(!failInsert)
        rows.putIfAbsent(row.owner, row)
    }
    override suspend fun update(row: IndividualPurchaseEntity): Int {
        if (!rows.containsKey(row.owner)) return 0
        rows[row.owner] = row
        return 1
    }
    override suspend fun delete(owner: String) {
        rows.remove(owner)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class IndividualPurchaseRepositoryTest {
    private val server = MockWebServer()
    private val journal = PurchaseJournalFake()
    private val sessions = SessionStore(InMemoryPreferences(), TokenCipher.PLAIN)
    private val json = Json { ignoreUnknownKeys = true }
    private val id = UUID.randomUUID().toString()
    private val intent = UUID.randomUUID().toString()
    private val plan = ProducerPlanCode("PRODUCER_ANNUAL")
    private val capacity = PlanCapacity.of(BigDecimal("0.5"))
    private val now = Instant.parse("2026-10-22T12:00:00Z")
    private val contract get() = """{"id":"$id","mode":"PLAN_PRODUCTOR_INDIVIDUAL","planCode":"PRODUCER_ANNUAL",
        "status":"PENDING_PAYMENT","quotaHa":0.5,"currency":"PEN","baseAmount":279.66,"taxAmount":50.34,
        "totalAmount":330.00,"startsAt":null,"endsAt":null,"entitlementActive":false,"cooperativeId":null}"""
    private val checkout get() = """{"intentId":"$intent","checkoutUrl":"https://sandbox.mercadopago.com.pe/checkout/v1/redirect?pref=test",
        "expiresAt":"2026-10-23T12:00:00Z"}"""

    @Before fun setUp() = runBlocking {
        server.start()
        sessions.save(StoredSession("test-access", "test-refresh", Long.MAX_VALUE, "owner-a"))
    }
    @After fun tearDown() { server.shutdown() }

    private fun repository(at: Instant = now): IndividualPurchaseRepositoryImpl {
        val service = Retrofit.Builder().baseUrl(server.url("/api/v1/"))
            .client(OkHttpClient.Builder().readTimeout(500, TimeUnit.MILLISECONDS).build())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType())).build()
            .create(IndividualPurchaseService::class.java)
        return IndividualPurchaseRepositoryImpl(service, journal, ApiCaller(ApiErrorMapper(json), UnconfinedTestDispatcher()),
            sessions, json, Clock.fixed(at, ZoneOffset.UTC))
    }

    @Test fun `create writes a stable key and sends only selection with exact decimals`() = runTest {
        server.enqueue(MockResponse().setResponseCode(201).setBody(contract))
        val result = repository().prepare(plan, capacity) as AppResult.Success
        assertEquals(BigDecimal("330.00"), result.value.total)
        val request = server.takeRequest()
        assertEquals("/api/v1/subscriptions", request.path)
        assertEquals(journal.rows.getValue("owner-a").createKey, request.getHeader("Idempotency-Key"))
        assertEquals("""{"planCode":"PRODUCER_ANNUAL","requestedQuotaHa":0.5}""", request.body.readUtf8())
        assertNotEquals(journal.rows.getValue("owner-a").createKey, journal.rows.getValue("owner-a").checkoutKey)
    }

    @Test fun `fresh journal recovers owned pending purchase after creation conflict`() = runTest {
        server.enqueue(MockResponse().setResponseCode(409))
        server.enqueue(MockResponse().setResponseCode(200).setBody(contract))
        val recovered = repository().prepare(plan, capacity) as AppResult.Success
        assertEquals(id, recovered.value.id)
        assertEquals("POST", server.takeRequest().method)
        val current = server.takeRequest()
        assertEquals("GET", current.method)
        assertEquals("/api/v1/subscriptions", current.path)
        assertTrue(journal.rows.getValue("owner-a").contractJson!!.contains(id))
        assertEquals(2, server.requestCount)
    }

    @Test fun `missing historical contract adopts existing current contract without creating purchase`() = runTest {
        server.enqueue(MockResponse().setResponseCode(201).setBody(contract))
        repository().prepare(plan, capacity)
        server.takeRequest()
        val replacement = UUID.randomUUID().toString()
        server.enqueue(MockResponse().setResponseCode(404))
        server.enqueue(MockResponse().setResponseCode(200).setBody(contract.replace(id, replacement)))
        val recovered = repository().resume() as AppResult.Success
        assertEquals(replacement, recovered.value!!.id)
        assertEquals("GET", server.takeRequest().method)
        val current = server.takeRequest()
        assertEquals("GET", current.method)
        assertEquals("/api/v1/subscriptions", current.path)
        assertTrue(journal.rows.getValue("owner-a").contractJson!!.contains(replacement))
    }

    @Test fun `a lost creation response replays the persisted key after repository recreation`() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST))
        assertTrue(repository().prepare(plan, capacity) is AppResult.Failure)
        val first = server.takeRequest()
        assertNull(journal.rows.getValue("owner-a").contractJson)
        server.enqueue(MockResponse().setResponseCode(200).setBody(contract))
        assertTrue(repository().resume() is AppResult.Success)
        assertEquals(first.getHeader("Idempotency-Key"), server.takeRequest().getHeader("Idempotency-Key"))
    }

    @Test fun `journal failure prevents sending a purchase and missing session sends nothing`() = runTest {
        journal.failInsert = true
        assertTrue(repository().prepare(plan, capacity) is AppResult.Failure)
        assertEquals(0, server.requestCount)
        sessions.clear()
        assertEquals(AppResult.Failure(AppError.Unauthorized), repository().prepare(plan, capacity))
        assertEquals(0, server.requestCount)
    }

    @Test fun `confirmed contract is refreshed by get`() = runTest {
        server.enqueue(MockResponse().setBody(contract))
        repository().prepare(plan, capacity)
        server.takeRequest()
        server.enqueue(MockResponse().setBody(contract))
        assertTrue(repository().resume() is AppResult.Success)
        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/v1/subscriptions/$id", request.path)
    }

    private val twoHectares get() = contract.replace("\"quotaHa\":0.5", "\"quotaHa\":2")
        .replace("279.66", "1118.64").replace("50.34", "201.36").replace("330.00", "1320.00")

    @Test fun `a changed selection asks the server to move the unpaid purchase under a new key`() = runTest {
        server.enqueue(MockResponse().setBody(contract))
        repository().prepare(plan, capacity)
        val first = server.takeRequest()
        server.enqueue(MockResponse().setResponseCode(201).setBody(twoHectares))
        val moved = repository().prepare(plan, PlanCapacity.of(BigDecimal("2"))) as AppResult.Success
        assertEquals(id, moved.value.id)
        assertEquals(BigDecimal("1320.00"), moved.value.total)
        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("""{"planCode":"PRODUCER_ANNUAL","requestedQuotaHa":2}""", request.body.readUtf8())
        assertNotEquals(first.getHeader("Idempotency-Key"), request.getHeader("Idempotency-Key"))
        assertEquals("2", journal.rows.getValue("owner-a").quota)
        assertTrue(journal.rows.getValue("owner-a").contractJson!!.contains("1320"))
    }

    @Test fun `a payment under way keeps the journal of the purchase it pays`() = runTest {
        server.enqueue(MockResponse().setBody(contract))
        repository().prepare(plan, capacity)
        server.takeRequest()
        val before = journal.rows.getValue("owner-a")
        server.enqueue(MockResponse().setResponseCode(409)
            .setBody("""{"code":"SUBSCRIPTION_CONFLICT","reason":"subscription.payment.unresolved"}"""))
        assertTrue(repository().prepare(plan, PlanCapacity.of(BigDecimal("2"))) is AppResult.Failure)
        assertEquals(2, server.requestCount)
        assertEquals(before, journal.rows.getValue("owner-a"))
    }

    @Test fun `checkout failure preserves its distinct key and recovers after recreation`() = runTest {
        server.enqueue(MockResponse().setBody(contract))
        repository().prepare(plan, capacity)
        server.takeRequest()
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST))
        assertTrue(repository().checkout(id) is AppResult.Failure)
        val first = server.takeRequest()
        server.enqueue(MockResponse().setBody(checkout))
        assertTrue(repository().checkout(id) is AppResult.Success)
        val second = server.takeRequest()
        assertEquals("/api/v1/subscriptions/$id/checkouts", second.path)
        assertEquals(first.getHeader("Idempotency-Key"), second.getHeader("Idempotency-Key"))
        assertNotEquals(journal.rows.getValue("owner-a").createKey, second.getHeader("Idempotency-Key"))
        assertEquals(intent, journal.rows.getValue("owner-a").intentId)
    }

    @Test fun `unknown provider urls are rejected before persistence`() = runTest {
        server.enqueue(MockResponse().setBody(contract))
        repository().prepare(plan, capacity)
        server.enqueue(MockResponse().setBody(checkout.replace("sandbox.mercadopago.com.pe", "evil.example")))
        assertTrue(repository().checkout(id) is AppResult.Failure)
        assertNull(journal.rows.getValue("owner-a").checkoutUrl)
    }

    @Test fun `expiry requests server reconciliation and unresolved evidence retains the new key`() = runTest {
        server.enqueue(MockResponse().setBody(contract))
        repository().prepare(plan, capacity)
        server.takeRequest()
        server.enqueue(MockResponse().setBody(checkout))
        repository().checkout(id)
        val original = server.takeRequest().getHeader("Idempotency-Key")
        val later = repository(now.plusSeconds(86_401))
        server.enqueue(MockResponse().setResponseCode(409).setBody("""{"code":"SUBSCRIPTION_CONFLICT","detail":"pending"}"""))
        assertTrue(later.checkout(id) is AppResult.Failure)
        val rotated = server.takeRequest().getHeader("Idempotency-Key")
        assertNotEquals(original, rotated)
        server.enqueue(MockResponse().setResponseCode(503))
        assertTrue(repository(now.plusSeconds(86_402)).checkout(id) is AppResult.Failure)
        assertEquals(rotated, server.takeRequest().getHeader("Idempotency-Key"))
    }

    @Test fun `an account change during a request cannot persist or expose its response`() = runTest {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                runBlocking { sessions.save(StoredSession("other-access", "other-refresh", Long.MAX_VALUE, "owner-b")) }
                return MockResponse().setBody(contract)
            }
        }
        assertEquals(AppResult.Failure(AppError.Unauthorized), repository().prepare(plan, capacity))
        assertNull(journal.rows.getValue("owner-a").contractJson)
        assertFalse(journal.rows.containsKey("owner-b"))
    }

    @Test fun `server errors preserve journal identity and amounts must balance`() = runTest {
        server.enqueue(MockResponse().setResponseCode(503))
        assertTrue(repository().prepare(plan, capacity) is AppResult.Failure)
        val original = journal.rows.getValue("owner-a")
        server.enqueue(MockResponse().setBody(contract.replace("330.00", "331.00")))
        assertTrue(repository().resume() is AppResult.Failure)
        assertEquals(original, journal.rows.getValue("owner-a"))
    }

    @Test fun `a lost checkout response can recover after expiry using an explicit server reason`() = runTest {
        server.enqueue(MockResponse().setBody(contract))
        repository().prepare(plan, capacity)
        server.takeRequest()
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST))
        repository().checkout(id)
        val original = server.takeRequest().getHeader("Idempotency-Key")
        assertNull(journal.rows.getValue("owner-a").expiresAt)
        server.enqueue(MockResponse().setResponseCode(409).setBody("""{"code":"SUBSCRIPTION_CONFLICT",
            "detail":"Localized human message","reason":"subscription.checkout.expired"}"""))
        server.enqueue(MockResponse().setBody(checkout))
        assertTrue(repository().checkout(id) is AppResult.Success)
        assertEquals(original, server.takeRequest().getHeader("Idempotency-Key"))
        assertNotEquals(original, server.takeRequest().getHeader("Idempotency-Key"))
    }

    @Test fun `unresolved and ambiguous conflicts never rotate a checkout key`() = runTest {
        server.enqueue(MockResponse().setBody(contract))
        repository().prepare(plan, capacity)
        server.takeRequest()
        val original = journal.rows.getValue("owner-a").checkoutKey
        listOf("""{"code":"SUBSCRIPTION_CONFLICT","reason":"subscription.payment.unresolved"}""",
            """{"code":"SUBSCRIPTION_CONFLICT","detail":"subscription.checkout.expired"}""").forEach { body ->
            server.enqueue(MockResponse().setResponseCode(409).setBody(body))
            assertTrue(repository().checkout(id) is AppResult.Failure)
            assertEquals(original, server.takeRequest().getHeader("Idempotency-Key"))
            assertEquals(original, journal.rows.getValue("owner-a").checkoutKey)
        }
    }
}
