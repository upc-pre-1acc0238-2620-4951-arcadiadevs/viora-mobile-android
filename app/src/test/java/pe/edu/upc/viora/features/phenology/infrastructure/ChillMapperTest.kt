package pe.edu.upc.viora.features.phenology.infrastructure

import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import pe.edu.upc.viora.features.phenology.domain.entity.EnsoRiskLevel
import pe.edu.upc.viora.features.phenology.domain.entity.WinterSeasonState
import pe.edu.upc.viora.features.phenology.infrastructure.mapper.toChillEntity
import pe.edu.upc.viora.features.phenology.infrastructure.mapper.toDomain
import pe.edu.upc.viora.features.phenology.infrastructure.mapper.toEnsoRiskLevel
import pe.edu.upc.viora.features.phenology.infrastructure.mapper.toWinterSeasonState
import pe.edu.upc.viora.features.phenology.infrastructure.remote.ChillCurvePointDto
import pe.edu.upc.viora.features.phenology.infrastructure.remote.MetricDetailsDto
import pe.edu.upc.viora.features.phenology.infrastructure.remote.MetricDto

class ChillMapperTest {

    @Test
    fun mapsMetricDtoToEntityAndDomain() {
        val nowEpochMs = 1728302400000L // 2024-10-07
        val dto = MetricDto(
            metricName = "CHILLING",
            value = 24.0,
            qualitativeCategory = "ACCUMULATING",
            details = MetricDetailsDto(
                portionsAccumulated = 24.0,
                thresholdPortions = 30.0,
                daysAbove24Celsius = 4,
                seasonState = "ACCUMULATING",
                projectedCompletionDate = "2026-08-14",
                previousWinterCompletionDate = "2025-08-05",
                ensoRisk = "NEUTRAL",
                curvePoints = listOf(
                    ChillCurvePointDto("2026-06-01", 1.0, 0.8),
                    ChillCurvePointDto("2026-06-15", 5.0, 4.5),
                ),
            ),
        )

        val entity = dto.toChillEntity("plot-abc", nowEpochMs)
        assertEquals("plot-abc", entity.plotId)
        assertEquals(24.0, entity.accumulatedPortions, 0.001)
        assertEquals(30.0, entity.thresholdPortions, 0.001)
        assertEquals(4, entity.daysAbove24Celsius)
        assertEquals("ACCUMULATING", entity.seasonState)
        assertEquals("2026-08-14", entity.projectedCompletionDate)
        assertEquals("NEUTRAL", entity.ensoRisk)

        val domain = entity.toDomain()
        assertEquals("plot-abc", domain.plotId)
        assertEquals(24.0, domain.accumulatedPortions, 0.001)
        assertEquals(30.0, domain.thresholdPortions, 0.001)
        assertEquals(4, domain.daysAbove24Celsius)
        assertEquals(WinterSeasonState.ACCUMULATING, domain.seasonState)
        assertEquals(LocalDate.of(2026, 8, 14), domain.projectedCompletionDate)
        assertEquals(LocalDate.of(2025, 8, 5), domain.previousWinterCompletionDate)
        assertEquals(EnsoRiskLevel.NEUTRAL, domain.ensoRisk)
        assertEquals(2, domain.curvePoints.size)
        assertEquals(LocalDate.of(2026, 6, 1), domain.curvePoints[0].date)
        assertEquals(1.0, domain.curvePoints[0].accumulatedThisYear, 0.001)
        assertEquals(0.8, domain.curvePoints[0].accumulatedPreviousYear!!, 0.001)
        assertEquals(Instant.ofEpochMilli(nowEpochMs), domain.syncedAt)
    }

    @Test
    fun mapsSeasonStateVariations() {
        assertEquals(WinterSeasonState.ACCUMULATING, "ACCUMULATING".toWinterSeasonState())
        assertEquals(WinterSeasonState.CHILL_HALTED, "CHILL_HALTED".toWinterSeasonState())
        assertEquals(WinterSeasonState.CHILL_HALTED, "HALTED".toWinterSeasonState())
        assertEquals(WinterSeasonState.COMPLETED, "COMPLETED".toWinterSeasonState())
        assertEquals(WinterSeasonState.COMPLETED, "ESTIMULO_COMPLETADO".toWinterSeasonState())
        assertEquals(WinterSeasonState.OFF_SEASON, "OFF_SEASON".toWinterSeasonState())
        assertEquals(WinterSeasonState.OFF_SEASON, "FUERA_DE_TEMPORADA".toWinterSeasonState())
        assertEquals(WinterSeasonState.ACCUMULATING, "UNKNOWN_OTHER".toWinterSeasonState())
    }

    @Test
    fun mapsEnsoRiskLevels() {
        assertEquals(EnsoRiskLevel.NEUTRAL, "NEUTRAL".toEnsoRiskLevel())
        assertEquals(EnsoRiskLevel.ACTIVE, "ACTIVE".toEnsoRiskLevel())
        assertEquals(EnsoRiskLevel.ACTIVE, "ACTIVO".toEnsoRiskLevel())
        assertEquals(EnsoRiskLevel.HIGH, "HIGH".toEnsoRiskLevel())
        assertEquals(EnsoRiskLevel.HIGH, "ALTO".toEnsoRiskLevel())
        assertEquals(EnsoRiskLevel.NEUTRAL, "UNKNOWN".toEnsoRiskLevel())
    }

    @Test
    fun handlesNullDetailsGracefully() {
        val dto = MetricDto(
            metricName = "CHILLING",
            value = 18.5,
            qualitativeCategory = "COMPLETED",
            details = null,
        )
        val entity = dto.toChillEntity("plot-null", 1000L)
        val domain = entity.toDomain()

        assertEquals(18.5, domain.accumulatedPortions, 0.001)
        assertEquals(30.0, domain.thresholdPortions, 0.001)
        assertEquals(0, domain.daysAbove24Celsius)
        assertEquals(WinterSeasonState.COMPLETED, domain.seasonState)
        assertEquals(EnsoRiskLevel.NEUTRAL, domain.ensoRisk)
        assertEquals(0, domain.curvePoints.size)
    }
}
