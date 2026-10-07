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
}
