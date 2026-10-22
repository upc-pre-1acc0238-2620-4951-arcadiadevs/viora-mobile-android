package pe.edu.upc.viora.features.profiles.domain.entity

import pe.edu.upc.viora.features.iam.domain.valueobject.MobileNumbers
import pe.edu.upc.viora.features.profiles.domain.valueobject.ProfileId

/**
 * The producer's civil profile (US03): created at sign-up, edited from Mi cuenta. [phoneNumber] is
 * E.164 (`+51952184337`); [email] comes from the session, since the profile does not hold it.
 */
data class UserProfile(
    val id: ProfileId,
    val userId: String,
    val fullName: String,
    val country: String,
    val phoneNumber: String,
    val email: String? = null,
) {
    private val words: List<String> get() = fullName.trim().split(Regex("\\s+")).filter { it.isNotBlank() }

    val initials: String
        get() = when (words.size) {
            0 -> "VI"
            1 -> words[0].take(2).uppercase()
            else -> "${words[0].first().uppercaseChar()}${words[1].first().uppercaseChar()}"
        }

    /** The national digits, without the country's dial code (`952184337`). */
    val nationalNumber: String get() = MobileNumbers.national(phoneNumber, country)

    /** As the account shows it: `+51 952 184 337`. */
    val formattedPhoneNumber: String
        get() = MobileNumbers.international(nationalNumber, country)
}
