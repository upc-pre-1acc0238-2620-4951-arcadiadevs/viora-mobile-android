package pe.edu.upc.viora.features.harvestsettlement.presentation

import java.time.LocalDate
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test
import pe.edu.upc.viora.features.harvestsettlement.presentation.ui.formatHash
import pe.edu.upc.viora.features.harvestsettlement.presentation.ui.formatWindow

class DossierFormatTest {

    private val spanish = Locale.forLanguageTag("es-PE")

    @Test
    fun `the whole code is written in four lines of four groups`() {
        val hash = "c7e109fa3b42d8e671a05c9d2e8b94f3" + "0123456789abcdef0123456789abcdef"

        assertEquals(
            "c7e1 09fa 3b42 d8e6\n71a0 5c9d 2e8b 94f3\n0123 4567 89ab cdef\n0123 4567 89ab cdef",
            formatHash(hash),
        )
    }

    @Test
    fun `a window inside one month writes the month once`() {
        assertEquals("3–30 nov", formatWindow(LocalDate.of(2026, 11, 3), LocalDate.of(2026, 11, 30), spanish))
    }

    @Test
    fun `a window across two months writes both`() {
        assertEquals("28 oct – 5 nov", formatWindow(LocalDate.of(2026, 10, 28), LocalDate.of(2026, 11, 5), spanish))
    }
}
