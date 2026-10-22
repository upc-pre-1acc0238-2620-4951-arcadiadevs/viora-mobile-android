package pe.edu.upc.viora.features.profiles.domain.entity

import pe.edu.upc.viora.features.iam.domain.valueobject.MobileNumbers
import pe.edu.upc.viora.features.profiles.domain.valueobject.ProfileId

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
    val initials: String
        get() {
            val words = fullName.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
            return when {
                words.isEmpty() -> "VI"
                words.size == 1 -> words[0].take(2).uppercase()
                else -> "${words[0].first().uppercaseChar()}${words[1].first().uppercaseChar()}"
            }
        }

    val formattedPhoneNumber: String
        get() = MobileNumbers.international(phoneNumber.removePrefix("+51").removePrefix("+"), country)
}
