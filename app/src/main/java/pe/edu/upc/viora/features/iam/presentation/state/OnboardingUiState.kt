package pe.edu.upc.viora.features.iam.presentation.state

import pe.edu.upc.viora.features.iam.domain.valueobject.MobileNumbers

/** The onboarding questions in Figma order ("02 · Preguntas del onboarding"). */
enum class OnboardingStep(val number: Int?) {
    NAME(1),
    PHONE(2),
    COOPERATIVE_CODE(3),
    HECTARES(4),
    ALERTS(5),
    LOCATION(6),
    SUMMARY(null),
}

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.NAME,
    /** True while a question was opened from "Editar" on the summary: continuing returns there. */
    val editing: Boolean = false,

    val fullName: String = "",
    val country: IamCountry = IamCountry.PE,
    val phone: String = "",
    /** The phone field was left or submitted, so an invalid number may show its error. */
    val phoneTouched: Boolean = false,
    val cooperativeCode: String = "",
    /** Code kept with "Guardar código"; null after "No tengo código". */
    val savedCooperativeCode: String? = null,
    val hectares: Int = DEFAULT_HECTARES,
    /** "Aún no lo sé" was chosen on T02d. */
    val hectaresUnknown: Boolean = false,
    val alertsEnabled: Boolean = false,
    val locationGranted: Boolean = false,
) {
    val nameValid: Boolean get() = fullName.trim().count(Char::isLetter) >= MIN_NAME_LETTERS
    val phoneValid: Boolean get() = MobileNumbers.isValidMobile(phone, country.iso)
    val showPhoneError: Boolean get() = !phoneValid && (phoneTouched || phone.length >= country.nationalDigits)
    val cooperativeCodeValid: Boolean get() = cooperativeCode.trim().length >= MIN_CODE_LENGTH

    val phoneInternational: String get() = MobileNumbers.international(phone, country.iso)

    /** Plan Productor estimate of T02d: a reference price per hectare and month. */
    val monthlyPrice: Int get() = hectares * PRICE_PER_HECTARE_MONTH
    val yearlyPrice: Int get() = monthlyPrice * 12

    companion object {
        const val MIN_HECTARES = 1
        const val MAX_HECTARES = 999
        /** The ruler of T02d spans 0-40 ha; larger farms keep using the + button. */
        const val RULER_MAX_HECTARES = 40
        const val DEFAULT_HECTARES = 5
        /** Soles per hectare and month, the reference price shown in Figma (12 ha = S/ 660 al mes). */
        const val PRICE_PER_HECTARE_MONTH = 55
        const val MIN_NAME_LETTERS = 2
        const val MIN_CODE_LENGTH = 4
        const val MAX_CODE_LENGTH = 16
    }
}
