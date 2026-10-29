package pe.edu.upc.viora.features.iam.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.viora.core.domain.MobileNumbers

class MobileNumbersTest {

    @Test
    fun `a Peruvian mobile starting with 9 is valid`() {
        assertTrue(MobileNumbers.isValidMobile("987654321", "PE"))
    }

    @Test
    fun `incomplete numbers and Lima landlines are refused`() {
        assertFalse(MobileNumbers.isValidMobile("98765", "PE"))
        assertFalse(MobileNumbers.isValidMobile("14567890", "PE"))
    }

    @Test
    fun `the number must belong to the chosen country`() {
        assertTrue(MobileNumbers.isValidMobile("612345678", "ES"))
        assertFalse(MobileNumbers.isValidMobile("612345678", "PE"))
    }

    @Test
    fun `formats for the backend and for reading`() {
        assertEquals("+51987654321", MobileNumbers.e164("987654321", "PE"))
        assertEquals("+51 987 654 321", MobileNumbers.international("987654321", "PE"))
    }
}
