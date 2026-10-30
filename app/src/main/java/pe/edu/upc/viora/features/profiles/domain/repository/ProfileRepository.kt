package pe.edu.upc.viora.features.profiles.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.profiles.domain.entity.UserProfile

interface ProfileRepository {

    /** The profile kept on the phone, so Mi cuenta and the Home greeting work offline. */
    fun observeProfile(): Flow<UserProfile?>

    /** Downloads the signed-in producer's profile and keeps it; offline, answers the kept one. */
    suspend fun refreshProfile(): AppResult<UserProfile>

    /**
     * Saves the full name and the mobile (US03). [nationalNumber] is the digits without the dial
     * code; it is sent in E.164 for [country]. Nothing is kept on the phone unless the server accepts it.
     */
    suspend fun updateProfile(fullName: String, country: String, nationalNumber: String): AppResult<UserProfile>
}
