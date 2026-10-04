package pe.edu.upc.viora.features.phenology.domain

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pe.edu.upc.viora.features.phenology.domain.entity.BearingYear
import pe.edu.upc.viora.features.phenology.domain.entity.HarvestRecord
import pe.edu.upc.viora.features.phenology.domain.entity.HoblynBbi

class HoblynBbiTest {

    private fun record(year: Int, kg: Double) = HarvestRecord(
        id = "r-$year",
        plotId = "p-1",
        campaignYear = year,
        totalYieldKg = kg,
        greenKg = null,
        blackKg = null,
        bearing = BearingYear.UNKNOWN,
        recordedAt = Instant.EPOCH,
    )

    private val figma = listOf(
        record(2022, 24000.0),
        record(2023, 7750.0),
        record(2024, 22000.0),
        record(2025, 6750.0),
    )

    @Test
    fun intervalsMatchTheFigmaExample() {
        val intervals = HoblynBbi.intervals(figma)
        assertEquals(listOf(2022 to 2023, 2023 to 2024, 2024 to 2025), intervals.map { it.fromYear to it.toYear })
        assertEquals(listOf(0.51, 0.48, 0.53), intervals.map { it.value })
    }

    @Test
    fun indexMatchesTheFigmaExample() {
        assertEquals(0.51, HoblynBbi.index(figma)!!, 0.0)
    }

    @Test
    fun indexIsNullWithFewerThanThreeCampaigns() {
        assertNull(HoblynBbi.index(figma.take(2)))
        assertNull(HoblynBbi.index(emptyList()))
    }

    @Test
    fun unsortedInputIsSortedByCampaignYear() {
        assertEquals(0.51, HoblynBbi.index(figma.reversed())!!, 0.0)
        assertEquals(2022, HoblynBbi.intervals(figma.shuffled()).first().fromYear)
    }

    @Test
    fun zeroSumPairsAreSkipped() {
        val records = listOf(record(2022, 0.0), record(2023, 0.0), record(2024, 100.0), record(2025, 0.0))
        val intervals = HoblynBbi.intervals(records)
        assertEquals(listOf(2023 to 2024, 2024 to 2025), intervals.map { it.fromYear to it.toYear })
        assertEquals(1.0, HoblynBbi.index(records)!!, 0.0)
    }

    @Test
    fun indexIsNullWhenEveryPairIsSkipped() {
        assertNull(HoblynBbi.index(listOf(record(2022, 0.0), record(2023, 0.0), record(2024, 0.0))))
    }
}
