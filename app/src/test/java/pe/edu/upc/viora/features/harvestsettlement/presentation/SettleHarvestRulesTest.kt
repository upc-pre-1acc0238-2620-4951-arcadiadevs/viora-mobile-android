package pe.edu.upc.viora.features.harvestsettlement.presentation

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.CalibreReading
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.CommercialSizeGrade
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.SettleHarvestRules

class SettleHarvestRulesTest {

    @Test
    fun `blank kilos count as zero and numbers are read like the campaign sheet`() {
        assertEquals(0.0, SettleHarvestRules.kilos("")!!, 0.0)
        assertEquals(12_500.0, SettleHarvestRules.kilos("12 500")!!, 0.0)
        assertEquals(8_300.5, SettleHarvestRules.kilos("8300,5")!!, 0.0)
        assertNull(SettleHarvestRules.kilos("abc"))
    }

    @Test
    fun `negative and unreadable kilos are errors`() {
        assertTrue(SettleHarvestRules.kilosError("-300"))
        assertTrue(SettleHarvestRules.kilosError("-"))
        assertFalse(SettleHarvestRules.kilosError(""))
        assertFalse(SettleHarvestRules.kilosError("0"))
        assertFalse(SettleHarvestRules.kilosError("12500"))
    }

    @Test
    fun `the total adds both qualities and is null while one is invalid`() {
        assertEquals(20_800.0, SettleHarvestRules.totalKilos("12 500", "8 300")!!, 0.0)
        assertEquals(8_300.0, SettleHarvestRules.totalKilos("", "8300")!!, 0.0)
        assertNull(SettleHarvestRules.totalKilos("-300", "8300"))
        assertNull(SettleHarvestRules.totalKilos("12500", "x"))
    }

    @Test
    fun `a zero total cannot be settled`() {
        assertFalse(SettleHarvestRules.isValidTotal(0.0))
        assertFalse(SettleHarvestRules.isValidTotal(null))
        assertTrue(SettleHarvestRules.isValidTotal(0.5))
    }

    @Test
    fun `the green share is the green over the total`() {
        assertEquals(0.6, SettleHarvestRules.greenShare(12_480.0, 8_320.0)!!, 1e-9)
        assertEquals(1.0, SettleHarvestRules.greenShare(500.0, 0.0)!!, 0.0)
        assertNull(SettleHarvestRules.greenShare(0.0, 0.0))
    }

    @Test
    fun `a future weighing date is rejected and today is fine`() {
        val today = LocalDate.of(2026, 10, 5)

        assertTrue(SettleHarvestRules.isFuture(today.plusDays(1), today))
        assertFalse(SettleHarvestRules.isFuture(today, today))
        assertFalse(SettleHarvestRules.isFuture(today.minusDays(40), today))
    }

    @Test
    fun `the date picker round trips a local date through UTC millis`() {
        val date = LocalDate.of(2026, 5, 14)

        assertEquals(date, SettleHarvestRules.fromPickerMillis(SettleHarvestRules.toPickerMillis(date)))
    }

    @Test
    fun `the mill ticket is trimmed, blank means none and thirty characters is the limit`() {
        assertNull(SettleHarvestRules.millTicket("   "))
        assertEquals("B-004512", SettleHarvestRules.millTicket(" B-004512 "))
        assertTrue(SettleHarvestRules.isMillTicketValid("x".repeat(30)))
        assertFalse(SettleHarvestRules.isMillTicketValid("x".repeat(31)))
    }

    @Test
    fun `a chosen grade sends its midpoint and wins over typed text`() {
        val grade = CommercialSizeGrade(101, 110)

        assertEquals(CalibreReading.Value(105.5), SettleHarvestRules.calibre(grade, "999"))
    }

    @Test
    fun `a typed calibre is a positive count up to 999 with dot or comma`() {
        assertEquals(CalibreReading.None, SettleHarvestRules.calibre(null, ""))
        assertEquals(CalibreReading.Value(105.0), SettleHarvestRules.calibre(null, "105"))
        assertEquals(CalibreReading.Value(105.5), SettleHarvestRules.calibre(null, "105,5"))
        assertEquals(CalibreReading.Invalid, SettleHarvestRules.calibre(null, "0"))
        assertEquals(CalibreReading.Invalid, SettleHarvestRules.calibre(null, "1000"))
        assertEquals(CalibreReading.Invalid, SettleHarvestRules.calibre(null, "1,2,3"))
    }

    @Test
    fun `the grade scale mirrors the backend table and every midpoint falls in its own grade`() {
        val scale = CommercialSizeGrade.SCALE

        assertEquals(17, scale.size)
        assertEquals("60/70", scale.first().label)
        assertEquals("101/110", scale[4].label)
        assertEquals("381/410", scale.last().label)
        scale.zipWithNext { a, b -> assertEquals(a.upper + 1, b.lower) }
        scale.forEach { grade ->
            val rounded = Math.round(grade.midpoint).toInt()
            assertTrue("${grade.label} midpoint ${grade.midpoint}", rounded in grade.lower..grade.upper)
        }
    }
}
