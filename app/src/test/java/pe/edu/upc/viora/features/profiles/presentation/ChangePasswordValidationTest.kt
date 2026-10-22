package pe.edu.upc.viora.features.profiles.presentation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.viora.features.profiles.presentation.state.ChangePasswordUiState

class ChangePasswordValidationTest {

    @Test
    fun `US04 esc 1 - valid current and new password with alphanumeric enables submit`() {
        val state = ChangePasswordUiState(
            currentPassword = "OldPassword1",
            newPassword = "NewPassword2",
            confirmPassword = "NewPassword2",
        )
        assertTrue(state.ruleLengthMet)
        assertTrue(state.ruleAlphanumericMet)
        assertTrue(state.ruleDifferentMet)
        assertTrue(state.newPasswordValid)
        assertTrue(state.confirmMatches)
        assertTrue(state.canSubmit)
    }

    @Test
    fun `US04 esc 3 - new password with less than 8 characters or no digits is rejected`() {
        // Less than 8 characters
        val shortPassword = ChangePasswordUiState(
            currentPassword = "OldPassword1",
            newPassword = "Short1",
            confirmPassword = "Short1",
            newPasswordTouched = true,
        )
        assertFalse(shortPassword.ruleLengthMet)
        assertFalse(shortPassword.newPasswordValid)
        assertFalse(shortPassword.canSubmit)

        // No digits
        val noDigits = ChangePasswordUiState(
            currentPassword = "OldPassword1",
            newPassword = "OnlyLettersHere",
            confirmPassword = "OnlyLettersHere",
            newPasswordTouched = true,
        )
        assertFalse(noDigits.ruleAlphanumericMet)
        assertFalse(noDigits.newPasswordValid)
        assertFalse(noDigits.canSubmit)
    }

    @Test
    fun `US04 esc 4 - new password identical to current password is rejected`() {
        val identicalPassword = ChangePasswordUiState(
            currentPassword = "OldPassword1",
            newPassword = "OldPassword1",
            confirmPassword = "OldPassword1",
            newPasswordTouched = true,
        )
        assertFalse(identicalPassword.ruleDifferentMet)
        assertFalse(identicalPassword.newPasswordValid)
        assertFalse(identicalPassword.canSubmit)
    }

    @Test
    fun `confirmation mismatch blocks submit`() {
        val mismatch = ChangePasswordUiState(
            currentPassword = "OldPassword1",
            newPassword = "NewPassword2",
            confirmPassword = "DifferentPassword3",
        )
        assertTrue(mismatch.newPasswordValid)
        assertFalse(mismatch.confirmMatches)
        assertFalse(mismatch.canSubmit)
    }
}
