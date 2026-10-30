package pe.edu.upc.viora.core.presentation

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class NumberFormatsTest {

    private val peru = Locale.forLanguageTag("es-PE")
    private val us = Locale.forLanguageTag("es-US")
    private val english = Locale.US

    @Test
    fun `spanish follows the figma convention whatever the region`() {
        listOf(peru, us).forEach { locale ->
            assertEquals("0,51", formatDecimal(0.514, 2, locale))
            assertEquals("12\u00A0500", formatWhole(12_500, locale))
            assertEquals("20\u00A0800,5", vioraNumberFormat(0, 1, locale).format(20_800.5))
        }
    }

    @Test
    fun `english keeps its own separators`() {
        assertEquals("0.51", formatDecimal(0.514, 2, english))
        assertEquals("12,500", formatWhole(12_500, english))
    }

    @Test
    fun `fraction digits are honoured`() {
        assertEquals("7", vioraNumberFormat(0, 1, peru).format(7.0))
        assertEquals("6,5", vioraNumberFormat(0, 1, peru).format(6.5))
        assertEquals("117,82", vioraNumberFormat(1, 2, peru).format(117.82))
        assertEquals("2,0", vioraNumberFormat(1, 2, peru).format(2.0))
    }

    @Test
    fun hectaresKeepOneOrTwoDecimalsEverywhere() {
        assertEquals("2,25", formatHectares(2.25, peru))
        assertEquals("2,5", formatHectares(2.5, peru))
        assertEquals("12,0", formatHectares(12.0, peru))
        assertEquals("117,82", formatHectares(117.82, peru))
        assertEquals("2.25", formatHectares(2.25, english))
    }

    @Test
    fun aPercentTakesItsValueIn0To100() {
        assertEquals("34\u00A0%", formatPercent(34.0, peru))
        assertEquals("34\u00A0%", formatPercent(34.4, peru))
    }

    @Test
    fun aRatioIsWrittenAsTheSamePercent() {
        assertEquals("32\u00A0%", formatPercentOf(0.32, peru))
        assertEquals(formatPercent(30.0, peru), formatPercentOf(0.30, peru))
    }

    @Test
    fun kilogramsSeparateThousandsAsTheLanguageDoes() {
        assertEquals("24\u00A0000", formatKg(24_000.0, peru))
        assertEquals("24,000", formatKg(24_000.0, english))
        assertEquals("7,8k", formatKgCompact(7_800.0, peru))
        assertEquals("950", formatKgCompact(950.0, peru))
    }

    @Test
    fun aTinyYieldKeepsTwoDecimals() {
        assertEquals("8,5", formatTonnesPerHectare(8.5, peru))
        assertEquals("0,02", formatTonnesPerHectare(0.02, peru))
        assertEquals("0,0", formatTonnesPerHectare(0.0, peru))
    }
}
