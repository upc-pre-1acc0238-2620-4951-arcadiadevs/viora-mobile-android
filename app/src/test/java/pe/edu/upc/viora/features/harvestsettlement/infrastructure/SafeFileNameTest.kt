package pe.edu.upc.viora.features.harvestsettlement.infrastructure

import org.junit.Assert.assertEquals
import org.junit.Test
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.repository.safeFileName

/** A dossier name from the server can never write outside the dossiers folder. */
class SafeFileNameTest {

    @Test
    fun keepsAPlainName() {
        assertEquals("expediente-2026.pdf", safeFileName("expediente-2026.pdf"))
    }

    @Test
    fun dropsFoldersAndParentReferences() {
        assertEquals("evil.pdf", safeFileName("../../shared_prefs/evil.pdf"))
        assertEquals("evil.pdf", safeFileName("..\\..\\evil.pdf"))
        assertEquals("dossier.pdf", safeFileName(".."))
    }
}
