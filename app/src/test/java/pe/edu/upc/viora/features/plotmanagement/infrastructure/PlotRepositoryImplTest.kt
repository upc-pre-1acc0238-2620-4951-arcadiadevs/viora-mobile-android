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
import pe.edu.upc.viora.features.plotmanagement.domain.entity.NewPlot
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlantationFrame
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotName
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotOutline
import pe.edu.upc.viora.features.plotmanagement.infrastructure.remote.CreatePlotRequestDto
import pe.edu.upc.viora.features.plotmanagement.infrastructure.remote.PlotDto
import pe.edu.upc.viora.features.plotmanagement.infrastructure.remote.PlotService
import pe.edu.upc.viora.features.plotmanagement.infrastructure.repository.PlotRepositoryImpl
import retrofit2.Response

private class FakePlotDao : PlotDao {
    val rows = MutableStateFlow<List<PlotEntity>>(emptyList())

    override fun observeActive(): Flow<List<PlotEntity>> =
        rows.map { all -> all.filter { it.status == "ACTIVE" }.sortedBy { it.name.lowercase() } }

    override fun observeById(id: String): Flow<PlotEntity?> = rows.map { all -> all.firstOrNull { it.id == id } }

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
    val createRequests = mutableListOf<CreatePlotRequestDto>()
    var created: () -> Response<PlotDto> = { throw IOException("not configured") }

    override suspend fun getPlots(): Response<List<PlotDto>> = next()
    override suspend fun createPlot(request: CreatePlotRequestDto): Response<PlotDto> {
        createRequests += request
        return created()
    }
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

    // ---- one plot

    @Test
    fun `observePlot emits the cached plot`() = runTest {
        service.next = { Response.success(listOf(dto("1", "Alfa"), dto("2", "Beta"))) }
        repository.refresh()

        assertEquals("Beta", repository.observePlot(PlotId("2")).first()?.name)
    }

    @Test
    fun `observePlot emits null for a plot that is not cached`() = runTest {
        assertNull(repository.observePlot(PlotId("missing")).first())
    }

    @Test
    fun `observePlot still emits a plot that is no longer active`() = runTest {
        service.next = { Response.success(listOf(dto("1", "Alfa", status = "INACTIVE"))) }
        repository.refresh()

        val plot = repository.observePlot(PlotId("1")).first()

        assertEquals(false, plot?.isActive)
    }

    // ---- register

    private val newPlot = NewPlot(
        name = PlotName.of("La Yarada 03"),
        variety = OliveVariety.SEVILLANA,
        outline = PlotOutline(
            listOf(GeoPoint(-18.05, -70.25), GeoPoint(-18.05, -70.24), GeoPoint(-18.06, -70.24)),
        ),
        frame = PlantationFrame(7.0, 7.0),
    )

    private fun problem(status: Int, code: String, detail: String) = Response.error<PlotDto>(
        status,
        """{"status":$status,"detail":"$detail","code":"$code"}""".toResponseBody("application/problem+json".toMediaType()),
    )

    @Test
    fun `register sends the plot, caches the answer and returns it`() = runTest {
        service.created = { Response.success(201, dto("new-1", "La Yarada 03")) }

        val result = repository.register(newPlot)

        assertEquals("new-1", (result as AppResult.Success).value.id.value)
        val sent = service.createRequests.single()
        assertEquals("La Yarada 03", sent.name)
        assertEquals("SEVILLANA", sent.variety)
        assertEquals(7.0, sent.rowSpacingM, 0.0)
        assertEquals(listOf("La Yarada 03"), repository.observePlots().first().map { it.name })
    }

    @Test
    fun `register maps a duplicated name to Conflict and caches nothing`() = runTest {
        service.created = { problem(409, "PLOT_CONFLICT", "Name already used") }

        val result = repository.register(newPlot)

        assertEquals(AppResult.Failure(AppError.Conflict("Name already used", "PLOT_CONFLICT")), result)
        assertTrue(repository.observePlots().first().isEmpty())
    }

    @Test
    fun `register maps a rejected polygon to Validation`() = runTest {
        service.created = { problem(400, "VALIDATION_ERROR", "Polygon ring is not closed") }

        val result = repository.register(newPlot)

        assertEquals(AppResult.Failure(AppError.Validation("Polygon ring is not closed", "VALIDATION_ERROR")), result)
    }

    @Test
    fun `register without connection maps to Offline`() = runTest {
        service.created = { throw IOException("no route") }

        assertEquals(AppResult.Failure(AppError.Offline), repository.register(newPlot))
    }
}
