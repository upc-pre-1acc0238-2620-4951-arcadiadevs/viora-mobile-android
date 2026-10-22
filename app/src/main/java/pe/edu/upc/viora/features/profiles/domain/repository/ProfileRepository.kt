package pe.edu.upc.viora.features.profiles.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.profiles.domain.entity.UserProfile

interface ProfileRepository {
    fun observeProfile(): Flow<UserProfile?>
    suspend fun getProfile(userId: String): AppResult<UserProfile>

    suspend fun updateProfile(
        userId: String,
        fullName: String,
        country: String,
        phoneNumber: String,
    ): AppResult<UserProfile>

    /**
     * Completes initial profile for a new user with E.164 normalization (US43).
     */
    suspend fun createProfile(
        fullName: String,
        country: String,
        phoneNumber: String,
    ): AppResult<UserProfile>

    /**
     * Updates authentication password checking current password (US04).
     */
    suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String,
    ): AppResult<Unit>

    /** Signs out the user, clearing stored session and cached profile data. */
    suspend fun signOut()
}
