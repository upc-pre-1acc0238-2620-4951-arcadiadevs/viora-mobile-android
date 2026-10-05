package pe.edu.upc.viora.features.harvestsettlement.infrastructure

import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
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
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.local.HarvestSettlementDao
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.local.HarvestSettlementEntity
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.mapper.toEntity
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.remote.HarvestSettlementDto
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.remote.HarvestSettlementService
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.repository.HarvestSettlementRepositoryImpl
import retrofit2.Response

private class FakeHarvestSettlementDao : HarvestSettlementDao {
    val rows = MutableStateFlow<List<HarvestSettlementEntity>>(emptyList())

    override fun observeAll(): Flow<List<HarvestSettlementEntity>> = rows

    override suspend fun upsertAll(entities: List<HarvestSettlementEntity>) {
        val byId = rows.value.associateBy { it.id }.toMutableMap()
        entities.forEach { byId[it.id] = it }
        rows.value = byId.values.toList()
    }

    override suspend fun deleteByPlot(plotId: String) {
        rows.value = rows.value.filter { it.plotId != plotId }
    }
}

private class FakeHarvestSettlementService : HarvestSettlementService {
    val responses = mutableMapOf<String, () -> Response<List<HarvestSettlementDto>>>()
    val requested = mutableListOf<String>()

    override suspend fun getSettlements(plotId: String): Response<List<HarvestSettlementDto>> {
        requested += plotId
        return (responses[plotId] ?: { Response.success(emptyList()) })()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class HarvestSettlementRepositoryImplTest {

    private val dao = FakeHarvestSettlementDao()
    private val service = FakeHarvestSettlementService()
    private val repository = HarvestSettlementRepositoryImpl(
        service = service,
        dao = dao,
        apiCaller = ApiCaller(ApiErrorMapper(Json), UnconfinedTestDispatcher()),
    )

    private fun dto(id: String, plotId: String, year: Int, settledAt: String) = HarvestSettlementDto(
        id = id,
        reportId = "rep-$id",
        plotId = plotId,
        campaignYear = year,
        greenOlivesKg = 1.0,
        blackOlivesKg = 2.0,
        totalYieldKg = 3.0,
        status = "SETTLED",
        settledAt = settledAt,
    )

    private fun <T> httpError(code: Int): Response<T> =
        Response.error(code, "{}".toResponseBody("application/json".toMediaType()))

    @Test
    fun refreshCachesTheSettlementsNewestSettledFirst() = runTest {
        service.responses["p-1"] = {
            Response.success(
                listOf(
                    dto("a", "p-1", 2023, "2025-03-01T00:00:00Z"),
                    dto("b", "p-1", 2024, "2026-03-01T00:00:00Z"),
                ),
            )
        }

        assertTrue(repository.refresh("p-1") is AppResult.Success)

        assertEquals(listOf("b", "a"), repository.observeAll().first().map { it.id })
    }

    @Test
    fun refreshReplacesTheCachedRowsOfThePlotOnly() = runTest {
        dao.upsertAll(
            listOf(
                dto("old", "p-1", 2020, "2021-01-01T00:00:00Z").toEntity(),
                dto("other", "p-2", 2021, "2022-01-01T00:00:00Z").toEntity(),
            ),
        )
        service.responses["p-1"] = { Response.success(listOf(dto("new", "p-1", 2024, "2026-03-01T00:00:00Z"))) }

        repository.refresh("p-1")

        assertEquals(setOf("new", "other"), repository.observeAll().first().map { it.id }.toSet())
    }

    @Test
    fun anEmptyResponseClearsThePlot() = runTest {
        dao.upsertAll(listOf(dto("old", "p-1", 2020, "2021-01-01T00:00:00Z").toEntity()))
        service.responses["p-1"] = { Response.success(emptyList()) }

        assertTrue(repository.refresh("p-1") is AppResult.Success)

        assertTrue(repository.observeAll().first().isEmpty())
    }

    @Test
    fun failuresAreMappedAndKeepTheCache() = runTest {
        dao.upsertAll(listOf(dto("old", "p-1", 2020, "2021-01-01T00:00:00Z").toEntity()))

        service.responses["p-1"] = { throw IOException("offline") }
        assertEquals(AppResult.Failure(AppError.Offline), repository.refresh("p-1"))

        service.responses["p-1"] = { httpError(500) }
        val server = repository.refresh("p-1")
        assertTrue(server is AppResult.Failure && server.error is AppError.Server)

        service.responses["p-1"] = { httpError(404) }
        val notFound = repository.refresh("p-1")
        assertTrue(notFound is AppResult.Failure && notFound.error is AppError.NotFound)

        assertEquals(listOf("old"), repository.observeAll().first().map { it.id })
    }

    @Test
    fun refreshAllKeepsSuccessfulPlotsAndReturnsTheFirstFailure() = runTest {
        service.responses["p-1"] = { Response.success(listOf(dto("a", "p-1", 2024, "2026-03-01T00:00:00Z"))) }
        service.responses["p-2"] = { throw IOException("offline") }
        service.responses["p-3"] = { httpError(500) }
        service.responses["p-4"] = { Response.success(listOf(dto("d", "p-4", 2024, "2026-04-01T00:00:00Z"))) }

        val result = repository.refreshAll(listOf("p-1", "p-2", "p-3", "p-4"))

        assertEquals(AppResult.Failure(AppError.Offline), result)
        assertEquals(listOf("p-1", "p-2", "p-3", "p-4"), service.requested)
        assertEquals(listOf("d", "a"), repository.observeAll().first().map { it.id })
    }

    @Test
    fun refreshAllSucceedsWhenEveryPlotRefreshes() = runTest {
        assertTrue(repository.refreshAll(listOf("p-1", "p-2")) is AppResult.Success)
        assertTrue(repository.refreshAll(emptyList()) is AppResult.Success)
    }
}
