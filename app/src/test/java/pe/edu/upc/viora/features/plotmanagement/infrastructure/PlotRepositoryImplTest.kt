package pe.edu.upc.viora.features.plotmanagement.infrastructure

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
import pe.edu.upc.viora.features.plotmanagement.infrastructure.local.PlotDao
import pe.edu.upc.viora.features.plotmanagement.infrastructure.local.PlotEntity
import pe.edu.upc.viora.features.plotmanagement.infrastructure.remote.PlotDto
import pe.edu.upc.viora.features.plotmanagement.infrastructure.remote.PlotService
import pe.edu.upc.viora.features.plotmanagement.infrastructure.repository.PlotRepositoryImpl
import retrofit2.Response

private class FakePlotDao : PlotDao {
    val rows = MutableStateFlow<List<PlotEntity>>(emptyList())

    override fun observeActive(): Flow<List<PlotEntity>> =
        rows.map { all -> all.filter { it.status == "ACTIVE" }.sortedBy { it.name.lowercase() } }

    override suspend fun upsertAll(entities: List<PlotEntity>) {
        val byId = rows.value.associateBy { it.id }.toMutableMap()
        entities.forEach { byId[it.id] = it }
        rows.value = byId.values.toList()
    }

    override suspend fun deleteAllExcept(keepIds: List<String>) {
        rows.value = rows.value.filter { it.id in keepIds }
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

private class FakePlotService(var next: () -> Response<List<PlotDto>>) : PlotService {
    override suspend fun getPlots(): Response<List<PlotDto>> = next()
}

@OptIn(ExperimentalCoroutinesApi::class)
class PlotRepositoryImplTest {

    private val now = Instant.parse("2026-10-01T12:00:00Z")
    private val plotDao = FakePlotDao()
    private val cacheDao = FakeCacheMetadataDao()
    private val service = FakePlotService { Response.success(emptyList()) }
    private val repository = PlotRepositoryImpl(
        service = service,
        plotDao = plotDao,
        cacheMetadataDao = cacheDao,
        apiCaller = ApiCaller(ApiErrorMapper(Json), UnconfinedTestDispatcher()),
        clock = Clock.fixed(now, ZoneOffset.UTC),
    )

    private fun dto(id: String, name: String, status: String = "ACTIVE", variety: String = "SEVILLANA") = PlotDto(
        id = id,
        producerId = "u-1",
        name = name,
        variety = variety,
        areaHa = 2.0,
        treeDensity = 100,
        rowSpacingM = 7.0,
        treeSpacingM = 5.0,
        polygonGeoJson = """{"type":"Polygon","coordinates":[[[0,0],[1,0],[1,1],[0,0]]]}""",
        status = status,
        revision = 0,
    )

    @Test
    fun `refresh stores the plots and observePlots emits them ordered by name`() = runTest {
        service.next = { Response.success(listOf(dto("2", "norte"), dto("1", "Alfa"))) }

        val result = repository.refresh()

        assertEquals(AppResult.Success(Unit), result)
        assertEquals(listOf("Alfa", "norte"), repository.observePlots().first().map { it.name })
    }

    @Test
    fun `refresh stamps the last refresh time`() = runTest {
        assertNull(repository.observeLastRefresh().first())

        repository.refresh()

        assertEquals(now, repository.observeLastRefresh().first())
    }

    @Test
    fun `refresh removes plots the server no longer returns`() = runTest {
        service.next = { Response.success(listOf(dto("1", "Alfa"), dto("2", "Beta"))) }
        repository.refresh()

        service.next = { Response.success(listOf(dto("2", "Beta"))) }
        repository.refresh()

        assertEquals(listOf("Beta"), repository.observePlots().first().map { it.name })
    }

    @Test
    fun `a server error keeps the cache and the previous refresh time`() = runTest {
        service.next = { Response.success(listOf(dto("1", "Alfa"))) }
        repository.refresh()
        val before = repository.observeLastRefresh().first()

        service.next = { Response.error(503, "".toResponseBody("application/json".toMediaType())) }
        val result = repository.refresh()

        assertEquals(AppResult.Failure(AppError.Server(status = 503, detail = null)), result)
        assertEquals(listOf("Alfa"), repository.observePlots().first().map { it.name })
        assertEquals(before, repository.observeLastRefresh().first())
    }

    @Test
    fun `no connection maps to Offline and keeps the cache`() = runTest {
        service.next = { Response.success(listOf(dto("1", "Alfa"))) }
        repository.refresh()

        service.next = { throw IOException("no route to host") }
        val result = repository.refresh()

        assertEquals(AppResult.Failure(AppError.Offline), result)
        assertEquals(1, repository.observePlots().first().size)
    }

    @Test
    fun `a cached plot with an unknown variety is skipped without hiding the others`() = runTest {
        service.next = { Response.success(listOf(dto("1", "Alfa"), dto("2", "Picual", variety = "PICUAL"))) }

        repository.refresh()

        assertEquals(listOf("Alfa"), repository.observePlots().first().map { it.name })
    }

    @Test
    fun `inactive plots never reach the list`() = runTest {
        service.next = { Response.success(listOf(dto("1", "Alfa"), dto("2", "Beta", status = "INACTIVE"))) }

        repository.refresh()

        assertTrue(repository.observePlots().first().none { it.name == "Beta" })
    }
}
