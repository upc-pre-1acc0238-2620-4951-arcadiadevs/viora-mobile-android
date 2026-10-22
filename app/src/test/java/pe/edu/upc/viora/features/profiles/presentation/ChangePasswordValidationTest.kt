package pe.edu.upc.viora.features.profiles.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.viora.features.profiles.presentation.state.ChangePasswordUiState

class ChangePasswordValidationTest {

    private fun state(current: String = "Olivo2025", new: String, confirm: String = new) =
        ChangePasswordUiState(currentPassword = current, newPassword = new, confirmPassword = confirm)

    @Test
    fun `US04 esc 1 - a new password of 8 or more letters and numbers, different and confirmed, can be sent`() {
        val ready = state(new = "Cosecha2026")

        assertTrue(ready.newPasswordValid)
        assertTrue(ready.canSubmit)
        assertEquals(4, ready.strength)
    }

    @Test
    fun `US04 esc 3 - shorter than 8 or without numbers is refused`() {
        assertFalse(state(new = "Abc123").newPasswordValid)
        assertFalse(state(new = "SoloLetras").newPasswordValid)
        assertFalse(state(new = "SoloLetras").canSubmit)
    }

    @Test
    fun `US04 esc 4 - the same as the current one is refused and judged right away`() {
        val same = state(current = "Olivo2025", new = "Olivo2025")

        assertFalse(same.ruleDifferentMet)
        assertFalse(same.canSubmit)
        assertTrue(same.showRuleErrors)
    }

    @Test
    fun `the rules are not judged while the first characters are typed`() {
        val typing = state(new = "Cos")

        assertFalse(typing.showRuleErrors)
        assertEquals(1, typing.strength)
    }

    @Test
    fun `a confirmation that does not match blocks sending and shows once it is as long`() {
        assertFalse(state(new = "Cosecha2026", confirm = "Cosecha").confirmMismatch)
        val mismatch = state(new = "Cosecha2026", confirm = "Cosecha2027")

        assertTrue(mismatch.confirmMismatch)
        assertFalse(mismatch.canSubmit)
    }
}
