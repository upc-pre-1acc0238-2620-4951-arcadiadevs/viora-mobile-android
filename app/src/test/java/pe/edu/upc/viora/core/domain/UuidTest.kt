package pe.edu.upc.viora.core.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class UuidTest {

    private val canonical = "123e4567-e89b-42d3-a456-426614174000"

    @Test
    fun acceptsTheCanonicalLowerCaseForm() {
        assertEquals(canonical, Uuid.parseOrNull(canonical)?.value)
        assertTrue(Uuid.isValid(canonical))
    }

    @Test
    fun trimsAndLowerCasesWhatTheBackendMayEcho() {
        assertEquals(canonical, Uuid.parseOrNull("  ${canonical.uppercase()} ")?.value)
    }

    @Test
    fun rejectsAnythingThatIsNotAUuid() {
        listOf("", "   ", "plot-1", "123e4567e89b42d3a456426614174000", "123e4567-e89b-42d3-a456-42661417400",
            "123e4567-e89b-42d3-a456-4266141740zz", "${canonical}0").forEach {
            assertNull(it, Uuid.parseOrNull(it))
            assertFalse(it, Uuid.isValid(it))
        }
        assertNull(Uuid.parseOrNull(null))
        assertFalse(Uuid.isValid(null))
    }

    @Test
    fun parseNamesTheOffendingText() {
        try {
            Uuid.parse("plot-1")
            fail("a non-UUID must not parse")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message.orEmpty().contains("plot-1"))
        }
    }

    @Test
    fun randomUuidsAreValidAndDistinct() {
        val first = Uuid.random()
        val second = Uuid.random()
        assertTrue(Uuid.isValid(first.value))
        assertNotEquals(first, second)
    }
}
