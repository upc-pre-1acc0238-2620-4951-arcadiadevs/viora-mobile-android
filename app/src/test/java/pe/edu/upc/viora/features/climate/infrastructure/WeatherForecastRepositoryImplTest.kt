package pe.edu.upc.viora.features.climate.infrastructure

import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
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
import pe.edu.upc.viora.features.climate.infrastructure.local.WeatherForecastDao
import pe.edu.upc.viora.features.climate.infrastructure.local.WeatherForecastEntity
import pe.edu.upc.viora.features.climate.infrastructure.remote.ClimateService
import pe.edu.upc.viora.features.climate.infrastructure.remote.dto.DayForecastDto
import pe.edu.upc.viora.features.climate.infrastructure.remote.dto.WeatherForecastResponseDto
import pe.edu.upc.viora.features.climate.infrastructure.repository.WeatherForecastRepositoryImpl
import retrofit2.Response

private class FakeWeatherForecastDao : WeatherForecastDao {
    val rows = MutableStateFlow<List<WeatherForecastEntity>>(emptyList())

    override fun observeByPlot(plotId: String): Flow<List<WeatherForecastEntity>> =
        rows.map { all -> all.filter { it.plotId == plotId }.sortedBy { it.forecastDate } }

    override suspend fun upsertAll(entities: List<WeatherForecastEntity>) {
        val byKey = rows.value.associateBy { it.plotId to it.forecastDate }.toMutableMap()
        entities.forEach { byKey[it.plotId to it.forecastDate] = it }
        rows.value = byKey.values.toList()
    }

    override suspend fun deleteByPlot(plotId: String) {
        rows.value = rows.value.filter { it.plotId != plotId }
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

private class FakeClimateService : ClimateService {
    var forecastResponse: () -> Response<WeatherForecastResponseDto> = {
        Response.success(
            WeatherForecastResponseDto(
                plotId = "p-1",
                dailyForecasts = emptyList(),
                generatedAt = "2026-10-05T12:00:00Z",
            ),
        )
    }

    override suspend fun getPlotForecast(plotId: String): Response<WeatherForecastResponseDto> =
        forecastResponse()
}

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherForecastRepositoryImplTest {

    private val fixedInstant = Instant.parse("2026-10-05T12:00:00Z")
    private val clock = Clock.fixed(fixedInstant, ZoneOffset.UTC)
    private val fakeDao = FakeWeatherForecastDao()
    private val fakeCacheMetadataDao = FakeCacheMetadataDao()
    private val fakeService = FakeClimateService()
    private val apiCaller = ApiCaller(ApiErrorMapper(Json { ignoreUnknownKeys = true }), UnconfinedTestDispatcher())

    private val repository = WeatherForecastRepositoryImpl(
        service = fakeService,
        forecastDao = fakeDao,
        cacheMetadataDao = fakeCacheMetadataDao,
        apiCaller = apiCaller,
        clock = clock,
    )

    private fun sampleDay(date: String, max: Double, min: Double) = DayForecastDto(
        forecastDate = date,
        maxTemperature = max,
        minTemperature = min,
        precipitationProbability = 0.0,
        windSpeedKmh = 12.0,
        isFrostRisk = false,
        syncedAt = "2026-10-05T12:00:00Z",
    )

    @Test
    fun observeForecastReturnsNullWhenEmpty() = runTest {
        assertNull(repository.observeForecast("plot-1").first())
    }

    @Test
    fun refreshForecastStoresRowsAndUpdatesCacheMetadata() = runTest {
        fakeService.forecastResponse = {
            Response.success(
                WeatherForecastResponseDto(
                    plotId = "plot-1",
                    dailyForecasts = listOf(
                        sampleDay("2026-10-06", 25.0, 15.0),
                        sampleDay("2026-10-05", 26.0, 14.0),
                    ),
                    generatedAt = "2026-10-05T12:00:00Z",
                ),
            )
        }

        val result = repository.refreshForecast("plot-1")
        assertTrue(result is AppResult.Success)

        val forecast = repository.observeForecast("plot-1").first()
        assertNotNull(forecast)
        assertEquals(2, forecast!!.dailyForecasts.size)
        // Verify chronological ordering
        assertEquals(LocalDate.of(2026, 10, 5), forecast.dailyForecasts[0].date)
        assertEquals(LocalDate.of(2026, 10, 6), forecast.dailyForecasts[1].date)

        val lastRefresh = repository.observeLastRefresh("plot-1").first()
        assertEquals(fixedInstant, lastRefresh)
    }

    @Test
    fun refreshForecastPropagatesNetworkFailureWithoutOverwritingCache() = runTest {
        // Pre-populate database
        fakeDao.upsertAll(
            listOf(
                WeatherForecastEntity(
                    plotId = "plot-1",
                    forecastDate = "2026-10-05",
                    maxTemperature = 22.0,
                    minTemperature = 12.0,
                    precipitationProbability = 5.0,
                    windSpeedKmh = 10.0,
                    isFrostRisk = false,
                    syncedAt = "2026-10-04T12:00:00Z",
                ),
            ),
        )

        fakeService.forecastResponse = { throw IOException("connection timed out") }

        val result = repository.refreshForecast("plot-1")
        assertTrue(result is AppResult.Failure)
        assertTrue((result as AppResult.Failure).error is AppError.Offline)

        // Cached data is still intact
        val forecast = repository.observeForecast("plot-1").first()
        assertNotNull(forecast)
        assertEquals(1, forecast!!.dailyForecasts.size)
    }
}
