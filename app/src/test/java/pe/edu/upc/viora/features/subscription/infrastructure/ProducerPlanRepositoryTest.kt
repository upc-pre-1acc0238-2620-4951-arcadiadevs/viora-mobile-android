package pe.edu.upc.viora.features.subscription.infrastructure

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.core.database.CacheMetadataEntity
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.network.ApiCaller
import pe.edu.upc.viora.core.network.ApiErrorMapper
import pe.edu.upc.viora.features.subscription.infrastructure.local.*
import pe.edu.upc.viora.features.subscription.infrastructure.mapper.toDomain
import pe.edu.upc.viora.features.subscription.infrastructure.mapper.toEntity
import pe.edu.upc.viora.features.subscription.infrastructure.remote.ProducerPlanOfferDto
import pe.edu.upc.viora.features.subscription.infrastructure.remote.ProducerPlanService
import pe.edu.upc.viora.features.subscription.infrastructure.repository.ProducerPlanRepositoryImpl
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/** The fake publishes one immutable transaction result, matching Room's snapshot contract. */
private class FakePlanDao : ProducerPlanDao {
    override suspend fun wipeAll() = Unit

    val snapshot = MutableStateFlow<CachedProducerPlanCatalog?>(null)
    var failWith: Exception? = null
    override fun observeCatalog(key: String) = snapshot
    override suspend fun deleteOffers(key: String) { error("Use atomic replacement") }
    override suspend fun upsertOffers(offers: List<ProducerPlanEntity>) { error("Use atomic replacement") }
    override suspend fun stamp(key: String, fetchedAt: Long) { error("Use atomic replacement") }
    override suspend fun replaceCatalog(offers: List<ProducerPlanEntity>, fetchedAt: Long) {
        failWith?.let { throw it }
        snapshot.value = CachedProducerPlanCatalog(CacheMetadataEntity(PRODUCER_PLAN_CATALOG_KEY, fetchedAt), offers)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ProducerPlanRepositoryTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val server = MockWebServer()
    private val dao = FakePlanDao()
    private val now = Instant.parse("2026-10-21T12:00:00Z")
    private lateinit var repository: ProducerPlanRepositoryImpl

    @Before fun setUp() {
        server.start()
        val service = Retrofit.Builder().baseUrl(server.url("/api/v1/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType())).build()
            .create(ProducerPlanService::class.java)
        repository = ProducerPlanRepositoryImpl(service, dao,
            ApiCaller(ApiErrorMapper(json), UnconfinedTestDispatcher()), Clock.fixed(now, ZoneOffset.UTC))
    }
    @After fun tearDown() { server.shutdown() }

    @Test fun `a successful refresh observes the real route and stamped exact policy`() = runTest {
        server.enqueue(MockResponse().setBody(catalogFixture))
        assertEquals(AppResult.Success(Unit), repository.refresh())
        val catalog = repository.observeCatalog().first()
        assertEquals(now, catalog.fetchedAt)
        assertEquals("55", catalog.offers.single().monthlyRatePerHa.toPlainString())
        assertEquals("/api/v1/subscription-plans", server.takeRequest().path)
        assertEquals(catalog.offers.single(), catalog.offers.single().toEntity().toDomain())
    }

    @Test fun `a successful empty catalog removes an obsolete offer and retains its refresh time`() = runTest {
        server.enqueue(MockResponse().setBody(catalogFixture)); repository.refresh()
        server.enqueue(MockResponse().setBody("[]"))
        assertEquals(AppResult.Success(Unit), repository.refresh())
        assertTrue(repository.observeCatalog().first().offers.isEmpty())
        assertEquals(now, repository.observeCatalog().first().fetchedAt)
    }

    @Test fun `an unavailable server does not replace cached offers or their timestamp`() = runTest {
        server.enqueue(MockResponse().setBody(catalogFixture)); repository.refresh()
        val original = dao.snapshot.value
        server.enqueue(MockResponse().setResponseCode(503))
        assertTrue(repository.refresh() is AppResult.Failure)
        assertEquals(original, dao.snapshot.value)
    }

    @Test fun `an invalid catalog is validated before writing any row`() = runTest {
        server.enqueue(MockResponse().setBody(catalogFixture)); repository.refresh()
        val original = dao.snapshot.value
        server.enqueue(MockResponse().setBody(catalogFixture.replace(":0.5,", ":0,")))
        assertTrue(repository.refresh() is AppResult.Failure)
        assertEquals(original, dao.snapshot.value)
    }

    @Test fun `duplicate offer identities cannot corrupt the cache`() = runTest {
        server.enqueue(MockResponse().setBody("[" + catalogFixture.trim().removePrefix("[").removeSuffix("]") + "," +
            catalogFixture.trim().removePrefix("[").removeSuffix("]") + "]"))
        assertTrue(repository.refresh() is AppResult.Failure)
        assertNull(dao.snapshot.value)
    }

    @Test fun `missing required fields are reported without replacing cached data`() = runTest {
        server.enqueue(MockResponse().setBody("[{\"code\":\"incomplete\"}]"))
        assertTrue(repository.refresh() is AppResult.Failure)
        assertNull(dao.snapshot.value)
    }

    @Test fun `cancellation of a cache write is never swallowed`() = runTest {
        server.enqueue(MockResponse().setBody(catalogFixture))
        dao.failWith = CancellationException("cancelled")
        try {
            repository.refresh()
            fail("Expected cancellation")
        } catch (_: CancellationException) {
            assertNull(dao.snapshot.value)
        }
    }

    @Test fun `a local write failure remains an error and keeps the old snapshot`() = runTest {
        server.enqueue(MockResponse().setBody(catalogFixture)); repository.refresh()
        val original = dao.snapshot.value
        dao.failWith = IllegalStateException("disk unavailable")
        server.enqueue(MockResponse().setBody("[]"))
        val result = repository.refresh()
        assertTrue(result is AppResult.Failure && result.error is AppError.Unknown)
        assertEquals(original, dao.snapshot.value)
    }
}
