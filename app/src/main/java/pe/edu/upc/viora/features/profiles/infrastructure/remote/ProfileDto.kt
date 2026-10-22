package pe.edu.upc.viora.features.profiles.infrastructure.remote

import kotlinx.serialization.Serializable

/**
 * Backend resource returned by GET/POST/PATCH /api/v1/profiles (TS04, TS05, TS35).
 */
@Serializable
data class ProfileResource(
    val id: String? = null,
    val userId: String? = null,
    val fullName: String,
    val country: String,
    val phoneNumber: String,
    val createdAt: String? = null,
)

/** Request payload for initial profile creation (TS35 / US43). */
@Serializable
data class CreateProfileRequest(
    val fullName: String,
    val country: String,
    val phoneNumber: String,
)

/** Request payload for profile update (TS05 / US03). */
@Serializable
data class UpdateProfileRequest(
    val fullName: String,
    val country: String,
    val phoneNumber: String,
)

/** Request payload for password update (TS36 / US04). */
@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String,
)
