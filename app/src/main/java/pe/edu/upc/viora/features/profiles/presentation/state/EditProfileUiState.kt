package pe.edu.upc.viora.features.profiles.presentation.state

import pe.edu.upc.viora.features.iam.domain.valueobject.MobileNumbers
import pe.edu.upc.viora.features.iam.presentation.state.IamCountry

data class EditProfileUiState(
    val fullName: String = "",
    val fullNameTouched: Boolean = false,
    val country: IamCountry = IamCountry.PE,
    val phone: String = "",
    val phoneTouched: Boolean = false,
    val email: String = "",
    val initialFullName: String = "",
    val initialPhone: String = "",
    val initialCountry: IamCountry = IamCountry.PE,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val attemptedSave: Boolean = false,
    val errorMessage: String? = null,
    val isInitialCompletion: Boolean = false,
) {
    val nameValid: Boolean get() = fullName.trim().count(Char::isLetter) >= 2
    val nameEmpty: Boolean get() = fullName.trim().isEmpty()
    val showNameError: Boolean get() = (fullNameTouched || attemptedSave) && nameEmpty

    val phoneValid: Boolean get() = MobileNumbers.isValidMobile(phone.filter(Char::isDigit), country.iso)
    val showPhoneError: Boolean get() = (phoneTouched || attemptedSave || phone.filter(Char::isDigit).length >= country.nationalDigits) && !phoneValid

    val isModified: Boolean get() =
        fullName.trim() != initialFullName.trim() ||
        phone.filter(Char::isDigit) != initialPhone.filter(Char::isDigit) ||
        country != initialCountry

    val canSave: Boolean get() = nameValid && phoneValid && (isModified || isInitialCompletion) && !isLoading

    val formattedInternational: String get() = MobileNumbers.international(phone.filter(Char::isDigit), country.iso)
}
