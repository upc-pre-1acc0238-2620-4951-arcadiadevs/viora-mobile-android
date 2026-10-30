package pe.edu.upc.viora.features.iam.presentation.state

import java.math.BigDecimal
import pe.edu.upc.viora.core.domain.MobileNumbers
import pe.edu.upc.viora.core.domain.validation.PersonName
import pe.edu.upc.viora.core.presentation.PhoneCountry
import pe.edu.upc.viora.features.subscription.application.published.InvitationCode

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
    val country: PhoneCountry = PhoneCountry.PE,
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
    /** Plan Productor price per hectare and month from the plan catalog; null until it is known. */
    val referenceRatePerHa: BigDecimal? = null,
    val currency: String = DEFAULT_CURRENCY,
) {
    val nameValid: Boolean get() = PersonName.isValid(fullName)
    val phoneValid: Boolean get() = MobileNumbers.isValidMobile(phone, country.iso)
    val showPhoneError: Boolean get() = !phoneValid && (phoneTouched || phone.length >= country.nationalDigits)
    /** The same format the redemption screen and the backend accept (COOP-XXXX), so a typo is caught here. */
    val cooperativeCodeValid: Boolean get() = InvitationCode.isWellFormed(cooperativeCode)

    val phoneInternational: String get() = MobileNumbers.international(phone, country.iso)

    /** Plan Productor estimate of T02d at the catalog's price; null while the catalog is unknown. */
    val monthlyPrice: BigDecimal? get() = referenceRatePerHa?.multiply(hectares.toBigDecimal())
    val yearlyPrice: BigDecimal? get() = monthlyPrice?.multiply(MONTHS_PER_YEAR.toBigDecimal())

    companion object {
        const val MIN_HECTARES = 1
        const val MAX_HECTARES = 999
        /** The ruler of T02d spans 0-40 ha; larger farms keep using the + button. */
        const val RULER_MAX_HECTARES = 40
        const val DEFAULT_HECTARES = 5
        const val DEFAULT_CURRENCY = "PEN"
        const val MONTHS_PER_YEAR = 12
        const val MAX_CODE_LENGTH = 16
    }
}
