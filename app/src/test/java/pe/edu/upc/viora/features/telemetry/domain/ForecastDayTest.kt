package pe.edu.upc.viora.features.telemetry.domain

import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pe.edu.upc.viora.features.telemetry.domain.entity.ForecastDay
import pe.edu.upc.viora.features.telemetry.domain.entity.WeatherForecast
import pe.edu.upc.viora.features.telemetry.domain.valueobject.HeatLevel
import pe.edu.upc.viora.features.telemetry.domain.valueobject.SkyCondition

class ForecastDayTest {

    private fun day(max: Double = 29.0, min: Double = 14.0, rain: Int = 0, date: LocalDate = LocalDate.of(2026, 10, 6)) =
        ForecastDay(date, max, min, rain, windSpeedKmh = 12.0)

    @Test
    fun `sky comes from the chance of rain`() {
        assertEquals(SkyCondition.SUNNY, day(rain = 0).sky)
        assertEquals(SkyCondition.SUNNY, day(rain = 9).sky)
        assertEquals(SkyCondition.PARTLY_CLOUDY, day(rain = 10).sky)
        assertEquals(SkyCondition.PARTLY_CLOUDY, day(rain = 49).sky)
        assertEquals(SkyCondition.RAINY, day(rain = 50).sky)
    }

    @Test
    fun `heat level marks the critical flowering threshold`() {
        assertEquals(HeatLevel.NORMAL, day(max = 29.9).heat)
        assertEquals(HeatLevel.WARM, day(max = 30.0).heat)
        assertEquals(HeatLevel.WARM, day(max = 31.9).heat)
        assertEquals(HeatLevel.EXTREME, day(max = 32.0).heat)
    }

    @Test
    fun `week range is the lowest minimum and the highest maximum`() {
        val forecast = WeatherForecast(
            "p",
            listOf(day(max = 29.0, min = 14.0), day(max = 34.0, min = 17.0), day(max = 27.0, min = 13.0)),
            Instant.parse("2026-10-06T11:00:00Z"),
        )
        assertEquals(13.0, forecast.weekMinCelsius!!, 0.0)
        assertEquals(34.0, forecast.weekMaxCelsius!!, 0.0)
    }

    @Test
    fun `dayOf finds a date and returns null when the cache does not reach it`() {
        val forecast = WeatherForecast("p", listOf(day()), Instant.parse("2026-10-06T11:00:00Z"))
        assertEquals(29.0, forecast.dayOf(LocalDate.of(2026, 10, 6))!!.maxTemperatureCelsius, 0.0)
        assertNull(forecast.dayOf(LocalDate.of(2026, 10, 9)))
    }
}
