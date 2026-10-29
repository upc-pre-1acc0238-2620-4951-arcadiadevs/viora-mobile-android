package pe.edu.upc.viora.features.profiles.presentation.state

import pe.edu.upc.viora.features.iam.domain.valueobject.MobileNumbers
import pe.edu.upc.viora.features.iam.presentation.state.IamCountry
import pe.edu.upc.viora.core.domain.validation.PersonName

/** Why saving failed, beyond the fields (P96). */
enum class SaveProfileError { OFFLINE, GENERIC }

/** P96 · Datos personales (US03): full name and mobile; the email is read-only. */
data class EditProfileUiState(
    val fullName: String = "",
    val fullNameTouched: Boolean = false,
    val country: IamCountry = IamCountry.PE,
    val phone: String = "",
    val phoneTouched: Boolean = false,
    val email: String = "",
    val initialFullName: String = "",
    val initialPhone: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val attemptedSave: Boolean = false,
    val error: SaveProfileError? = null,
) {
    /** The same rule as the onboarding: at least two letters, so a lone initial is not saved as a name. */
    val nameInvalid: Boolean get() = !PersonName.isValid(fullName)
    val showNameError: Boolean get() = (fullNameTouched || attemptedSave) && nameInvalid

    val phoneValid: Boolean get() = MobileNumbers.isValidMobile(phone, country.iso)

    /** Shown once the number is complete or after trying to save, never while typing the first digits. */
    val showPhoneError: Boolean get() = (attemptedSave || phone.length >= country.nationalDigits) && !phoneValid

    val isModified: Boolean get() = fullName.trim() != initialFullName.trim() || phone != initialPhone

    val canSave: Boolean get() = !nameInvalid && phoneValid && isModified && !isLoading

    /** How the number reads abroad, under the field (`+51 952 184 337`). */
    val internationalPhone: String get() = MobileNumbers.international(phone, country.iso)
}
