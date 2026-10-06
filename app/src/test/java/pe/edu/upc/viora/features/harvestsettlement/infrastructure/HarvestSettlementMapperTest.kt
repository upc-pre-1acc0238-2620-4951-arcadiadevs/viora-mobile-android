package pe.edu.upc.viora.features.harvestsettlement.infrastructure

import java.time.Instant
import java.time.LocalDate
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettleHarvestDraft
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettlementStatus
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettlementSummary
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.StabilizationStatus
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.ThinningStatus
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.mapper.toDomain
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.mapper.toRequestDto
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.mapper.toEntity
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.remote.ExistingSettlementDto
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.remote.HarvestSettlementDto
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.remote.StabilizationDto
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.remote.ThinningBalanceDto

class HarvestSettlementMapperTest {

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    private val fullJson = """
        [{
          "id": "s-1",
          "reportId": "rep-1",
          "plotId": "p-1",
          "campaignYear": 2025,
          "greenOlivesKg": 4000.5,
          "blackOlivesKg": 2750.0,
          "totalYieldKg": 6750.5,
          "commercialFruitsPerKg": 180.0,
          "notes": "good year",
          "status": "AUDITED",
          "settledAt": "2026-03-01T10:00:00Z",
          "futureField": {"a": 1},
          "thinningBalance": {
            "status": "EXECUTED_LATE",
            "executedDate": "2025-11-20",
            "prescribedRemovalPercentage": 40.0,
            "actualRemovalPercentage": 35.5,
            "deviationPercentagePoints": -4.5,
            "extra": true
          },
          "stabilization": {
            "status": "EVALUATED",
            "baselineCampaigns": 4,
            "settledCampaigns": 3,
            "baselineYieldKg": 15000.0,
            "baselineAlternationIndex": 0.6,
            "managedAlternationIndex": 0.3,
            "amplitudeReductionRate": 0.5,
            "targetAchieved": true,
            "interannualVarianceKg2": 1200.0,
            "coefficientOfVariation": 0.12,
            "requiredConsecutivePairs": 2,
            "other": "ignored"
          }
        }]
    """.trimIndent()

    private val minimalDto = HarvestSettlementDto(
        id = "s-2",
        reportId = "rep-2",
        plotId = "p-1",
        campaignYear = 2024,
        greenOlivesKg = 1.0,
        blackOlivesKg = 2.0,
        totalYieldKg = 3.0,
    )

    @Test
    fun deserializesARealisticPayloadIgnoringUnknownFields() {
        val dto = json.decodeFromString<List<HarvestSettlementDto>>(fullJson).single()

        assertEquals("s-1", dto.id)
        assertEquals(4000.5, dto.greenOlivesKg, 0.0)
        assertEquals("EXECUTED_LATE", dto.thinningBalance!!.status)
        assertEquals(2, dto.stabilization!!.requiredConsecutivePairs)
    }

    @Test
    fun mapsAFullPayloadToDomain() {
        val settlement = json.decodeFromString<List<HarvestSettlementDto>>(fullJson).single().toEntity().toDomain()

        assertEquals(2025, settlement.campaignYear)
        assertEquals(4000.5, settlement.greenKg, 0.0)
        assertEquals(2750.0, settlement.blackKg, 0.0)
        assertEquals(180.0, settlement.commercialFruitsPerKg!!, 0.0)
        assertEquals("good year", settlement.notes)
        assertEquals(SettlementStatus.AUDITED, settlement.status)
        assertEquals(Instant.parse("2026-03-01T10:00:00Z"), settlement.settledAt)
        with(settlement.thinningBalance) {
            assertEquals(ThinningStatus.EXECUTED_LATE, status)
            assertEquals(LocalDate.of(2025, 11, 20), executedDate)
            assertEquals(40.0, prescribedRemovalPercentage!!, 0.0)
            assertEquals(35.5, actualRemovalPercentage!!, 0.0)
            assertEquals(-4.5, deviationPercentagePoints!!, 0.0)
        }
        with(settlement.stabilization) {
            assertEquals(StabilizationStatus.EVALUATED, status)
            assertEquals(4, baselineCampaigns)
            assertEquals(3, settledCampaigns)
            assertEquals(15000.0, baselineYieldKg!!, 0.0)
            assertEquals(0.5, amplitudeReductionRate!!, 0.0)
            assertEquals(true, targetAchieved)
            assertEquals(2, requiredConsecutivePairs)
        }
    }

    @Test
    fun mapsANullablePayloadWithSafeDefaults() {
        val settlement = minimalDto.toEntity().toDomain()

        assertNull(settlement.commercialFruitsPerKg)
        assertNull(settlement.notes)
        assertEquals(SettlementStatus.SETTLED, settlement.status)
        assertEquals(Instant.EPOCH, settlement.settledAt)
        assertEquals(ThinningStatus.NOT_RECORDED, settlement.thinningBalance.status)
        assertNull(settlement.thinningBalance.executedDate)
        assertNull(settlement.thinningBalance.deviationPercentagePoints)
        assertEquals(StabilizationStatus.INSUFFICIENT_SETTLEMENTS, settlement.stabilization.status)
        assertNull(settlement.stabilization.targetAchieved)
    }

    @Test
    fun nullableNestedValuesStayNull() {
        val dto = minimalDto.copy(
            thinningBalance = ThinningBalanceDto(status = "NOT_RECORDED"),
            stabilization = StabilizationDto(status = "NO_BASELINE_ALTERNATION", baselineCampaigns = 2),
        )

        val settlement = dto.toEntity().toDomain()

        assertEquals(ThinningStatus.NOT_RECORDED, settlement.thinningBalance.status)
        assertNull(settlement.thinningBalance.actualRemovalPercentage)
        assertEquals(StabilizationStatus.NO_BASELINE_ALTERNATION, settlement.stabilization.status)
        assertEquals(2, settlement.stabilization.baselineCampaigns)
        assertNull(settlement.stabilization.managedAlternationIndex)
    }

    @Test
    fun unknownEnumStringsFallBackWithoutCrashing() {
        val dto = minimalDto.copy(
            status = "ARCHIVED",
            thinningBalance = ThinningBalanceDto(status = "SOMETHING_NEW"),
            stabilization = StabilizationDto(status = "SOMETHING_NEW"),
        )

        val settlement = dto.toEntity().toDomain()

        assertEquals(SettlementStatus.SETTLED, settlement.status)
        assertEquals(ThinningStatus.NOT_RECORDED, settlement.thinningBalance.status)
        assertEquals(StabilizationStatus.INSUFFICIENT_SETTLEMENTS, settlement.stabilization.status)
    }

    @Test
    fun parsesOffsetTimestampsAndToleratesGarbageDates() {
        val offset = minimalDto.copy(settledAt = "2026-03-01T05:00:00-05:00").toEntity().toDomain()
        assertEquals(Instant.parse("2026-03-01T10:00:00Z"), offset.settledAt)

        val garbage = minimalDto.copy(
            settledAt = "garbage",
            thinningBalance = ThinningBalanceDto(status = "EXECUTED_ON_TIME", executedDate = "nope"),
        ).toEntity().toDomain()
        assertEquals(Instant.EPOCH, garbage.settledAt)
        assertTrue(garbage.thinningBalance.executedDate == null)
    }

    @Test
    fun newReceiptFieldsAreParsedFromAFreshResponse() {
        val dto = json.decodeFromString<HarvestSettlementDto>(
            """{"id":"s","reportId":"r","plotId":"p","campaignYear":2026,"greenOlivesKg":1.0,"blackOlivesKg":2.0,
            "totalYieldKg":3.0,"receiptNumber":"VR-26-0042","weighedOn":"2026-10-03",
            "millTicketNumber":"T-9","commercialSizeGrade":"101/110"}""",
        )

        val settlement = dto.toDomain()

        assertEquals("VR-26-0042", settlement.receiptNumber)
        assertEquals(LocalDate.of(2026, 10, 3), settlement.weighedOn)
        assertEquals("T-9", settlement.millTicketNumber)
        assertEquals("101/110", settlement.commercialSizeGrade)
    }

    @Test
    fun absentReceiptFieldsMapToNullAndAnInvalidDateIsDropped() {
        val settlement = minimalDto.toDomain()
        assertNull(settlement.receiptNumber)
        assertNull(settlement.weighedOn)
        assertNull(settlement.millTicketNumber)
        assertNull(settlement.commercialSizeGrade)

        assertNull(minimalDto.copy(weighedOn = "03/10/2026").toDomain().weighedOn)
    }

    @Test
    fun cachedRowsMapReceiptFieldsToNull() {
        val settlement = minimalDto.copy(receiptNumber = "VR-26-0001").toEntity().toDomain()

        assertNull(settlement.receiptNumber)
        assertNull(settlement.weighedOn)
    }

    @Test
    fun requestBodyUsesIsoDateAndOmitsNullOptionals() {
        val draft = SettleHarvestDraft(
            plotId = "p-1",
            campaignYear = 2026,
            greenOlivesKg = 1200.5,
            blackOlivesKg = 800.0,
            weighedOn = LocalDate.of(2026, 10, 3),
        )

        val body = json.encodeToString(draft.toRequestDto())

        assertEquals(
            """{"campaignYear":2026,"greenOlivesKg":1200.5,"blackOlivesKg":800.0,"weighedOn":"2026-10-03"}""",
            body,
        )
        val full = json.encodeToString(
            draft.copy(millTicketNumber = "T-9", commercialFruitsPerKg = 105.0, notes = "ok").toRequestDto(),
        )
        assertTrue(full.contains(""""millTicketNumber":"T-9""""))
        assertTrue(full.contains(""""commercialFruitsPerKg":105.0"""))
        assertTrue(full.contains(""""notes":"ok""""))
    }

    @Test
    fun existingSettlementMapsToTheSummary() {
        val summary = ExistingSettlementDto(2026, 2000.5, "VR-26-0003", "2026-10-01").toDomain()

        assertEquals(SettlementSummary(2026, 2000.5, "VR-26-0003", LocalDate.of(2026, 10, 1)), summary)
        assertNull(ExistingSettlementDto(2026, 1.0).toDomain().receiptNumber)
    }
}
