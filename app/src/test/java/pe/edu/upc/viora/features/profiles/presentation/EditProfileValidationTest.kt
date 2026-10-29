package pe.edu.upc.viora.features.profiles.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.viora.core.presentation.PhoneCountry
import pe.edu.upc.viora.features.profiles.presentation.state.EditProfileUiState

class EditProfileValidationTest {

    private val saved = EditProfileUiState(
        fullName = "Teodoro Mamani",
        initialFullName = "Teodoro Mamani",
        country = PhoneCountry.PE,
        phone = "952184337",
        initialPhone = "952184337",
    )

    @Test
    fun `US03 esc 1 - a changed name with a valid Peruvian mobile can be saved`() {
        val edited = saved.copy(fullName = "Teodoro Mamani Quispe")

        assertTrue(edited.canSave)
        assertEquals("+51 952 184 337", edited.internationalPhone)
    }

    @Test
    fun `US03 esc 2 - a mobile with missing digits or not starting with 9 is refused`() {
        val short = saved.copy(phone = "95218433")
        assertFalse(short.canSave)
        // While typing it waits; after trying to save it says so.
        assertFalse(short.showPhoneError)
        assertTrue(short.copy(attemptedSave = true).showPhoneError)

        val landline = saved.copy(phone = "152184337")
        assertFalse(landline.canSave)
        assertTrue(landline.showPhoneError)
    }

    @Test
    fun `US03 esc 3 - an empty name is refused and the error shows once touched`() {
        val blank = saved.copy(fullName = "   ", fullNameTouched = true)

        assertFalse(blank.canSave)
        assertTrue(blank.showNameError)
    }

    @Test
    fun `an unchanged form cannot be saved`() {
        assertFalse(saved.isModified)
        assertFalse(saved.canSave)
    }
}
