package pe.edu.upc.viora.features.phenology.infrastructure

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test
import pe.edu.upc.viora.features.phenology.domain.entity.BearingYear
import pe.edu.upc.viora.features.phenology.infrastructure.mapper.toDomain
import pe.edu.upc.viora.features.phenology.infrastructure.mapper.toEntity
import pe.edu.upc.viora.features.phenology.infrastructure.remote.HarvestRecordDto
import pe.edu.upc.viora.features.phenology.infrastructure.remote.MetricDetailsDto
import pe.edu.upc.viora.features.phenology.infrastructure.remote.MetricDto

class HarvestRecordMapperTest {

    private fun dto(classification: String?, recordedAt: String? = "2026-10-01T12:00:00Z") = HarvestRecordDto(
        id = "r-1",
        plotId = "p-1",
        campaignYear = 2024,
        totalYieldKg = 22000.0,
        bearingClassification = classification,
        recordedAt = recordedAt,
    )

    @Test
    fun mapsBackendClassificationsToBearingYears() {
        assertEquals(BearingYear.ON, dto("ON_YEAR").toEntity().toDomain().bearing)
        assertEquals(BearingYear.OFF, dto("OFF_YEAR").toEntity().toDomain().bearing)
        assertEquals(BearingYear.BALANCED, dto("BALANCED").toEntity().toDomain().bearing)
        assertEquals(BearingYear.UNKNOWN, dto("INSUFFICIENT_DATA").toEntity().toDomain().bearing)
    }

    @Test
    fun unknownOrMissingClassificationIsUnknown() {
        assertEquals(BearingYear.UNKNOWN, dto("SOMETHING_NEW").toEntity().toDomain().bearing)
        assertEquals(BearingYear.UNKNOWN, dto(null).toEntity().toDomain().bearing)
    }

    @Test
    fun parsesRecordedAtAndFallsBackToEpoch() {
        assertEquals(Instant.parse("2026-10-01T12:00:00Z"), dto("ON_YEAR").toEntity().toDomain().recordedAt)
        assertEquals(Instant.parse("2026-10-01T12:00:00Z"), dto("ON_YEAR", "2026-10-01T07:00:00-05:00").toEntity().toDomain().recordedAt)
        assertEquals(Instant.EPOCH, dto("ON_YEAR", "garbage").toEntity().toDomain().recordedAt)
    }

    @Test
    fun mapsMetricToBearingIndex() {
        val metric = MetricDto(
            metricName = "BIENNIAL_BEARING_INDEX",
            value = 0.51,
            details = MetricDetailsDto(evaluatedYearsCount = 4),
            evaluatedAt = "2026-10-01T12:00:00Z",
        )
        val index = metric.toEntity("p-1").toDomain()
        assertEquals(0.51, index.value!!, 0.0)
        assertEquals(4, index.evaluatedYears)
        assertEquals(Instant.parse("2026-10-01T12:00:00Z"), index.evaluatedAt)
    }
}
