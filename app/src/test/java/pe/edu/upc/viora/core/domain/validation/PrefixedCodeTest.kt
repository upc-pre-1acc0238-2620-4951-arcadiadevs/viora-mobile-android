package pe.edu.upc.viora.core.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PrefixedCodeTest {

    private val cooperative = PrefixedCode("COOP-", 4)
    private val extension = PrefixedCode("AMPL-", 4)

    @Test
    fun acceptsThePrefixPlusFourLettersOrDigits() {
        assertTrue(cooperative.isWellFormed("COOP-7K2Q"))
        assertTrue(extension.isWellFormed("AMPL-4D9X"))
        assertEquals(9, cooperative.length)
    }

    @Test
    fun normalizesWhatTheProducerTypes() {
        assertEquals("COOP-7K2Q", cooperative.canonicalOrNull(" coop- 7k2q "))
        assertTrue(cooperative.isWellFormed("coop-7k2q"))
    }

    @Test
    fun rejectsAnotherPrefixAWrongLengthOrSymbols() {
        assertFalse(cooperative.isWellFormed("AMPL-7K2Q"))
        assertFalse(cooperative.isWellFormed("COOP-7K2"))
        assertFalse(cooperative.isWellFormed("COOP-7K2QX"))
        assertFalse(cooperative.isWellFormed("COOP-7K-Q"))
        assertFalse(cooperative.isWellFormed(""))
        assertNull(cooperative.canonicalOrNull("COOP-"))
    }
}
