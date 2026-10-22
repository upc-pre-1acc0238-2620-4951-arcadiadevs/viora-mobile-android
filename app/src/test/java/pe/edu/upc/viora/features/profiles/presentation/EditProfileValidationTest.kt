package pe.edu.upc.viora.features.profiles.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.viora.features.iam.domain.valueobject.MobileNumbers
import pe.edu.upc.viora.features.iam.presentation.state.IamCountry
import pe.edu.upc.viora.features.profiles.presentation.state.EditProfileUiState

class EditProfileValidationTest {

    @Test
    fun `US03 esc 1 and US43 esc 1 - valid full name and phone enables saving with E164 normalization`() {
        val state = EditProfileUiState(
            fullName = "Rubén Ticona",
            country = IamCountry.PE,
            phone = "951406118",
            initialFullName = "Old Name",
            initialPhone = "987654321",
        )

        assertTrue(state.nameValid)
        assertFalse(state.nameEmpty)
        assertFalse(state.showNameError)

        assertTrue(state.phoneValid)
        assertFalse(state.showPhoneError)

        assertTrue(state.isModified)
        assertTrue(state.canSave)

        // E.164 normalization check
        assertEquals("+51951406118", MobileNumbers.e164(state.phone, state.country.iso))
    }

    @Test
    fun `US03 esc 2 and US43 esc 2 - phone with invalid length or prefix is rejected`() {
        // Less than 9 digits
        val incompletePhone = EditProfileUiState(
            fullName = "Rubén Ticona",
            country = IamCountry.PE,
            phone = "95140611",
            phoneTouched = true,
            initialFullName = "Old Name",
            initialPhone = "987654321",
        )
        assertFalse(incompletePhone.phoneValid)
        assertTrue(incompletePhone.showPhoneError)
        assertFalse(incompletePhone.canSave)

        // Does not start with 9 for Peru
        val wrongPrefixPhone = EditProfileUiState(
            fullName = "Rubén Ticona",
            country = IamCountry.PE,
            phone = "145678901",
            phoneTouched = true,
            initialFullName = "Old Name",
            initialPhone = "987654321",
        )
        assertFalse(wrongPrefixPhone.phoneValid)
        assertTrue(wrongPrefixPhone.showPhoneError)
        assertFalse(wrongPrefixPhone.canSave)
    }

    @Test
    fun `US03 esc 3 and US43 esc 3 - empty or blank full name is rejected`() {
        val emptyNameState = EditProfileUiState(
            fullName = "   ",
            fullNameTouched = true,
            country = IamCountry.PE,
            phone = "951406118",
            initialFullName = "Old Name",
            initialPhone = "987654321",
        )
        assertFalse(emptyNameState.nameValid)
        assertTrue(emptyNameState.nameEmpty)
        assertTrue(emptyNameState.showNameError)
        assertFalse(emptyNameState.canSave)
    }

    @Test
    fun `unmodified form cannot be saved`() {
        val unmodified = EditProfileUiState(
            fullName = "Rubén Ticona",
            country = IamCountry.PE,
            phone = "951406118",
            initialFullName = "Rubén Ticona",
            initialPhone = "951406118",
            initialCountry = IamCountry.PE,
        )
        assertTrue(unmodified.nameValid)
        assertTrue(unmodified.phoneValid)
        assertFalse(unmodified.isModified)
        assertFalse(unmodified.canSave)
    }

    @Test
    fun `US43 initial profile completion allows save when not pre-populated`() {
        val initialCompletion = EditProfileUiState(
            fullName = "Rubén Ticona",
            country = IamCountry.PE,
            phone = "951406118",
            isInitialCompletion = true,
        )
        assertTrue(initialCompletion.nameValid)
        assertTrue(initialCompletion.phoneValid)
        assertTrue(initialCompletion.canSave)
    }
}
