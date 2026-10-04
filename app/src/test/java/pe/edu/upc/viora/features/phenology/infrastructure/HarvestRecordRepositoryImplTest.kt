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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.viora.core.database.CacheMetadataDao
import pe.edu.upc.viora.core.database.CacheMetadataEntity
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.network.ApiCaller
import pe.edu.upc.viora.core.network.ApiErrorMapper
import pe.edu.upc.viora.features.phenology.domain.entity.BearingYear
import pe.edu.upc.viora.features.phenology.infrastructure.local.BearingIndexDao
import pe.edu.upc.viora.features.phenology.infrastructure.local.BearingIndexEntity
import pe.edu.upc.viora.features.phenology.infrastructure.local.HarvestRecordDao
import pe.edu.upc.viora.features.phenology.infrastructure.local.HarvestRecordEntity
import pe.edu.upc.viora.features.phenology.infrastructure.mapper.toEntity
import pe.edu.upc.viora.features.phenology.infrastructure.remote.HarvestRecordDto
import pe.edu.upc.viora.features.phenology.infrastructure.remote.MetricDetailsDto
import pe.edu.upc.viora.features.phenology.infrastructure.remote.MetricDto
import pe.edu.upc.viora.features.phenology.infrastructure.remote.PhenologyService
import pe.edu.upc.viora.features.phenology.infrastructure.remote.RectifyHarvestYieldRequestDto
import pe.edu.upc.viora.features.phenology.infrastructure.remote.RecordHarvestYieldRequestDto
import pe.edu.upc.viora.features.phenology.infrastructure.repository.HarvestRecordRepositoryImpl
import retrofit2.Response

private class FakeHarvestRecordDao : HarvestRecordDao {
    val rows = MutableStateFlow<List<HarvestRecordEntity>>(emptyList())

    override fun observeByPlot(plotId: String): Flow<List<HarvestRecordEntity>> =
        rows.map { all -> all.filter { it.plotId == plotId }.sortedByDescending { it.campaignYear } }

    override suspend fun upsertAll(entities: List<HarvestRecordEntity>) {
        val byId = rows.value.associateBy { it.id }.toMutableMap()
        entities.forEach { byId[it.id] = it }
        rows.value = byId.values.toList()
    }

    override suspend fun deleteByPlot(plotId: String) {
        rows.value = rows.value.filter { it.plotId != plotId }
    }
}

private class FakeBearingIndexDao : BearingIndexDao {
    val rows = MutableStateFlow<Map<String, BearingIndexEntity>>(emptyMap())

    override fun observeByPlot(plotId: String): Flow<BearingIndexEntity?> = rows.map { it[plotId] }
    override suspend fun upsert(entity: BearingIndexEntity) {
        rows.value = rows.value + (entity.plotId to entity)
    }

    override suspend fun deleteByPlot(plotId: String) {
        rows.value = rows.value - plotId
    }
}

private class FakeCacheMetadataDao : CacheMetadataDao {
    val values = MutableStateFlow<Map<String, Long>>(emptyMap())

    override suspend fun fetchedAt(resourceKey: String): Long? = values.value[resourceKey]
    override fun observeFetchedAt(resourceKey: String): Flow<Long?> = values.map { it[resourceKey] }
    override suspend fun upsert(entity: CacheMetadataEntity) {
        values.value = values.value + (entity.resourceKey to entity.fetchedAtEpochMs)
    }

    override suspend fun delete(resourceKey: String) {
        values.value = values.value - resourceKey
    }
}

private class FakePhenologyService : PhenologyService {
    var records: () -> Response<List<HarvestRecordDto>> = { Response.success(emptyList()) }
    var metrics: () -> Response<List<MetricDto>> = { Response.success(emptyList()) }
    var recorded: () -> Response<HarvestRecordDto> = { throw IOException("not configured") }
    var rectified: () -> Response<HarvestRecordDto> = { throw IOException("not configured") }
    var removed: () -> Response<Unit> = { Response.success(204, Unit) }
    val recordRequests = mutableListOf<RecordHarvestYieldRequestDto>()
    val rectifyRequests = mutableListOf<RectifyHarvestYieldRequestDto>()
    val removedIds = mutableListOf<String>()

    override suspend fun getRecords(plotId: String) = records()
    override suspend fun getMetrics(plotId: String, metricName: String) = metrics()

    override suspend fun recordYield(plotId: String, request: RecordHarvestYieldRequestDto): Response<HarvestRecordDto> {
        recordRequests += request
        return recorded()
    }

    override suspend fun rectifyYield(
        plotId: String,
        recordId: String,
        request: RectifyHarvestYieldRequestDto,
    ): Response<HarvestRecordDto> {
        rectifyRequests += request
        return rectified()
    }

    override suspend fun removeRecord(plotId: String, recordId: String): Response<Unit> {
        removedIds += recordId
        return removed()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class HarvestRecordRepositoryImplTest {

    private val now = Instant.parse("2026-10-01T12:00:00Z")
    private val recordDao = FakeHarvestRecordDao()
    private val indexDao = FakeBearingIndexDao()
    private val cacheDao = FakeCacheMetadataDao()
    private val service = FakePhenologyService()
    private val repository = HarvestRecordRepositoryImpl(
        service = service,
        recordDao = recordDao,
        indexDao = indexDao,
        cacheMetadataDao = cacheDao,
        apiCaller = ApiCaller(ApiErrorMapper(Json), UnconfinedTestDispatcher()),
        clock = Clock.fixed(now, ZoneOffset.UTC),
    )

    private fun dto(id: String, year: Int, kg: Double, plotId: String = "p-1") = HarvestRecordDto(
        id = id,
        plotId = plotId,
        campaignYear = year,
        totalYieldKg = kg,
        bearingClassification = "ON_YEAR",
        recordedAt = "2026-10-01T12:00:00Z",
    )

    private fun metric(value: Double = 0.51) = MetricDto(
        metricName = "BIENNIAL_BEARING_INDEX",
        value = value,
        details = MetricDetailsDto(evaluatedYearsCount = 4),
        evaluatedAt = "2026-10-01T12:00:00Z",
    )

    private fun <T> notFound(): Response<T> =
        Response.error(404, "{}".toResponseBody("application/json".toMediaType()))

    @Test
    fun refreshCachesRecordsIndexAndStampsTheRefreshTime() = runTest {
        service.records = { Response.success(listOf(dto("r-1", 2023, 100.0), dto("r-2", 2024, 200.0))) }
        service.metrics = { Response.success(listOf(metric())) }

        assertTrue(repository.refresh("p-1") is AppResult.Success)

        assertEquals(listOf(2024, 2023), repository.observeRecords("p-1").first().map { it.campaignYear })
        assertEquals(BearingYear.ON, repository.observeRecords("p-1").first().first().bearing)
        assertEquals(0.51, repository.observeBearingIndex("p-1").first()!!.value!!, 0.0)
        assertEquals(now, repository.observeLastRefresh("p-1").first())
    }

    @Test
    fun refreshReplacesTheCachedRecordsOfThePlotOnly() = runTest {
        recordDao.upsertAll(listOf(dto("old", 2020, 1.0).toEntityForTest(), dto("other", 2021, 1.0, "p-2").toEntityForTest()))
        service.records = { Response.success(listOf(dto("r-1", 2024, 200.0))) }

        repository.refresh("p-1")

        assertEquals(listOf("r-1"), repository.observeRecords("p-1").first().map { it.id })
        assertEquals(listOf("other"), repository.observeRecords("p-2").first().map { it.id })
    }

    @Test
    fun metricsNotFoundMeansNoIndexAndRefreshStillSucceeds() = runTest {
        indexDao.upsert(BearingIndexEntity("p-1", 0.3, 3, null))
        service.records = { Response.success(emptyList()) }
        service.metrics = { notFound() }

        assertTrue(repository.refresh("p-1") is AppResult.Success)

        assertNull(repository.observeBearingIndex("p-1").first())
        assertEquals(now, repository.observeLastRefresh("p-1").first())
    }

    @Test
    fun failedRefreshKeepsTheCacheAndDoesNotStamp() = runTest {
        recordDao.upsertAll(listOf(dto("old", 2020, 1.0).toEntityForTest()))
        service.records = { throw IOException("offline") }

        val result = repository.refresh("p-1")

        assertEquals(AppResult.Failure(AppError.Offline), result)
        assertEquals(listOf("old"), repository.observeRecords("p-1").first().map { it.id })
        assertNull(repository.observeLastRefresh("p-1").first())
    }

    @Test
    fun metricsServerErrorFailsTheRefresh() = runTest {
        service.metrics = { Response.error(500, "{}".toResponseBody("application/json".toMediaType())) }

        val result = repository.refresh("p-1")

        assertTrue(result is AppResult.Failure && result.error is AppError.Server)
    }

    @Test
    fun recordSendsTheRequestAndRefreshesTheCache() = runTest {
        service.recorded = { Response.success(dto("r-9", 2025, 6750.0)) }
        service.records = { Response.success(listOf(dto("r-9", 2025, 6750.0))) }
        service.metrics = { Response.success(listOf(metric(0.4))) }

        val result = repository.record("p-1", 2025, 6750.0)

        assertEquals(RecordHarvestYieldRequestDto(campaignYear = 2025, totalYieldKg = 6750.0), service.recordRequests.single())
        assertEquals("r-9", (result as AppResult.Success).value.id)
        assertEquals(0.4, repository.observeBearingIndex("p-1").first()!!.value!!, 0.0)
    }

    @Test
    fun recordConflictIsReturnedAndNothingIsCached() = runTest {
        service.recorded = { Response.error(409, "{}".toResponseBody("application/json".toMediaType())) }

        val result = repository.record("p-1", 2025, 1.0)

        assertTrue(result is AppResult.Failure && result.error is AppError.Conflict)
        assertTrue(repository.observeRecords("p-1").first().isEmpty())
    }

    @Test
    fun recordSucceedsEvenWhenTheFollowUpRefreshFails() = runTest {
        service.recorded = { Response.success(dto("r-9", 2025, 6750.0)) }
        service.records = { throw IOException("offline") }

        assertTrue(repository.record("p-1", 2025, 6750.0) is AppResult.Success)
        assertEquals(listOf("r-9"), repository.observeRecords("p-1").first().map { it.id })
    }

    @Test
    fun rectifySendsTheNewYieldAndRefreshes() = runTest {
        service.rectified = { Response.success(dto("r-1", 2024, 500.0)) }
        service.records = { Response.success(listOf(dto("r-1", 2024, 500.0))) }

        val result = repository.rectify("p-1", "r-1", 500.0)

        assertEquals(RectifyHarvestYieldRequestDto(totalYieldKg = 500.0), service.rectifyRequests.single())
        assertEquals(500.0, (result as AppResult.Success).value.totalYieldKg, 0.0)
        assertEquals(500.0, repository.observeRecords("p-1").first().single().totalYieldKg, 0.0)
    }

    @Test
    fun removeDeletesRemotelyThenRefreshes() = runTest {
        recordDao.upsertAll(listOf(dto("r-1", 2024, 1.0).toEntityForTest()))
        service.records = { Response.success(emptyList()) }

        val result = repository.remove("p-1", "r-1")

        assertTrue(result is AppResult.Success)
        assertEquals(listOf("r-1"), service.removedIds)
        assertTrue(repository.observeRecords("p-1").first().isEmpty())
    }

    @Test
    fun removeFailureKeepsTheCachedRecord() = runTest {
        recordDao.upsertAll(listOf(dto("r-1", 2024, 1.0).toEntityForTest()))
        service.removed = { throw IOException("offline") }

        assertEquals(AppResult.Failure(AppError.Offline), repository.remove("p-1", "r-1"))
        assertEquals(1, repository.observeRecords("p-1").first().size)
    }

    @Test
    fun removeAcceptsA200WithAMessageBody() = runTest {
        recordDao.upsertAll(listOf(dto("r-1", 2024, 1.0).toEntityForTest()))
        service.records = { Response.success(emptyList()) }
        service.removed = { Response.success(200, Unit) }

        assertTrue(repository.remove("p-1", "r-1") is AppResult.Success)
        assertTrue(repository.observeRecords("p-1").first().isEmpty())
    }

    @Test
    fun removePreconditionFailedIsReturnedAndTheCacheKept() = runTest {
        recordDao.upsertAll(listOf(dto("r-1", 2024, 1.0).toEntityForTest()))
        service.removed = { Response.error(412, "{}".toResponseBody("application/json".toMediaType())) }

        val result = repository.remove("p-1", "r-1")

        assertTrue((result as AppResult.Failure).error is AppError.PreconditionFailed)
        assertEquals(1, repository.observeRecords("p-1").first().size)
    }

    private fun HarvestRecordDto.toEntityForTest() =
        toEntity()
}
