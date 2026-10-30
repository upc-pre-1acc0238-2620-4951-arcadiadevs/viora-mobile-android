package pe.edu.upc.viora.core.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EnumParsingTest {

    private enum class Level { LOW, HIGH, UNKNOWN }

    @Test
    fun findsAConstantIgnoringCaseAndSpaces() {
        assertEquals(Level.HIGH, enumOrNull<Level>("HIGH"))
        assertEquals(Level.HIGH, enumOrNull<Level>(" high "))
    }

    @Test
    fun anUnknownOrMissingNameIsNull() {
        assertNull(enumOrNull<Level>("MEDIUM"))
        assertNull(enumOrNull<Level>(""))
        assertNull(enumOrNull<Level>(null))
    }

    @Test
    fun aValueTheServerAddedLaterFallsBackToTheDefault() {
        assertEquals(Level.UNKNOWN, enumOrDefault("EXTREME", Level.UNKNOWN))
        assertEquals(Level.LOW, enumOrDefault("low", Level.UNKNOWN))
    }
}
