package pe.edu.upc.viora.features.harvestsettlement.infrastructure

import java.time.LocalDate
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.network.ApiCaller
import pe.edu.upc.viora.core.network.ApiErrorMapper
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettleHarvestDraft
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettleOutcome
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettlementSummary
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.local.HarvestSettlementDao
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.local.HarvestSettlementEntity
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.remote.HarvestSettlementService
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.repository.HarvestSettlementRepositoryImpl
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

private class InMemorySettlementDao : HarvestSettlementDao {
    val rows = MutableStateFlow<List<HarvestSettlementEntity>>(emptyList())
    var failOnUpsert = false

    override fun observeAll(): Flow<List<HarvestSettlementEntity>> = rows

    override suspend fun findByPlotAndYear(plotId: String, campaignYear: Int): HarvestSettlementEntity? =
        rows.value.firstOrNull { it.plotId == plotId && it.campaignYear == campaignYear }

    override suspend fun upsertAll(entities: List<HarvestSettlementEntity>) {
        if (failOnUpsert) error("disk full")
        val byId = rows.value.associateBy { it.id }.toMutableMap()
        entities.forEach { byId[it.id] = it }
        rows.value = byId.values.toList()
    }

    override suspend fun deleteByPlot(plotId: String) {
        rows.value = rows.value.filter { it.plotId != plotId }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class HarvestSettlementSettleTest {

    private val server = MockWebServer()
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }
    private val dao = InMemorySettlementDao()
    private lateinit var repository: HarvestSettlementRepositoryImpl

    private val draft = SettleHarvestDraft(
        plotId = "p-1",
        campaignYear = 2026,
        greenOlivesKg = 1200.5,
        blackOlivesKg = 800.0,
        weighedOn = LocalDate.of(2026, 10, 3),
        millTicketNumber = "T-9",
    )

    @Before
    fun setUp() {
        server.start()
        val client = OkHttpClient.Builder().readTimeout(300, TimeUnit.MILLISECONDS).build()
        val service = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(HarvestSettlementService::class.java)
        repository = HarvestSettlementRepositoryImpl(
            service = service,
            dao = dao,
            apiCaller = ApiCaller(ApiErrorMapper(json), UnconfinedTestDispatcher()),
            json = json,
        )
    }

    @After
    fun tearDown() {
        runCatching { server.shutdown() }
    }

    private val settlementJson = """
        {"id":"s-1","reportId":"rep-1","plotId":"p-1","campaignYear":2026,"greenOlivesKg":1200.5,
        "blackOlivesKg":800.0,"totalYieldKg":2000.5,"status":"SETTLED","settledAt":"2026-10-03T10:00:00Z",
        "receiptNumber":"VR-26-0001","weighedOn":"2026-10-03","millTicketNumber":"T-9","commercialSizeGrade":"101/110"}
    """.trimIndent()

    private fun conflict(extra: String) = MockResponse().setResponseCode(409)
        .setHeader("Content-Type", "application/problem+json")
        .setBody("""{"status":409,"detail":"already settled","code":"HARVESTSETTLEMENT_CONFLICT"$extra}""")

    @Test
    fun `201 caches the settlement and returns the receipt fields`() = runTest {
        server.enqueue(MockResponse().setResponseCode(201).setBody(settlementJson))

        val result = repository.settle(draft, "key-1")

        val outcome = (result as AppResult.Success).value as SettleOutcome.Settled
        assertEquals("VR-26-0001", outcome.settlement.receiptNumber)
        assertEquals(LocalDate.of(2026, 10, 3), outcome.settlement.weighedOn)
        assertEquals("101/110", outcome.settlement.commercialSizeGrade)
        assertEquals(listOf("s-1"), repository.observeAll().first().map { it.id })
    }

    @Test
    fun `200 replay also succeeds`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(settlementJson))

        val result = repository.settle(draft, "key-1")

        assertTrue((result as AppResult.Success).value is SettleOutcome.Settled)
    }

    @Test
    fun `the request carries the idempotency key and the expected body`() = runTest {
        server.enqueue(MockResponse().setResponseCode(201).setBody(settlementJson))

        repository.settle(draft, "key-abc")

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/plots/p-1/harvest-settlements", request.path)
        assertEquals("key-abc", request.getHeader("Idempotency-Key"))
        assertEquals(
            """{"campaignYear":2026,"greenOlivesKg":1200.5,"blackOlivesKg":800.0,"weighedOn":"2026-10-03","millTicketNumber":"T-9"}""",
            request.body.readUtf8(),
        )
    }

    @Test
    fun `409 with existingSettlement returns the parsed summary and caches nothing`() = runTest {
        server.enqueue(
            conflict(
                ""","existingSettlement":{"campaignYear":2026,"totalYieldKg":1500.0,"receiptNumber":"VR-26-0003","weighedOn":"2026-10-01"}""",
            ),
        )

        val result = repository.settle(draft, "key-1")

        val outcome = (result as AppResult.Success).value as SettleOutcome.AlreadySettled
        assertEquals(
            SettlementSummary(2026, 1500.0, "VR-26-0003", LocalDate.of(2026, 10, 1)),
            outcome.existing,
        )
        assertTrue(repository.observeAll().first().isEmpty())
    }

    @Test
    fun `409 without the property still returns the outcome`() = runTest {
        server.enqueue(conflict(""))

        val result = repository.settle(draft, "key-1")

        assertEquals(AppResult.Success(SettleOutcome.AlreadySettled(null)), result)
    }

    @Test
    fun `409 without the property falls back to the cached settlement`() = runTest {
        server.enqueue(MockResponse().setResponseCode(201).setBody(settlementJson))
        repository.settle(draft, "key-1")
        server.enqueue(conflict(""))

        val result = repository.settle(draft, "key-2")

        val outcome = (result as AppResult.Success).value as SettleOutcome.AlreadySettled
        assertEquals(2026, outcome.existing!!.campaignYear)
        assertEquals(2000.5, outcome.existing!!.totalYieldKg, 0.0)
        assertEquals("VR-26-0001", outcome.existing!!.receiptNumber)
    }

    @Test
    fun `409 with a malformed existingSettlement does not fail`() = runTest {
        server.enqueue(conflict(""","existingSettlement":{"campaignYear":"x"}"""))

        val result = repository.settle(draft, "key-1")

        assertEquals(AppResult.Success(SettleOutcome.AlreadySettled(null)), result)
    }

    @Test
    fun `400 passes through as Validation`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(400)
                .setBody("""{"status":400,"detail":"weighedOn in the future","code":"VALIDATION_ERROR"}"""),
        )

        val error = (repository.settle(draft, "key-1") as AppResult.Failure).error

        assertEquals(AppError.Validation("weighedOn in the future", "VALIDATION_ERROR"), error)
        assertTrue(repository.observeAll().first().isEmpty())
    }

    @Test
    fun `an unreachable server passes through as Offline`() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        assertEquals(AppResult.Failure(AppError.Offline), repository.settle(draft, "key-1"))
    }

    @Test
    fun `a failing cache write is reported as Unknown`() = runTest {
        dao.failOnUpsert = true
        server.enqueue(MockResponse().setResponseCode(201).setBody(settlementJson))

        val error = (repository.settle(draft, "key-1") as AppResult.Failure).error

        assertTrue(error is AppError.Unknown)
    }
}
