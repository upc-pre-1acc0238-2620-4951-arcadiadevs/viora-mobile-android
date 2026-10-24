package pe.edu.upc.viora.features.profiles.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.core.domain.AppError
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

    /**
     * Changes the password after checking the current one (US04). The server closes every other
     * session and hands this phone a new one, which replaces the stored tokens. A wrong current
     * password fails with [WRONG_CURRENT_PASSWORD] as the validation code.
     */
    suspend fun changePassword(currentPassword: String, newPassword: String): AppResult<Unit>

    /** Whether the producer's access (plan or cooperative membership) is active today. */
    suspend fun hasActiveMembership(): AppResult<Boolean>

    /** Closes the session on the server (best effort) and forgets the tokens and the kept profile. */
    suspend fun signOut()

    /**
     * Deletes the account for good after checking [password] (P100). On success the phone forgets the session, the
     * profile and every local record of the account. A wrong password fails with [WRONG_CURRENT_PASSWORD] as the
     * validation code and deletes nothing; the shared demo account fails with [AppError.Forbidden].
     */
    suspend fun deleteAccount(password: String): AppResult<Unit>

    companion object {
        const val WRONG_CURRENT_PASSWORD = "WRONG_CURRENT_PASSWORD"
    }
}
