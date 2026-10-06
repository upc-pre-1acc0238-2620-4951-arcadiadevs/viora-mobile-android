package pe.edu.upc.viora.features.harvestsettlement.infrastructure

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.network.ApiCaller
import pe.edu.upc.viora.core.network.ApiErrorMapper
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.PendingSettlement
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.PendingStatus
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettleHarvestDraft
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettleOutcome
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettlementSummary
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettlementSyncResult
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.mapper.toDomain
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.mapper.toPendingEntity
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.remote.HarvestSettlementService
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.repository.HarvestSettlementRepositoryImpl
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@OptIn(ExperimentalCoroutinesApi::class)
class SettlementQueueTest {

    private val server = MockWebServer()
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }
    private val dao = InMemorySettlementDao()
    private val pendingDao = InMemoryPendingSettlementDao()
    private val scheduler = RecordingSyncScheduler()
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
            pendingDao = pendingDao,
            scheduler = scheduler,
            apiCaller = ApiCaller(ApiErrorMapper(json), UnconfinedTestDispatcher()),
            json = json,
            clock = Clock.fixed(Instant.parse("2026-10-03T12:00:00Z"), ZoneOffset.UTC),
        )
    }

    @After
    fun tearDown() {
        runCatching { server.shutdown() }
    }

    private val settlementJson = """
        {"id":"s-1","reportId":"rep-1","plotId":"p-1","campaignYear":2026,"greenOlivesKg":1200.5,
        "blackOlivesKg":800.0,"totalYieldKg":2000.5,"status":"SETTLED","settledAt":"2026-10-03T10:00:00Z",
        "receiptNumber":"VR-26-0001","weighedOn":"2026-10-03"}
    """.trimIndent()

    private fun offline() = MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START)

    /** Unlike [offline], the server still records the request, so its headers can be asserted. */
    private fun timeout() = MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE)

    private fun problem(status: Int, code: String, extra: String = "") = MockResponse().setResponseCode(status)
        .setHeader("Content-Type", "application/problem+json")
        .setBody("""{"status":$status,"detail":"d","code":"$code"$extra}""")

    private suspend fun queued(): PendingSettlement {
        server.enqueue(timeout())
        return ((repository.settle(draft) as AppResult.Success).value as SettleOutcome.Queued).pending
    }

    @Test
    fun `an offline settle stores a pending row with a key, schedules the sync and returns Queued`() = runTest {
        val pending = queued()

        assertEquals(draft, pending.draft)
        assertEquals(PendingStatus.PENDING, pending.status)
        assertTrue(pending.idempotencyKey.isNotBlank())
        assertEquals(1, scheduler.scheduled)
        assertEquals(listOf(pending), repository.observePending().first())
        assertTrue(repository.observeAll().first().isEmpty())
    }

    @Test
    fun `a timeout is queued as well`() = runTest {
        server.enqueue(timeout())

        val outcome = (repository.settle(draft) as AppResult.Success).value

        assertTrue(outcome is SettleOutcome.Queued)
        assertEquals(1, scheduler.scheduled)
    }

    @Test
    fun `the first request carries the key that is stored`() = runTest {
        val pending = queued()

        assertEquals(pending.idempotencyKey, server.takeRequest().getHeader("Idempotency-Key"))
    }

    @Test
    fun `a second offline settle for the same plot and year reuses the key`() = runTest {
        val first = queued()
        server.enqueue(offline())

        val second = ((repository.settle(draft.copy(greenOlivesKg = 10.0)) as AppResult.Success).value as SettleOutcome.Queued)
            .pending

        assertEquals(first.idempotencyKey, second.idempotencyKey)
        assertEquals(first.createdAt, second.createdAt)
        assertEquals(10.0, second.draft.greenOlivesKg, 0.0)
        assertEquals(1, repository.observePending().first().size)
    }

    @Test
    fun `an explicit key is used for the first attempt and stored`() = runTest {
        server.enqueue(timeout())

        val pending = ((repository.settle(draft, "given-key") as AppResult.Success).value as SettleOutcome.Queued).pending

        assertEquals("given-key", pending.idempotencyKey)
        assertEquals("given-key", server.takeRequest().getHeader("Idempotency-Key"))
    }

    @Test
    fun `different campaigns get independent rows and keys`() = runTest {
        val first = queued()
        server.enqueue(offline())
        val other = ((repository.settle(draft.copy(campaignYear = 2025)) as AppResult.Success).value as SettleOutcome.Queued).pending

        assertNotEquals(first.idempotencyKey, other.idempotencyKey)
        assertEquals(2, repository.observePending().first().size)
    }

    @Test
    fun `a validation error is returned and nothing is queued`() = runTest {
        server.enqueue(problem(400, "VALIDATION_ERROR"))

        val result = repository.settle(draft)

        assertTrue((result as AppResult.Failure).error is AppError.Validation)
        assertTrue(repository.observePending().first().isEmpty())
        assertEquals(0, scheduler.scheduled)
    }

    @Test
    fun `an online success removes a pending row of the same plot and year`() = runTest {
        queued()
        server.enqueue(MockResponse().setResponseCode(201).setBody(settlementJson))

        val outcome = (repository.settle(draft) as AppResult.Success).value

        assertTrue(outcome is SettleOutcome.Settled)
        assertTrue(repository.observePending().first().isEmpty())
    }

    @Test
    fun `sync Settled deletes the row, caches the settlement and sends the stored key`() = runTest {
        val pending = queued()
        server.takeRequest()
        server.enqueue(MockResponse().setResponseCode(201).setBody(settlementJson))

        val result = repository.syncPending()

        assertEquals(SettlementSyncResult.DONE, result)
        assertTrue(repository.observePending().first().isEmpty())
        assertEquals(listOf("s-1"), repository.observeAll().first().map { it.id })
        assertEquals(pending.idempotencyKey, server.takeRequest().getHeader("Idempotency-Key"))
    }

    @Test
    fun `sync AlreadySettled keeps a CONFLICT row with the server summary and does not retry it`() = runTest {
        queued()
        server.enqueue(
            problem(
                409,
                "HARVESTSETTLEMENT_CONFLICT",
                ""","existingSettlement":{"campaignYear":2026,"totalYieldKg":1500.0,"receiptNumber":"VR-26-0003","weighedOn":"2026-10-01"}""",
            ),
        )

        assertEquals(SettlementSyncResult.DONE, repository.syncPending())

        val row = repository.observePending().first().single()
        assertEquals(PendingStatus.CONFLICT, row.status)
        assertEquals(SettlementSummary(2026, 1500.0, "VR-26-0003", LocalDate.of(2026, 10, 1)), row.existing)
        val requestsBefore = server.requestCount
        assertEquals(SettlementSyncResult.DONE, repository.syncPending())
        assertEquals(requestsBefore, server.requestCount)
    }

    @Test
    fun `sync offline asks for a retry and keeps the row with the same key`() = runTest {
        val pending = queued()
        server.enqueue(offline())

        assertEquals(SettlementSyncResult.RETRY_LATER, repository.syncPending())

        val row = repository.observePending().first().single()
        assertEquals(PendingStatus.PENDING, row.status)
        assertEquals("OFFLINE", row.lastErrorCode)
        assertEquals(pending.idempotencyKey, row.idempotencyKey)
    }

    @Test
    fun `sync retries with the same key on every attempt`() = runTest {
        val pending = queued()
        server.enqueue(timeout())
        server.enqueue(problem(503, "UNAVAILABLE"))
        server.enqueue(MockResponse().setResponseCode(201).setBody(settlementJson))

        val results = List(3) { repository.syncPending() }

        assertEquals(
            listOf(SettlementSyncResult.RETRY_LATER, SettlementSyncResult.RETRY_LATER, SettlementSyncResult.DONE),
            results,
        )
        val keys = List(4) { server.takeRequest().getHeader("Idempotency-Key") }
        assertEquals(List(4) { pending.idempotencyKey }, keys)
        assertTrue(repository.observePending().first().isEmpty())
    }

    @Test
    fun `sync 5xx keeps the row as PENDING and counts the attempt`() = runTest {
        queued()
        server.enqueue(problem(500, "INTERNAL"))

        assertEquals(SettlementSyncResult.RETRY_LATER, repository.syncPending())

        val entity = pendingDao.rows.value.single()
        assertEquals("PENDING", entity.status)
        assertEquals(1, entity.attemptCount)
        assertEquals("SERVER_500", entity.lastErrorCode)
    }

    @Test
    fun `sync 400 marks the row FAILED with the code and stops retrying it`() = runTest {
        queued()
        server.enqueue(problem(400, "VALIDATION_ERROR"))

        assertEquals(SettlementSyncResult.DONE, repository.syncPending())

        val row = repository.observePending().first().single()
        assertEquals(PendingStatus.FAILED, row.status)
        assertEquals("VALIDATION_ERROR", row.lastErrorCode)
        val requestsBefore = server.requestCount
        repository.syncPending()
        assertEquals(requestsBefore, server.requestCount)
    }

    @Test
    fun `sync 403, 404 and 422 are permanent failures`() = runTest {
        listOf(403, 404, 422).forEachIndexed { index, status ->
            pendingDao.upsert(draft.copy(campaignYear = 2000 + index).toPendingEntity("k-$status", 1L, 1L))
            server.enqueue(problem(status, "CODE_$status"))
        }

        assertEquals(SettlementSyncResult.DONE, repository.syncPending())

        assertEquals(List(3) { "FAILED" }, pendingDao.rows.value.map { it.status })
    }

    @Test
    fun `a permanent failure does not stop the other rows`() = runTest {
        pendingDao.upsert(draft.copy(campaignYear = 2025).toPendingEntity("k-a", 1L, 1L))
        pendingDao.upsert(draft.toPendingEntity("k-b", 2L, 2L))
        server.enqueue(problem(400, "VALIDATION_ERROR"))
        server.enqueue(MockResponse().setResponseCode(201).setBody(settlementJson))

        assertEquals(SettlementSyncResult.DONE, repository.syncPending())

        assertEquals(listOf("k-a" to "FAILED"), pendingDao.rows.value.map { it.idempotencyKey to it.status })
    }

    @Test
    fun `an edit made while a pass is in flight is not overwritten by its stale result`() = runTest {
        val pending = queued()
        pendingDao.afterFindByStatus = {
            val row = pendingDao.rows.value.single()
            pendingDao.upsert(row.copy(updatedAt = row.updatedAt + 1, greenOlivesKg = 5.0))
        }
        server.enqueue(offline())

        repository.syncPending()

        assertEquals(5.0, pendingDao.rows.value.single().greenOlivesKg, 0.0)
        assertEquals(0, pendingDao.rows.value.single().attemptCount)
        assertEquals(pending.idempotencyKey, pendingDao.rows.value.single().idempotencyKey)
    }

    @Test
    fun `updatePending keeps the key, resets a FAILED row to PENDING and schedules the sync`() = runTest {
        val pending = queued()
        server.enqueue(problem(400, "VALIDATION_ERROR"))
        repository.syncPending()
        val before = scheduler.scheduled

        val result = repository.updatePending(draft.copy(greenOlivesKg = 99.0, weighedOn = LocalDate.of(2026, 10, 2)))

        assertEquals(AppResult.Success(Unit), result)
        val row = repository.observePending().first().single()
        assertEquals(pending.idempotencyKey, row.idempotencyKey)
        assertEquals(PendingStatus.PENDING, row.status)
        assertNull(row.lastErrorCode)
        assertEquals(99.0, row.draft.greenOlivesKg, 0.0)
        assertEquals(LocalDate.of(2026, 10, 2), row.draft.weighedOn)
        assertEquals(before + 1, scheduler.scheduled)
    }

    @Test
    fun `updatePending fails with NotFound once the row is gone`() = runTest {
        val result = repository.updatePending(draft)

        assertTrue((result as AppResult.Failure).error is AppError.NotFound)
        assertTrue(repository.observePending().first().isEmpty())
    }

    @Test
    fun `updatePending after the sync settled it fails and does not recreate the row`() = runTest {
        queued()
        server.enqueue(MockResponse().setResponseCode(201).setBody(settlementJson))
        repository.syncPending()

        val result = repository.updatePending(draft)

        assertTrue(result is AppResult.Failure)
        assertTrue(repository.observePending().first().isEmpty())
    }

    @Test
    fun `discardPending removes the row`() = runTest {
        queued()

        assertEquals(AppResult.Success(Unit), repository.discardPending("p-1", 2026))

        assertTrue(repository.observePending().first().isEmpty())
    }

    @Test
    fun `a pending row maps back to the same draft`() {
        val entity = draft.copy(commercialFruitsPerKg = 105.0, notes = "ok").toPendingEntity("k", 1L, 2L)

        val pending = entity.toDomain()

        assertEquals(draft.copy(commercialFruitsPerKg = 105.0, notes = "ok"), pending.draft)
        assertEquals("k", pending.idempotencyKey)
        assertEquals(1L, pending.createdAt)
        assertEquals(2L, pending.updatedAt)
        assertNull(pending.existing)
    }
}
