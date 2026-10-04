package pe.edu.upc.viora.features.phenology.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pe.edu.upc.viora.features.phenology.domain.entity.BbiClass
import pe.edu.upc.viora.features.phenology.domain.entity.BearingYear
import pe.edu.upc.viora.features.phenology.presentation.state.Headline
import pe.edu.upc.viora.features.phenology.presentation.state.HarvestPresenter
import pe.edu.upc.viora.features.phenology.presentation.state.Outlook
import pe.edu.upc.viora.features.phenology.presentation.state.Voice

class HarvestPresenterTest {

    @Test
    fun `severe alternation after an OFF-ending history says the next year is likely ON`() {
        val summary = HarvestPresenter.summarize(figmaRecords(), BbiClass.SEVERE, currentYear = 2026)

        assertEquals(Headline.STRONG, summary.headline)
        assertEquals(Outlook.ON_LIKELY, summary.outlook)
        assertEquals(2026, summary.nextYear)
        assertEquals(Voice.Recover(2026), summary.voice)
    }

    @Test
    fun `an ON last campaign warns that the next one is likely OFF`() {
        val records = figmaRecords().drop(1)

        val summary = HarvestPresenter.summarize(records.dropLast(1), BbiClass.SEVERE, currentYear = 2026)

        assertEquals(Outlook.OFF_LIKELY, summary.outlook)
        assertEquals(Voice.BreakCycle(2025), summary.voice)
    }

    @Test
    fun `moderate alternation gets the moderate headline`() {
        val summary = HarvestPresenter.summarize(figmaRecords(), BbiClass.MODERATE, currentYear = 2026)

        assertEquals(Headline.MODERATE, summary.headline)
    }

    @Test
    fun `a low index is steady with no drop card and no warning`() {
        val summary = HarvestPresenter.summarize(figmaRecords(), BbiClass.LOW, currentYear = 2026)

        assertEquals(Headline.STEADY, summary.headline)
        assertEquals(Outlook.STEADY, summary.outlook)
        assertEquals(Voice.Steady, summary.voice)
        assertNull(summary.dropAfterOn)
    }

    @Test
    fun `an even last campaign is steady even when the alternation is severe`() {
        val records = figmaRecords().dropLast(1) + harvest(2025, 15_000.0, BearingYear.BALANCED)

        assertEquals(Outlook.STEADY, HarvestPresenter.summarize(records, BbiClass.SEVERE, 2026).outlook)
    }

    @Test
    fun `drop after ON is the most recent ON to OFF pair`() {
        val drop = HarvestPresenter.summarize(figmaRecords(), BbiClass.SEVERE, 2026).dropAfterOn

        assertEquals(22_000.0, drop!!.fromKg, 0.0)
        assertEquals(6_750.0, drop.afterKg, 0.0)
        assertEquals(6_750.0 / 22_000.0, drop.ratio, 1e-9)
    }

    @Test
    fun `no ON to OFF pair means no drop card`() {
        val records = listOf(
            harvest(2023, 10_000.0, BearingYear.BALANCED),
            harvest(2024, 11_000.0, BearingYear.ON),
            harvest(2025, 12_000.0, BearingYear.ON),
        )

        assertNull(HarvestPresenter.summarize(records, BbiClass.MODERATE, 2026).dropAfterOn)
    }

    @Test
    fun `fewer than three campaigns count what is missing and ask for the year before the oldest`() {
        val two = HarvestPresenter.summarize(figmaRecords().takeLast(2), null, currentYear = 2026)
        val none = HarvestPresenter.summarize(emptyList(), null, currentYear = 2026)

        assertEquals(Headline.MISSING, two.headline)
        assertEquals(1, two.missing)
        assertEquals(Voice.AskYear(2023), two.voice)
        assertEquals(3, none.missing)
        assertEquals(Voice.AskYear(2025), none.voice)
        assertNull(none.outlook)
    }

    @Test
    fun `suggested year is one before the oldest campaign or last year`() {
        assertEquals(2021, HarvestPresenter.suggestedYear(figmaRecords(), currentYear = 2026))
        assertEquals(2025, HarvestPresenter.suggestedYear(emptyList(), currentYear = 2026))
    }

    @Test
    fun `the explainer example is the oldest interval with its Hoblyn value`() {
        val example = HarvestPresenter.example(figmaRecords())!!

        assertEquals(2022, example.fromYear)
        assertEquals(2023, example.toYear)
        assertEquals(16_250.0, example.differenceKg, 0.0)
        assertEquals(31_750.0, example.sumKg, 0.0)
        assertEquals(0.51, example.value, 0.0)
        assertNull(HarvestPresenter.example(figmaRecords().take(1)))
    }
}
