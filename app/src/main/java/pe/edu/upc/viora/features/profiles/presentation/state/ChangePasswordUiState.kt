package pe.edu.upc.viora.features.profiles.presentation.state

import pe.edu.upc.viora.core.domain.validation.PasswordPolicy
/** Why the change failed, beyond the fields (P97). */
enum class ChangePasswordError { OFFLINE, GENERIC }

/** P97 · Cambiar contraseña (US04): 8 or more characters, letters and numbers, different from the current one. */
data class ChangePasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val currentPasswordIncorrect: Boolean = false,
    /** The server's reason when it refuses the new password (for example, one of the last five). */
    val newPasswordRejection: String? = null,
    val isLoading: Boolean = false,
    val isChanged: Boolean = false,
    val error: ChangePasswordError? = null,
) {
    val ruleLengthMet: Boolean get() = PasswordPolicy.isLongEnough(newPassword)
    val ruleAlphanumericMet: Boolean get() = PasswordPolicy.hasLettersAndDigits(newPassword)
    val ruleDifferentMet: Boolean get() = PasswordPolicy.differsFromCurrent(newPassword, currentPassword)

    val newPasswordValid: Boolean get() = ruleLengthMet && ruleAlphanumericMet && ruleDifferentMet

    /** How many of the four segments of the strength bar fill: one per rule, the fourth when all pass. */
    val strength: Int get() = when {
        newPassword.isEmpty() -> 0
        newPasswordValid -> 4
        else -> listOf(ruleLengthMet, ruleAlphanumericMet, ruleDifferentMet).count { it }.coerceAtLeast(1)
    }

    /** The rules are judged once the new password reaches the minimum length, not on the first key. */
    val showRuleErrors: Boolean get() = newPassword.length >= PasswordPolicy.MIN_LENGTH || (newPassword.isNotEmpty() && !ruleDifferentMet)

    val confirmMismatch: Boolean get() = confirmPassword.isNotEmpty() && confirmPassword != newPassword &&
        confirmPassword.length >= newPassword.length

    val canSubmit: Boolean get() =
        currentPassword.isNotEmpty() && newPasswordValid && confirmPassword == newPassword && !isLoading
}
