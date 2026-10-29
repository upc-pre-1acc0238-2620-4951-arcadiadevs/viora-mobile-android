package pe.edu.upc.viora.core.domain.validation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordPolicyTest {

    @Test
    fun needsEightCharactersWithLettersAndDigits() {
        assertTrue(PasswordPolicy.isValid("abcdef12"))
        assertFalse("too short", PasswordPolicy.isValid("abc123"))
        assertFalse("no digits", PasswordPolicy.isValid("abcdefgh"))
        assertFalse("no letters", PasswordPolicy.isValid("12345678"))
        assertFalse(PasswordPolicy.isValid(""))
    }

    @Test
    fun eachRuleCanBeJudgedOnItsOwnForTheLiveChecklist() {
        assertTrue(PasswordPolicy.isLongEnough("abcdefgh"))
        assertFalse(PasswordPolicy.hasLettersAndDigits("abcdefgh"))
        assertTrue(PasswordPolicy.hasLettersAndDigits("a1"))
    }

    @Test
    fun aNewPasswordMustNotRepeatTheCurrentOne() {
        assertTrue(PasswordPolicy.differsFromCurrent("nueva123", "vieja123"))
        assertFalse(PasswordPolicy.differsFromCurrent("misma123", "misma123"))
        assertFalse(PasswordPolicy.differsFromCurrent("", "vieja123"))
    }

    @Test
    fun theConfirmationMustBeFilledInAndEqual() {
        assertTrue(PasswordPolicy.isConfirmed("abcdef12", "abcdef12"))
        assertFalse(PasswordPolicy.isConfirmed("abcdef12", "abcdef13"))
        assertFalse(PasswordPolicy.isConfirmed("", ""))
    }
}
