package pe.edu.upc.viora.core.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonNameTest {

    @Test
    fun aNameNeedsAtLeastTwoLetters() {
        assertTrue(PersonName.isValid("Teodoro Mamani"))
        assertTrue(PersonName.isValid("  Li  "))
        assertFalse(PersonName.isValid("A"))
        assertFalse(PersonName.isValid("12 34"))
        assertFalse(PersonName.isValid("   "))
    }

    @Test
    fun theFirstNameIsTheTextBeforeTheFirstSpace() {
        assertEquals("Teodoro", PersonName.firstName("  Teodoro Mamani Quispe "))
        assertEquals("Teodoro", PersonName.firstName("Teodoro"))
        assertEquals("", PersonName.firstName("   "))
    }
}
