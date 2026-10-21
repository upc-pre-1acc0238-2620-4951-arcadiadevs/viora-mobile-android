package pe.edu.upc.viora.features.iam.domain.valueobject

/**
 * What the producer answered in the onboarding questions (Figma T02b-T02e2). They stay on the
 * phone until the email is verified (guide 02, option B): name, country and mobile become the
 * profile at sign-up; the rest waits for the plan and alerts features.
 */
data class OnboardingAnswers(
    val fullName: String = "",
    /** ISO-3166-1 alpha-2 country of the operation. */
    val countryIso: String = DEFAULT_COUNTRY,
    /** Mobile number without the country code, digits only. */
    val nationalPhone: String = "",
    /** Cooperative code typed in T02c; null when the producer has none. */
    val cooperativeCode: String? = null,
    /** Hectares of olive in T02d; null for "Aún no lo sé" or when a code covers the plan. */
    val hectares: Int? = null,
    /** Null until T02e is answered. */
    val alertsEnabled: Boolean? = null,
    /** Null until T02e2 is answered. */
    val locationGranted: Boolean? = null,
) {
    val firstName: String get() = fullName.trim().substringBefore(' ')

    companion object {
        const val DEFAULT_COUNTRY = "PE"
    }
}
