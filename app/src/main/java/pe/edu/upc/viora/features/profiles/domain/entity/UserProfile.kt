package pe.edu.upc.viora.features.profiles.domain.entity

import pe.edu.upc.viora.features.iam.domain.valueobject.MobileNumbers
import pe.edu.upc.viora.features.profiles.domain.valueobject.ProfileId

/**
 * Civil identity and contact profile of an authenticated user (Productor Olivarero or Gestor Técnico).
 * Governed by US03 and US43 with E.164 phone normalization.
 */
data class UserProfile(
    val id: ProfileId,
    val userId: String,
    val fullName: String,
    val country: String,
    val phoneNumber: String,
    val email: String? = null,
    val role: String? = null,
    val organization: String? = null,
    val licenseActive: Boolean = true,
) {
    /** Initials for avatar representation (e.g. "RT" for "Rubén Ticona"). */
    val initials: String
        get() {
            val words = fullName.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
            return when {
                words.isEmpty() -> "VI"
                words.size == 1 -> words[0].take(2).uppercase()
                else -> "${words[0].first().uppercaseChar()}${words[1].first().uppercaseChar()}"
            }
        }

    /** Formatted international mobile number for display ("+51 951 406 118"). */
    val formattedPhoneNumber: String
        get() = MobileNumbers.international(phoneNumber.removePrefix("+51").removePrefix("+"), country)
}
