package pe.edu.upc.viora.features.harvestsettlement.presentation

import org.junit.Assert.assertEquals
import org.junit.Test
import pe.edu.upc.viora.features.harvestsettlement.presentation.ui.formatHash

class DossierFormatTest {

    @Test
    fun `the whole code is written in four lines of four groups`() {
        val hash = "c7e109fa3b42d8e671a05c9d2e8b94f3" + "0123456789abcdef0123456789abcdef"

        assertEquals(
            "c7e1 09fa 3b42 d8e6\n71a0 5c9d 2e8b 94f3\n0123 4567 89ab cdef\n0123 4567 89ab cdef",
            formatHash(hash),
        )
    }
}
