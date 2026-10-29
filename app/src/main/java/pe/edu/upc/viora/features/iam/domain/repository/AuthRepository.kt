package pe.edu.upc.viora.features.iam.domain.repository

import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.iam.domain.model.AuthResult
import pe.edu.upc.viora.features.iam.domain.model.Registration

/**
 * The producer's account: opening and closing its session, recovering and changing its password, and
 * deleting it. Opening a session also claims the data kept on the phone for that account.
 */
interface AuthRepository {

    /** Opens a session; another account's data on the phone is wiped first. */
    suspend fun signIn(email: String, password: String): AuthResult<Unit>

    /** Creates the account, which waits for its email code; answers the email the code went to. */
    suspend fun signUp(registration: Registration): AuthResult<String>

    /** Checks the email code and, when it is right, opens the session. */
    suspend fun verifyEmail(email: String, code: String): AuthResult<Unit>

    /** Sends a new email code. */
    suspend fun resendCode(email: String): AuthResult<Unit>

    /** Sends the password-recovery link to [email]. */
    suspend fun requestPasswordReset(email: String): AuthResult<Unit>

    /** Whether the recovery link with [token] still works. */
    suspend fun checkPasswordResetLink(token: String): AuthResult<Unit>

    /** Sets a new password through the recovery link; the server closes every session of the account. */
    suspend fun resetPassword(token: String, newPassword: String): AuthResult<Unit>

    /**
     * Changes the password after checking the current one (US04). The server closes every other session
     * and hands this phone a new one, which replaces the stored tokens. A wrong current password fails
     * with [WRONG_CURRENT_PASSWORD] as the validation code.
     */
    suspend fun changePassword(currentPassword: String, newPassword: String): AppResult<Unit>

    /** Closes the session on the server (best effort) and forgets it with every record of the account. */
    suspend fun signOut()

    /**
     * Deletes the account for good after checking [password] (P100). On success the phone forgets the
     * session and every local record of the account. A wrong password fails with [WRONG_CURRENT_PASSWORD]
     * as the validation code and deletes nothing; the shared demo account fails with [AppError.Forbidden].
     */
    suspend fun deleteAccount(password: String): AppResult<Unit>

    companion object {
        const val WRONG_CURRENT_PASSWORD = "WRONG_CURRENT_PASSWORD"
    }
}
