package pe.edu.upc.viora.features.phenology.infrastructure

import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.viora.core.database.CacheMetadataDao
import pe.edu.upc.viora.core.database.CacheMetadataEntity
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.network.ApiCaller
import pe.edu.upc.viora.core.network.ApiErrorMapper
import pe.edu.upc.viora.features.phenology.REAL_CHILL_METRIC_JSON
import pe.edu.upc.viora.features.phenology.chillEntity
import pe.edu.upc.viora.features.phenology.domain.entity.WinterSeasonState
import pe.edu.upc.viora.features.phenology.infrastructure.local.ChillTrackerDao
import pe.edu.upc.viora.features.phenology.infrastructure.local.ChillTrackerEntity
import pe.edu.upc.viora.features.phenology.infrastructure.remote.HarvestRecordDto
import pe.edu.upc.viora.features.phenology.infrastructure.remote.MetricDetailsDto
import pe.edu.upc.viora.features.phenology.infrastructure.remote.MetricDto
import pe.edu.upc.viora.features.phenology.infrastructure.remote.PhenologyService
import pe.edu.upc.viora.features.phenology.infrastructure.remote.RectifyHarvestYieldRequestDto
import pe.edu.upc.viora.features.phenology.infrastructure.remote.RecordHarvestYieldRequestDto
import pe.edu.upc.viora.features.phenology.infrastructure.repository.ChillRepositoryImpl
import retrofit2.Response

private class FakeChillTrackerDao : ChillTrackerDao {
    val row = MutableStateFlow<ChillTrackerEntity?>(null)

    override fun observeByPlot(plotId: String): Flow<ChillTrackerEntity?> =
        row.map { it?.takeIf { entity -> entity.plotId == plotId } }

    override suspend fun upsert(entity: ChillTrackerEntity) {
        row.value = entity
    }

    override suspend fun deleteByPlot(plotId: String) {
        if (row.value?.plotId == plotId) row.value = null
    }
}

private class FakeChillCacheMetadataDao : CacheMetadataDao {
    val values = MutableStateFlow<Map<String, Long>>(emptyMap())

    override suspend fun fetchedAt(resourceKey: String): Long? = values.value[resourceKey]

    override fun observeFetchedAt(resourceKey: String): Flow<Long?> =
        values.map { it[resourceKey] }

    override suspend fun upsert(entity: CacheMetadataEntity) {
        values.value = values.value + (entity.resourceKey to entity.fetchedAtEpochMs)
    }

    override suspend fun delete(resourceKey: String) {
        values.value = values.value - resourceKey
    }
}

private class FakeChillPhenologyService : PhenologyService {
    var metricsResponse: () -> Response<List<MetricDto>> = { Response.success(emptyList()) }

    override suspend fun getRecords(plotId: String): Response<List<HarvestRecordDto>> =
        Response.success(emptyList())

    override suspend fun recordYield(
        plotId: String,
        request: RecordHarvestYieldRequestDto,
    ): Response<HarvestRecordDto> = throw UnsupportedOperationException()

    override suspend fun rectifyYield(
        plotId: String,
        recordId: String,
        request: RectifyHarvestYieldRequestDto,
    ): Response<HarvestRecordDto> = throw UnsupportedOperationException()

    override suspend fun removeRecord(plotId: String, recordId: String): Response<Unit> =
        Response.success(Unit)

    override suspend fun getMetrics(plotId: String, metricName: String): Response<List<MetricDto>> =
        metricsResponse()
}

@OptIn(ExperimentalCoroutinesApi::class)
class ChillRepositoryImplTest {

    private val fakeDao = FakeChillTrackerDao()
    private val fakeCacheDao = FakeChillCacheMetadataDao()
    private val fakeService = FakeChillPhenologyService()
    private val testClock = Clock.fixed(Instant.parse("2026-10-07T12:00:00Z"), ZoneOffset.UTC)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val json = Json { ignoreUnknownKeys = true }
    private val apiCaller = ApiCaller(ApiErrorMapper(json), testDispatcher)

    private val repository = ChillRepositoryImpl(
        service = fakeService,
        chillTrackerDao = fakeDao,
        cacheMetadataDao = fakeCacheDao,
        apiCaller = apiCaller,
        clock = testClock,
    )

    @Test
    fun observeChillTrackerEmitsMappedDomain() = runTest {
        assertNull(repository.observeChillTracker("plot-1").first())

        fakeDao.upsert(chillEntity(accumulated = 24.43))

        val tracker = repository.observeChillTracker("plot-1").first()
        assertNotNull(tracker)
        assertEquals(24.43, tracker!!.accumulatedPortions, 0.001)
        assertEquals(WinterSeasonState.ACCUMULATING, tracker.seasonState)
    }

    @Test
    fun refreshSuccessCachesTheRealBackendAnswer() = runTest {
        fakeService.metricsResponse = { Response.success(json.decodeFromString<List<MetricDto>>(REAL_CHILL_METRIC_JSON)) }

        val result = repository.refresh("plot-1")
        assertTrue(result is AppResult.Success)

        val cached = fakeDao.row.value
        assertNotNull(cached)
        assertEquals("plot-1", cached!!.plotId)
        assertEquals(3.03, cached.accumulatedPortions, 0.001)
        assertEquals("OFF_SEASON", cached.seasonState)
        assertEquals(testClock.millis(), fakeCacheDao.values.value["chill:plot-1"])
    }

    @Test
    fun refreshIgnoresOtherMetricsOfTheList() = runTest {
        fakeService.metricsResponse = {
            Response.success(listOf(MetricDto(metricName = "BIENNIAL_BEARING_INDEX", value = 0.3)))
        }
        fakeDao.upsert(chillEntity())

        val result = repository.refresh("plot-1")

        assertTrue(result is AppResult.Success)
        assertNull(fakeDao.row.value)
    }

    @Test
    fun refreshNotFoundClearsDao() = runTest {
        fakeDao.upsert(chillEntity())
        val notFoundBody = "{\"type\":\"about:blank\",\"status\":404}".toResponseBody("application/json".toMediaType())
        fakeService.metricsResponse = { Response.error(404, notFoundBody) }

        val result = repository.refresh("plot-1")
        assertTrue(result is AppResult.Success)
        assertNull(fakeDao.row.value)
    }

    @Test
    fun refreshWithAMalformedMetricKeepsTheCache() = runTest {
        val initialEntity = chillEntity()
        fakeDao.upsert(initialEntity)
        fakeService.metricsResponse = {
            Response.success(listOf(MetricDto(metricName = "EREZ_CHILLING_PORTIONS", value = 28.5, details = MetricDetailsDto())))
        }

        val result = repository.refresh("plot-1")

        assertTrue(result is AppResult.Failure)
        assertEquals(initialEntity, fakeDao.row.value)
    }

    @Test
    fun refreshNetworkFailurePreservesCache() = runTest {
        val initialEntity = chillEntity()
        fakeDao.upsert(initialEntity)

        fakeService.metricsResponse = { throw IOException("Failed to connect to server") }

        val result = repository.refresh("plot-1")
        assertTrue(result is AppResult.Failure)
        assertTrue((result as AppResult.Failure).error is AppError.Offline)
        assertEquals(initialEntity, fakeDao.row.value)
    }
}
