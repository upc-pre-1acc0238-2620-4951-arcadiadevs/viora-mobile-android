package pe.edu.upc.viora.features.profiles.infrastructure.remote

import kotlinx.serialization.Serializable

@Serializable
data class ProfileResource(
    val id: String? = null,
    val userId: String? = null,
    val fullName: String,
    val country: String,
    val phoneNumber: String,
)

@Serializable
data class UpdateProfileRequest(
    val fullName: String,
    val country: String,
    val phoneNumber: String,
)

@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
)

/** The part of the current subscription the account needs: whether access is active today. */
@Serializable
data class CurrentSubscriptionResource(
    val status: String? = null,
    val entitlementActive: Boolean = false,
)
