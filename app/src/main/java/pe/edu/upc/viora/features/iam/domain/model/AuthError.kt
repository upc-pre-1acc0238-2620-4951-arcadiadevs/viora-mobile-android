package pe.edu.upc.viora.features.iam.domain.model

/**
 * Why an account operation failed, in the terms the sign-in and recovery screens react to. The
 * infrastructure reads them from the RFC 7807 body (guide 02, section 3.4); screens never see HTTP.
 */
sealed interface AuthError {
    /** The server could not be reached: no connection, a timeout or an unreadable answer. */
    data object Offline : AuthError

    /** Too many attempts in a short time: wait and try again. */
    data object TooManyRequests : AuthError

    /** Anything else the screens show as a generic failure. */
    data object Unexpected : AuthError

    /** Wrong email or password. */
    data object InvalidCredentials : AuthError

    /** Right password, but the email was never verified: a new code is on its way. */
    data object EmailNotVerified : AuthError

    /** The email already has an account. */
    data object EmailTaken : AuthError

    /** The server refused the mobile number of the profile. */
    data object PhoneRejected : AuthError

    /** The password does not meet the server's rules. */
    data object WeakPassword : AuthError

    /** The new password repeats a recent one. */
    data object PasswordReused : AuthError

    /** A code was sent less than the cooldown ago; [retryAfterSeconds] says how long to wait, when known. */
    data class CodeNotYetResendable(val retryAfterSeconds: Int?) : AuthError

    /** The email code is wrong; [remainingAttempts] are left before it locks, when the server says. */
    data class InvalidCode(val remainingAttempts: Int?) : AuthError

    /** Too many wrong codes: this one no longer works. */
    data object CodeAttemptsExceeded : AuthError

    /** The code expired. */
    data object CodeExpired : AuthError

    /** There is no code waiting for this email. */
    data object NoActiveCode : AuthError

    /** The password-recovery link expired, was used or was replaced by a newer one. */
    data object ResetLinkExpired : AuthError
}

/** Typed outcome of an account operation that can fail with an [AuthError]. */
sealed interface AuthResult<out T> {
    data class Success<out T>(val value: T) : AuthResult<T>
    data class Failure(val error: AuthError) : AuthResult<Nothing>
}
