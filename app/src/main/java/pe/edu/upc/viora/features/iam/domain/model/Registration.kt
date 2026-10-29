package pe.edu.upc.viora.features.iam.domain.model

/**
 * What a producer signs up with: the credentials of the account and the profile answered in the
 * onboarding. [nationalPhone] is the mobile without the dial code of [countryIso].
 */
data class Registration(
    val email: String,
    val password: String,
    val fullName: String,
    val countryIso: String,
    val nationalPhone: String,
)
