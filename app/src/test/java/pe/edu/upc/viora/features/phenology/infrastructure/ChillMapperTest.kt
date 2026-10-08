package pe.edu.upc.viora.features.phenology.infrastructure

import java.time.LocalDate
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import pe.edu.upc.viora.features.phenology.REAL_CHILL_METRIC_JSON
import pe.edu.upc.viora.features.phenology.domain.entity.ChillProjection
import pe.edu.upc.viora.features.phenology.domain.entity.ThermalAnomaly
import pe.edu.upc.viora.features.phenology.domain.entity.WinterSeasonState
import pe.edu.upc.viora.features.phenology.infrastructure.mapper.toChillEntity
import pe.edu.upc.viora.features.phenology.infrastructure.mapper.toChillProjection
import pe.edu.upc.viora.features.phenology.infrastructure.mapper.toDomain
import pe.edu.upc.viora.features.phenology.infrastructure.mapper.toThermalAnomaly
import pe.edu.upc.viora.features.phenology.infrastructure.mapper.toWinterSeasonState
import pe.edu.upc.viora.features.phenology.infrastructure.remote.MetricDetailsDto
import pe.edu.upc.viora.features.phenology.infrastructure.remote.MetricDto

class ChillMapperTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun mapsTheRealBackendAnswerToTheDomain() {
        val dto = json.decodeFromString<List<MetricDto>>(REAL_CHILL_METRIC_JSON).single()

        val entity = dto.toChillEntity("plot-1", nowEpochMs = 1000L)
        assertNotNull(entity)
        val tracker = entity!!.toDomain()

        assertEquals(2026, tracker.seasonYear)
        assertEquals(3.03, tracker.accumulatedPortions, 0.001)
        assertEquals(30.0, tracker.thresholdPortions, 0.001)
        assertEquals(WinterSeasonState.OFF_SEASON, tracker.seasonState)
        assertEquals(LocalDate.of(2026, 8, 31), tracker.evaluatedThrough)
        assertNull(tracker.completionDate)
        assertEquals(ChillProjection.NOT_APPLICABLE, tracker.projection)
        assertNull(tracker.projectedCompletionDate)
        assertEquals(1, tracker.daysAbove24Celsius)
        assertEquals(1, tracker.longestWarmStreakDays)
        assertEquals(ThermalAnomaly.NONE, tracker.thermalAnomaly)
        assertEquals(2025, tracker.previousSeason!!.seasonYear)
        assertEquals(27.21, tracker.previousSeason!!.accumulatedPortions, 0.001)
        assertNull(tracker.previousSeason!!.completionDate)

        assertEquals(3, tracker.curvePoints.size)
        assertEquals(LocalDate.of(2026, 7, 16), tracker.curvePoints[1].date)
        assertEquals(2.02, tracker.curvePoints[1].accumulatedThisYear!!, 0.001)
        assertEquals(10.1, tracker.curvePoints[1].accumulatedPreviousYear!!, 0.001)
    }

    @Test
    fun keepsTheDaysWithoutDataAsNull() {
        val dto = json.decodeFromString<List<MetricDto>>(
            REAL_CHILL_METRIC_JSON.replace(
                """{"date":"2026-08-31","portions":3.03,"previousSeasonPortions":27.21}""",
                """{"date":"2026-08-31","portions":null,"previousSeasonPortions":null}""",
            ),
        ).single()

        val last = dto.toChillEntity("plot-1", 1000L)!!.toDomain().curvePoints.last()

        assertNull(last.accumulatedThisYear)
        assertNull(last.accumulatedPreviousYear)
    }

    @Test
    fun rejectsAMetricWithoutTheSeasonFields() {
        val dto = MetricDto(
            metricName = "EREZ_CHILLING_PORTIONS",
            value = 28.5,
            details = MetricDetailsDto(seasonState = "COMPLETED"),
        )

        assertNull(dto.toChillEntity("plot-1", 1000L))
    }

    @Test
    fun mapsTheBackendEnums() {
        assertEquals(WinterSeasonState.ACCUMULATING, "IN_PROGRESS".toWinterSeasonState())
        assertEquals(WinterSeasonState.CHILL_HALTED, "HALTED".toWinterSeasonState())
        assertEquals(WinterSeasonState.COMPLETED, "COMPLETED".toWinterSeasonState())
        assertEquals(WinterSeasonState.OFF_SEASON, "OFF_SEASON".toWinterSeasonState())
        assertEquals(ThermalAnomaly.ACTIVE, "ACTIVE".toThermalAnomaly())
        assertEquals(ThermalAnomaly.RECORDED, "RECORDED".toThermalAnomaly())
        assertEquals(ThermalAnomaly.NONE, "NONE".toThermalAnomaly())
        assertEquals(ChillProjection.PROJECTED, "PROJECTED".toChillProjection())
        assertEquals(ChillProjection.NOT_REACHABLE_IN_SEASON, "NOT_REACHABLE_IN_SEASON".toChillProjection())
        assertEquals(ChillProjection.INSUFFICIENT_DATA, "INSUFFICIENT_DATA".toChillProjection())
    }
}
