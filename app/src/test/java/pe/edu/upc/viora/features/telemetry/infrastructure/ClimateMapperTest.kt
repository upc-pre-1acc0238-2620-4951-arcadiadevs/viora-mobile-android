package pe.edu.upc.viora.features.telemetry.infrastructure

import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pe.edu.upc.viora.features.telemetry.infrastructure.local.ForecastDayEntity
import pe.edu.upc.viora.features.telemetry.infrastructure.mapper.toDomain
import pe.edu.upc.viora.features.telemetry.infrastructure.mapper.toEntity
import pe.edu.upc.viora.features.telemetry.infrastructure.mapper.toForecastOrNull
import pe.edu.upc.viora.features.telemetry.infrastructure.remote.ForecastDayDto
import pe.edu.upc.viora.features.telemetry.infrastructure.remote.HourlyReadingDto

class ClimateMapperTest {

    @Test
    fun `a reading dto becomes an entity keyed by plot and instant`() {
        val entity = HourlyReadingDto("2026-10-06T15:00:00Z", 27.4, 38.0, 34.0, 40.0).toEntity("p1")!!
        assertEquals("p1|1791298800000", entity.id)
        assertEquals(34.0, entity.soilMoisture30cmPercent!!, 0.0)
        assertEquals(Instant.parse("2026-10-06T15:00:00Z"), entity.toDomain().observedAt)
    }

    @Test
    fun `a reading with an unreadable timestamp is dropped`() {
        assertNull(HourlyReadingDto("yesterday", 20.0).toEntity("p1"))
    }

    @Test
    fun `a forecast day dto keeps only the date part of the timestamp`() {
        val entity = ForecastDayDto("2026-10-07T00:00:00Z", 31.0, 15.0, 20.0, 18.0).toEntity("p1", 100L)!!
        assertEquals("2026-10-07", entity.forecastDate)
        assertEquals("p1|2026-10-07", entity.id)
    }

    @Test
    fun `cached days become a forecast sorted by date with the latest sync time`() {
        fun row(date: String, sync: Long, rain: Double = 12.6) =
            ForecastDayEntity("p|$date", "p", date, 30.0, 15.0, rain, 10.0, sync)
        val forecast = listOf(row("2026-10-08", 200), row("2026-10-07", 100)).toForecastOrNull("p")!!
        assertEquals(listOf(LocalDate.of(2026, 10, 7), LocalDate.of(2026, 10, 8)), forecast.days.map { it.date })
        assertEquals(Instant.ofEpochMilli(200), forecast.syncedAt)
        assertEquals(13, forecast.days.first().precipitationProbabilityPercent)
    }

    @Test
    fun `nothing cached means no forecast`() {
        assertNull(emptyList<ForecastDayEntity>().toForecastOrNull("p"))
    }
}
