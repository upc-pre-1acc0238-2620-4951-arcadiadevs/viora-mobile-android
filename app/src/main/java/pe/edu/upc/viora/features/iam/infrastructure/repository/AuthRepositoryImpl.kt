package pe.edu.upc.viora.features.iam.infrastructure.repository

import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import pe.edu.upc.viora.core.database.LocalDataOwner
import pe.edu.upc.viora.core.datastore.SessionStore
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.network.ApiCaller
import pe.edu.upc.viora.features.iam.domain.model.AuthError
import pe.edu.upc.viora.features.iam.domain.model.AuthResult
import pe.edu.upc.viora.features.iam.domain.model.Registration
import pe.edu.upc.viora.features.iam.domain.repository.AuthRepository
import pe.edu.upc.viora.features.iam.domain.valueobject.MobileNumbers
import pe.edu.upc.viora.features.iam.infrastructure.remote.AccountService
import pe.edu.upc.viora.features.iam.infrastructure.remote.AuthSession
import pe.edu.upc.viora.features.iam.infrastructure.remote.ChangePasswordRequest
import pe.edu.upc.viora.features.iam.infrastructure.remote.DeleteAccountRequest
import pe.edu.upc.viora.features.iam.infrastructure.remote.IamService
import pe.edu.upc.viora.features.iam.infrastructure.remote.NewPasswordRequest
import pe.edu.upc.viora.features.iam.infrastructure.remote.PasswordResetRequest
import pe.edu.upc.viora.features.iam.infrastructure.remote.RefreshTokenRequest
import pe.edu.upc.viora.features.iam.infrastructure.remote.ResendCodeRequest
import pe.edu.upc.viora.features.iam.infrastructure.remote.SignInRequest
import pe.edu.upc.viora.features.iam.infrastructure.remote.SignUpProfile
import pe.edu.upc.viora.features.iam.infrastructure.remote.SignUpRequest
import pe.edu.upc.viora.features.iam.infrastructure.remote.VerifyEmailRequest
import pe.edu.upc.viora.features.iam.infrastructure.session.toStoredSession
import retrofit2.HttpException

/**
 * The account against the IAM endpoints. The public ones (sign-in, sign-up, codes, recovery) read the
 * RFC 7807 members the guide tells the app to branch on (`reason`, `remainingAttempts`, `violations`,
 * `Retry-After`), which the shared error mapper does not keep; the ones that need the session go
 * through [ApiCaller] like any other feature.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    /** Public endpoints and sign-out: no token goes out and no renewal is ever triggered. */
    private val iamService: IamService,
    private val accountService: AccountService,
    private val apiCaller: ApiCaller,
    private val sessionStore: SessionStore,
    private val localDataOwner: LocalDataOwner,
    private val clock: Clock,
) : AuthRepository {

    private val lenientJson = Json { ignoreUnknownKeys = true }

    override suspend fun signIn(email: String, password: String): AuthResult<Unit> =
        attempt(
            call = { saveSession(iamService.signIn(SignInRequest(email, password))) },
            onFailure = { failure ->
                when {
                    // Right password, unverified email: the screen continues at T04a and sends a fresh code.
                    failure.status == 403 && failure.problem?.code == "EMAIL_VERIFICATION_FORBIDDEN" -> AuthError.EmailNotVerified
                    failure.status == 403 -> AuthError.InvalidCredentials
                    else -> failure.generic()
                }
            },
        )

    override suspend fun signUp(registration: Registration): AuthResult<String> =
        attempt(
            call = {
                iamService.signUp(
                    SignUpRequest(
                        email = registration.email.trim(),
                        password = registration.password,
                        role = PRODUCER_ROLE,
                        // The terms are accepted on the onboarding summary ("Al continuar aceptas..."), the
                        // only way to reach the sign-up, so there is no second checkbox to read here.
                        termsAccepted = true,
                        profile = SignUpProfile(
                            fullName = registration.fullName.trim(),
                            country = registration.countryIso,
                            phoneNumber = MobileNumbers.e164(registration.nationalPhone, registration.countryIso),
                        ),
                    ),
                ).email
            },
            onFailure = { failure ->
                when {
                    failure.status == 409 -> AuthError.EmailTaken
                    failure.status == 400 && failure.mentions("profile.") -> AuthError.PhoneRejected
                    failure.status == 400 -> AuthError.WeakPassword
                    // The pending account already got a code less than the cooldown ago: wait for it.
                    failure.status == 429 -> AuthError.CodeNotYetResendable(failure.retryAfter)
                    else -> failure.generic()
                }
            },
        )

    override suspend fun verifyEmail(email: String, code: String): AuthResult<Unit> =
        attempt(
            call = { saveSession(iamService.verifyEmail(VerifyEmailRequest(email, code))) },
            onFailure = { failure ->
                if (failure.status == 422) {
                    when (failure.problem?.reason) {
                        "user.email_verification.invalid_code" -> AuthError.InvalidCode(failure.problem?.remainingAttempts)
                        "user.email_verification.attempts_exceeded" -> AuthError.CodeAttemptsExceeded
                        "user.email_verification.code_expired" -> AuthError.CodeExpired
                        "user.email_verification.no_active_code" -> AuthError.NoActiveCode
                        else -> AuthError.InvalidCode(null)
                    }
                } else {
                    failure.generic()
                }
            },
        )

    override suspend fun resendCode(email: String): AuthResult<Unit> =
        attempt(
            call = { iamService.resendCode(ResendCodeRequest(email)) },
            onFailure = { failure ->
                if (failure.status == 429) AuthError.CodeNotYetResendable(failure.retryAfter) else failure.generic()
            },
        )

    override suspend fun requestPasswordReset(email: String): AuthResult<Unit> =
        attempt(call = { iamService.requestPasswordReset(PasswordResetRequest(email)) }, onFailure = { it.generic() })

    override suspend fun checkPasswordResetLink(token: String): AuthResult<Unit> =
        attempt(
            call = { iamService.checkPasswordResetToken(token) },
            onFailure = { failure -> if (failure.status == 404) AuthError.ResetLinkExpired else failure.generic() },
        )

    override suspend fun resetPassword(token: String, newPassword: String): AuthResult<Unit> =
        attempt(
            call = { iamService.resetPassword(token, NewPasswordRequest(newPassword)) },
            onFailure = { failure ->
                when {
                    failure.status == 404 -> AuthError.ResetLinkExpired
                    failure.status == 422 && failure.problem?.reason == "user.password.reused" -> AuthError.PasswordReused
                    failure.status == 400 -> AuthError.WeakPassword
                    else -> failure.generic()
                }
            },
        )

    override suspend fun changePassword(currentPassword: String, newPassword: String): AppResult<Unit> {
        val request = ChangePasswordRequest(currentPassword = currentPassword, newPassword = newPassword)
        return when (val result = apiCaller.call { accountService.changePassword(request) }) {
            is AppResult.Success -> {
                // Every session was closed, this one too: only the new tokens keep the phone signed in.
                sessionStore.save(result.value.toStoredSession(clock))
                AppResult.Success(Unit)
            }
            is AppResult.Failure -> result.wrongPasswordTold()
        }
    }

    override suspend fun signOut() {
        // The server forgets the refresh token; offline it simply expires. Either way the phone forgets it.
        sessionStore.refreshToken()?.let { refreshToken ->
            apiCaller.callUnit { iamService.signOut(RefreshTokenRequest(refreshToken)) }
        }
        sessionStore.clear()
        // Nothing of the farm stays on the phone for whoever uses it next (the kept profile included).
        localDataOwner.release()
    }

    override suspend fun deleteAccount(password: String): AppResult<Unit> =
        when (val result = apiCaller.callUnit { accountService.deleteAccount(DeleteAccountRequest(password)) }) {
            is AppResult.Success -> {
                // The server has nothing left of the account: the phone forgets its session and every record too.
                sessionStore.clear()
                localDataOwner.release()
                result
            }
            is AppResult.Failure -> result.wrongPasswordTold()
        }

    // ---- Internals ------------------------------------------------------------------------

    /** Another account signing in first wipes the data the phone kept for the previous one. */
    private suspend fun saveSession(session: AuthSession) {
        localDataOwner.claimFor(session.userId)
        sessionStore.save(session.toStoredSession(clock))
    }

    private suspend fun <T> attempt(call: suspend () -> T, onFailure: (Failure) -> AuthError): AuthResult<T> =
        try {
            AuthResult.Success(call())
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            AuthResult.Failure(onFailure(throwable.asFailure()))
        }

    /** The only business rule the password check can break while signed in is the password itself. */
    private fun AppResult.Failure.wrongPasswordTold(): AppResult.Failure {
        val error = error
        return if (error is AppError.Validation && error.code == BUSINESS_RULE_VIOLATION) {
            AppResult.Failure(AppError.Validation(error.detail, AuthRepository.WRONG_CURRENT_PASSWORD))
        } else {
            this
        }
    }

    private fun Throwable.asFailure(): Failure {
        if (this is HttpException) {
            val raw = runCatching { response()?.errorBody()?.string() }.getOrNull()
            val problem = raw?.takeIf { it.isNotBlank() }?.let {
                runCatching { lenientJson.decodeFromString<IamProblem>(it) }.getOrNull()
            }
            val header = response()?.headers()?.get("Retry-After")?.toIntOrNull()
            return Failure(code(), problem, header)
        }
        // Not an HTTP answer: no connection, timeout... (IOException) or an unexpected body.
        return Failure(null, null, null)
    }

    /** Extra members of the RFC 7807 body that the guide tells the app to branch on (section 3.4). */
    @Serializable
    private data class IamProblem(
        val code: String? = null,
        val reason: String? = null,
        val remainingAttempts: Int? = null,
        val retryAfterSeconds: Int? = null,
        val violations: List<String> = emptyList(),
    )

    private class Failure(val status: Int?, val problem: IamProblem?, private val retryAfterHeader: Int?) {
        val retryAfter: Int? get() = problem?.retryAfterSeconds ?: retryAfterHeader

        fun mentions(prefix: String): Boolean =
            problem?.reason?.startsWith(prefix) == true || problem?.violations.orEmpty().any { it.startsWith(prefix) }

        fun generic(): AuthError = when {
            status == null -> AuthError.Offline
            status == 429 -> AuthError.TooManyRequests
            else -> AuthError.Unexpected
        }
    }

    private companion object {
        const val PRODUCER_ROLE = "ROLE_PRODUCER"
        const val BUSINESS_RULE_VIOLATION = "BUSINESS_RULE_VIOLATION"
    }
}
