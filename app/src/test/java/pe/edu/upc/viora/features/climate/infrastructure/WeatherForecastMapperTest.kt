package pe.edu.upc.viora.features.climate.infrastructure

import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.viora.features.climate.infrastructure.mapper.toDomain
import pe.edu.upc.viora.features.climate.infrastructure.mapper.toEntity
import pe.edu.upc.viora.features.climate.infrastructure.remote.dto.DayForecastDto
import pe.edu.upc.viora.features.climate.infrastructure.remote.dto.WeatherForecastResponseDto

class WeatherForecastMapperTest {

    private fun sampleDayDto(
        date: String = "2026-10-05",
        isFrostRisk: Boolean = false,
        syncedAt: String = "2026-10-05T17:14:30.833Z",
    ) = DayForecastDto(
        forecastDate = date,
        maxTemperature = 24.7,
        minTemperature = 14.1,
        precipitationProbability = 10.0,
        windSpeedKmh = 16.1,
        isFrostRisk = isFrostRisk,
        syncedAt = syncedAt,
    )

    @Test
    fun mapsDayForecastDtoToEntityAndDomain() {
        val dto = sampleDayDto(isFrostRisk = true)
        val entity = dto.toEntity("plot-1")

        assertEquals("plot-1", entity.plotId)
        assertEquals("2026-10-05", entity.forecastDate)
        assertEquals(24.7, entity.maxTemperature, 0.0)
        assertEquals(14.1, entity.minTemperature, 0.0)
        assertEquals(10.0, entity.precipitationProbability, 0.0)
        assertEquals(16.1, entity.windSpeedKmh, 0.0)
        assertTrue(entity.isFrostRisk)

        val domain = entity.toDomain()
        assertEquals(LocalDate.of(2026, 10, 5), domain.date)
        assertEquals(24.7, domain.maxTempCelsius, 0.0)
        assertEquals(14.1, domain.minTempCelsius, 0.0)
        assertEquals(10.0, domain.precipitationProbability, 0.0)
        assertEquals(16.1, domain.windSpeedKmh, 0.0)
        assertTrue(domain.isFrostRisk)
        assertEquals(Instant.parse("2026-10-05T17:14:30.833Z"), domain.syncedAt)
    }

    @Test
    fun mapsFullResponseDtoToDomain() {
        val responseDto = WeatherForecastResponseDto(
            plotId = "plot-123",
            dailyForecasts = listOf(
                sampleDayDto(date = "2026-10-06"),
                sampleDayDto(date = "2026-10-05"),
            ),
            generatedAt = "2026-10-05T17:14:30.836Z",
        )

        val domain = responseDto.toDomain()
        assertEquals("plot-123", domain.plotId.value)
        assertEquals(2, domain.dailyForecasts.size)
        // Ensure sorted chronologically
        assertEquals(LocalDate.of(2026, 10, 5), domain.dailyForecasts[0].date)
        assertEquals(LocalDate.of(2026, 10, 6), domain.dailyForecasts[1].date)
        assertEquals(Instant.parse("2026-10-05T17:14:30.836Z"), domain.generatedAt)
    }

    @Test
    fun mapsEntitiesListToDomain() {
        val entities = listOf(
            sampleDayDto(date = "2026-10-07").toEntity("plot-abc"),
            sampleDayDto(date = "2026-10-05").toEntity("plot-abc"),
        )

        val domain = entities.toDomain("plot-abc", Instant.parse("2026-10-05T18:00:00Z"))
        assertEquals("plot-abc", domain.plotId.value)
        assertEquals(2, domain.dailyForecasts.size)
        assertEquals(LocalDate.of(2026, 10, 5), domain.dailyForecasts[0].date)
        assertEquals(LocalDate.of(2026, 10, 7), domain.dailyForecasts[1].date)
        assertEquals(Instant.parse("2026-10-05T18:00:00Z"), domain.generatedAt)
    }

    @Test
    fun fallsBackToEpochWhenTimestampIsInvalid() {
        val dto = sampleDayDto(syncedAt = "invalid-date")
        val domain = dto.toEntity("p-1").toDomain()
        assertEquals(Instant.EPOCH, domain.syncedAt)
    }
}
