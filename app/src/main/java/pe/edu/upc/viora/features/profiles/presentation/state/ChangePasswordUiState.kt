package pe.edu.upc.viora.features.profiles.presentation.state

data class ChangePasswordUiState(
    val currentPassword: String = "",
    val currentPasswordTouched: Boolean = false,
    val newPassword: String = "",
    val newPasswordTouched: Boolean = false,
    val confirmPassword: String = "",
    val confirmPasswordTouched: Boolean = false,
    val currentPasswordIncorrect: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
) {
    val ruleLengthMet: Boolean get() = newPassword.length >= 8
    val ruleAlphanumericMet: Boolean get() = newPassword.any(Char::isLetter) && newPassword.any(Char::isDigit)
    val ruleDifferentMet: Boolean get() = newPassword.isNotEmpty() && newPassword != currentPassword

    val newPasswordValid: Boolean get() = ruleLengthMet && ruleAlphanumericMet && ruleDifferentMet
    val confirmMatches: Boolean get() = confirmPassword.isNotEmpty() && confirmPassword == newPassword

    val canSubmit: Boolean get() =
        currentPassword.isNotEmpty() && newPasswordValid && confirmMatches && !isLoading

    val showNewPasswordError: Boolean get() =
        newPasswordTouched && newPassword.isNotEmpty() && !newPasswordValid
}
