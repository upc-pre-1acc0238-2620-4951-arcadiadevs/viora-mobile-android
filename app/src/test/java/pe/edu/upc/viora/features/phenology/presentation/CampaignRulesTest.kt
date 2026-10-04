package pe.edu.upc.viora.features.phenology.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.viora.features.phenology.presentation.state.CampaignRules
import pe.edu.upc.viora.features.phenology.presentation.state.YearCaption
import pe.edu.upc.viora.features.phenology.presentation.state.YearError

class CampaignRulesTest {

    @Test
    fun `years from 2000 to the current year are valid`() {
        assertNull(CampaignRules.yearError(2000, emptySet(), 2026))
        assertNull(CampaignRules.yearError(2026, emptySet(), 2026))
    }

    @Test
    fun `a future year is rejected`() {
        assertEquals(YearError.FUTURE, CampaignRules.yearError(2027, emptySet(), 2026))
    }

    @Test
    fun `a year before 2000 is rejected`() {
        assertEquals(YearError.TOO_OLD, CampaignRules.yearError(1999, emptySet(), 2026))
    }

    @Test
    fun `a registered year is a duplicate`() {
        assertEquals(YearError.DUPLICATE, CampaignRules.yearError(2024, setOf(2024, 2025), 2026))
    }

    @Test
    fun `kilos accept plain, grouped and decimal writings`() {
        assertEquals(20_500.0, CampaignRules.parseKilos("20500")!!, 0.0)
        assertEquals(20_500.0, CampaignRules.parseKilos("20 500")!!, 0.0)
        assertEquals(20_500.0, CampaignRules.parseKilos("20.500")!!, 0.0)
        assertEquals(20_500.0, CampaignRules.parseKilos("20,500")!!, 0.0)
        assertEquals(20_500.5, CampaignRules.parseKilos("20500,5")!!, 0.0)
        assertEquals(20_500.5, CampaignRules.parseKilos("20500.5")!!, 0.0)
        assertEquals(-500.0, CampaignRules.parseKilos("-500")!!, 0.0)
    }

    @Test
    fun `kilos that are not a number are null`() {
        assertNull(CampaignRules.parseKilos(""))
        assertNull(CampaignRules.parseKilos("  "))
        assertNull(CampaignRules.parseKilos("abc"))
        assertNull(CampaignRules.parseKilos("-"))
    }

    @Test
    fun `only kilos above zero are valid`() {
        assertTrue(CampaignRules.isValidKilos(0.5))
        assertFalse(CampaignRules.isValidKilos(0.0))
        assertFalse(CampaignRules.isValidKilos(-500.0))
        assertFalse(CampaignRules.isValidKilos(null))
    }

    @Test
    fun `the caption tells where the year falls among the registered ones`() {
        val registered = listOf(2022, 2023, 2024, 2025)

        assertEquals(YearCaption.BeforeOldest(2022), CampaignRules.yearCaption(2021, registered))
        assertEquals(YearCaption.AfterLatest(2025), CampaignRules.yearCaption(2026, registered))
        assertEquals(YearCaption.Between, CampaignRules.yearCaption(2023, registered))
        assertEquals(YearCaption.FirstEver, CampaignRules.yearCaption(2024, emptyList()))
    }

    @Test
    fun `adding 2021 with 10500 kg moves the Figma index from 0,51 to 0,48`() {
        val preview = CampaignRules.previewAdd(figmaRecords(), currentIndex = 0.51, year = 2021, kilos = 10_500.0)

        assertEquals(0.51, preview.before!!, 0.0)
        assertEquals(0.48, preview.after!!, 0.0)
        assertEquals(5, preview.campaignsAfter)
    }

    @Test
    fun `correcting 2024 to 20500 kg gives 0,49 and recalculates the two intervals around it`() {
        val preview = CampaignRules.previewCorrect(figmaRecords(), currentIndex = 0.51, year = 2024, kilos = 20_500.0)

        assertEquals(0.49, preview.after!!, 0.0)
        assertEquals(listOf(2023 to 2024, 2024 to 2025), preview.intervals.map { it.fromYear to it.toYear })
    }

    @Test
    fun `deleting the oldest of five campaigns returns to 0,51`() {
        val records = listOf(harvest(2021, 10_500.0)) + figmaRecords()

        val preview = CampaignRules.previewDelete(records, currentIndex = 0.48, year = 2021)

        assertEquals(0.48, preview.before!!, 0.0)
        assertEquals(0.51, preview.after!!, 0.0)
        assertEquals(4, preview.campaignsAfter)
    }

    @Test
    fun `deleting down to two campaigns leaves no index`() {
        val preview = CampaignRules.previewDelete(figmaRecords().take(3), currentIndex = 0.5, year = 2024)

        assertNull(preview.after)
        assertNull(preview.afterClass)
        assertEquals(2, preview.campaignsAfter)
    }

    @Test
    fun `adding the third campaign gives the first index`() {
        val preview = CampaignRules.previewAdd(figmaRecords().take(2), currentIndex = null, year = 2021, kilos = 10_500.0)

        assertNull(preview.before)
        assertEquals(3, preview.campaignsAfter)
        assertTrue(preview.after != null)
    }
}
