package pe.edu.upc.viora.features.iam.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.Clock
import pe.edu.upc.viora.core.datastore.SessionStore
import pe.edu.upc.viora.features.iam.infrastructure.remote.AuthSession
import pe.edu.upc.viora.features.iam.infrastructure.remote.IamService
import pe.edu.upc.viora.features.iam.infrastructure.remote.NewPasswordRequest
import pe.edu.upc.viora.features.iam.infrastructure.remote.PasswordResetRequest
import pe.edu.upc.viora.features.iam.infrastructure.remote.ResendCodeRequest
import pe.edu.upc.viora.features.iam.infrastructure.remote.SignInRequest
import pe.edu.upc.viora.features.iam.infrastructure.remote.SignUpProfile
import pe.edu.upc.viora.features.iam.infrastructure.remote.SignUpRequest
import pe.edu.upc.viora.features.iam.infrastructure.remote.VerifyEmailRequest
import pe.edu.upc.viora.features.iam.infrastructure.session.toStoredSession
import pe.edu.upc.viora.features.iam.presentation.state.CodeError
import pe.edu.upc.viora.features.iam.presentation.state.IamCountry
import pe.edu.upc.viora.features.iam.presentation.state.IamError
import pe.edu.upc.viora.features.iam.presentation.state.IamStep
import pe.edu.upc.viora.features.iam.presentation.state.IamUiState
import retrofit2.HttpException

private const val RESEND_COOLDOWN_SECONDS = 45
private const val CODE_LENGTH = 6

/** Extra members of the RFC 7807 body that the guide tells the app to branch on (section 3.4). */
@Serializable
private data class IamProblem(
    val code: String? = null,
    val reason: String? = null,
    val remainingAttempts: Int? = null,
    val retryAfterSeconds: Int? = null,
    val violations: List<String> = emptyList(),
)

private class Failure(val status: Int?, val problem: IamProblem?, val retryAfterHeader: Int?) {
    val isOffline: Boolean get() = status == null
    val retryAfter: Int? get() = problem?.retryAfterSeconds ?: retryAfterHeader
    fun mentions(prefix: String): Boolean =
        problem?.reason?.startsWith(prefix) == true || problem?.violations.orEmpty().any { it.startsWith(prefix) }
}

@HiltViewModel
class IamViewModel @Inject constructor(
    private val service: IamService,
    private val sessionStore: SessionStore,
    private val clock: Clock,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // The route arguments: `AuthRoute.createAccount` picks the first screen and
    // `ResetPasswordRoute.token` comes from the recovery link of the email.
    private val startsAtCreateAccount: Boolean = savedStateHandle["createAccount"] ?: false
    private val linkToken: String? = savedStateHandle["token"]

    private val _uiState = MutableStateFlow(
        IamUiState(step = if (startsAtCreateAccount) IamStep.CREATE_ACCOUNT else IamStep.SIGN_IN),
    )
    val uiState: StateFlow<IamUiState> = _uiState.asStateFlow()

    private val lenientJson = Json { ignoreUnknownKeys = true }
    private var countdownJob: Job? = null
    private var resetToken: String? = null

    init {
        linkToken?.let(::openResetLink)
    }

    // ---- Navigation between steps ---------------------------------------------------------

    fun goTo(step: IamStep) = _uiState.update {
        it.copy(step = step, error = null, notice = null, credentialsError = false, codeError = null)
    }

    /** Back arrow and system back. Returns false at the first screen so the screen can be left. */
    fun back(): Boolean {
        val target = when (_uiState.value.step) {
            IamStep.SIGN_IN, IamStep.CREATE_ACCOUNT -> return false
            IamStep.VERIFY -> IamStep.CREATE_ACCOUNT
            IamStep.FORGOT -> IamStep.SIGN_IN
            IamStep.LINK_SENT -> IamStep.FORGOT
            IamStep.NEW_PASSWORD, IamStep.CHECKING_LINK, IamStep.LINK_EXPIRED, IamStep.RESET_DONE -> IamStep.SIGN_IN
        }
        goTo(target)
        return true
    }

    /** "Inicia sesión" of the "correo ya registrado" error: sign-in with the email prefilled (T03). */
    fun goToSignInWithEmail() = _uiState.update {
        it.copy(step = IamStep.SIGN_IN, emailTaken = false, password = "", credentialsError = false, error = null)
    }

    // ---- Field edits (each one clears the error it may have caused) -----------------------

    fun onName(value: String) = _uiState.update { it.copy(fullName = value) }

    fun onCountry(value: IamCountry) = _uiState.update { it.copy(country = value, phoneRejectedByServer = false) }

    fun onPhone(value: String) =
        _uiState.update { it.copy(phone = value.filter(Char::isDigit).take(15), phoneRejectedByServer = false) }

    fun onEmail(value: String) = _uiState.update {
        it.copy(email = value, emailTaken = false, credentialsError = false, error = null)
    }

    fun onPassword(value: String) = _uiState.update {
        it.copy(password = value, passwordWeak = false, passwordReused = false, credentialsError = false, error = null)
    }

    fun onConfirmPassword(value: String) = _uiState.update { it.copy(confirmPassword = value, passwordReused = false) }

    fun onCode(value: String) {
        val digits = value.filter(Char::isDigit).take(CODE_LENGTH)
        _uiState.update { it.copy(code = digits, codeError = null, error = null) }
        if (digits.length == CODE_LENGTH) verify()
    }

    // ---- Sign in --------------------------------------------------------------------------

    fun signIn() {
        val s = _uiState.value
        if (!s.canSignIn || s.loading) return

        request {
            try {
                saveSession(service.signIn(SignInRequest(s.email.trim(), s.password)))
                _uiState.update { it.copy(authenticated = true) }
            } catch (e: Exception) {
                val failure = e.asFailure()
                when {
                    failure.status == 403 && failure.problem?.code == "EMAIL_VERIFICATION_FORBIDDEN" -> {
                        // Right password, unverified email: continue at T04a and send a fresh code.
                        _uiState.update {
                            it.copy(step = IamStep.VERIFY, pendingEmail = s.email.trim(), code = "", codeError = null)
                        }
                        resend()
                    }
                    failure.status == 403 -> _uiState.update { it.copy(credentialsError = true) }
                    else -> _uiState.update { it.copy(error = failure.toError()) }
                }
            }
        }
    }

    // ---- Sign up and email verification ---------------------------------------------------

    fun signUp() {
        val s = _uiState.value
        if (!s.canCreateAccount || s.loading) return
        request {
            try {
                val response = service.signUp(
                    SignUpRequest(
                        email = s.email.trim(),
                        password = s.password,
                        role = PRODUCER_ROLE,
                        termsAccepted = true,
                        profile = SignUpProfile(s.fullName.trim(), s.country.iso, s.country.e164(s.phone)),
                    ),
                )
                _uiState.update {
                    it.copy(step = IamStep.VERIFY, pendingEmail = response.email, code = "", codeError = null, notice = null)
                }
                startCountdown(RESEND_COOLDOWN_SECONDS)
            } catch (e: Exception) {
                val failure = e.asFailure()
                when {
                    failure.status == 409 -> _uiState.update { it.copy(emailTaken = true) }
                    failure.status == 400 && failure.mentions("profile.") ->
                        _uiState.update { it.copy(phoneRejectedByServer = true, error = IamError.GENERIC) }
                    failure.status == 400 -> _uiState.update { it.copy(passwordWeak = true) }
                    failure.status == 429 -> {
                        // The pending account already got a code less than 45 s ago: wait for it.
                        _uiState.update { it.copy(step = IamStep.VERIFY, pendingEmail = s.email.trim(), code = "") }
                        startCountdown(failure.retryAfter ?: RESEND_COOLDOWN_SECONDS)
                    }
                    else -> _uiState.update { it.copy(error = failure.toError()) }
                }
            }
        }
    }

    private fun verify() {
        val s = _uiState.value
        if (s.code.length != CODE_LENGTH || s.loading) return
        request {
            try {
                saveSession(service.verifyEmail(VerifyEmailRequest(s.pendingEmail, s.code)))
                // Onboarding data lives only until the email is verified (guide, section 3.1).
                _uiState.update {
                    it.copy(authenticated = true, password = "", confirmPassword = "", phone = "", fullName = "")
                }
            } catch (e: Exception) {
                val failure = e.asFailure()
                if (failure.status == 422) {
                    val codeError = when (failure.problem?.reason) {
                        "user.email_verification.invalid_code" -> CodeError.Wrong(failure.problem?.remainingAttempts)
                        "user.email_verification.attempts_exceeded" -> CodeError.Blocked
                        "user.email_verification.code_expired" -> CodeError.Expired
                        "user.email_verification.no_active_code" -> CodeError.Missing
                        else -> CodeError.Wrong(null)
                    }
                    // A dead code can be replaced right away, no need to wait out the 45 s.
                    if (codeError !is CodeError.Wrong) stopCountdown()
                    _uiState.update { it.copy(codeError = codeError) }
                } else {
                    _uiState.update { it.copy(error = failure.toError()) }
                }
            }
        }
    }

    fun resend() {
        val s = _uiState.value
        val target = s.pendingEmail.ifBlank { s.email.trim() }
        if (target.isBlank() || s.resendSeconds > 0) return
        viewModelScope.launch {
            try {
                service.resendCode(ResendCodeRequest(target))
                _uiState.update { it.copy(code = "", codeError = null, error = null, notice = IamUiState.Notice.CODE_RESENT) }
                startCountdown(RESEND_COOLDOWN_SECONDS)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                val failure = e.asFailure()
                if (failure.status == 429) {
                    startCountdown(failure.retryAfter ?: RESEND_COOLDOWN_SECONDS)
                } else {
                    _uiState.update { it.copy(error = failure.toError()) }
                }
            }
        }
    }

    // ---- Password recovery (T06-T09) ------------------------------------------------------

    fun requestResetLink(isResend: Boolean = false) {
        val s = _uiState.value
        if (!s.emailValid || s.loading) return
        request {
            try {
                service.requestPasswordReset(PasswordResetRequest(s.email.trim()))
                _uiState.update {
                    it.copy(step = IamStep.LINK_SENT, notice = if (isResend) IamUiState.Notice.LINK_RESENT else null)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.asFailure().toError()) }
            }
        }
    }

    /** Opens the app from `https://<domain>/password-resets/<token>`: checks the link before showing T08. */
    private fun openResetLink(token: String) {
        resetToken = token
        _uiState.update {
            it.copy(step = IamStep.CHECKING_LINK, password = "", confirmPassword = "", error = null, passwordReused = false, passwordWeak = false)
        }
        checkResetLink()
    }

    fun checkResetLink() {
        val token = resetToken ?: return _uiState.update { it.copy(step = IamStep.LINK_EXPIRED) }
        request {
            try {
                service.checkPasswordResetToken(token)
                _uiState.update { it.copy(step = IamStep.NEW_PASSWORD, error = null) }
            } catch (e: Exception) {
                val failure = e.asFailure()
                if (failure.status == 404) {
                    _uiState.update { it.copy(step = IamStep.LINK_EXPIRED) }
                } else {
                    _uiState.update { it.copy(error = failure.toError()) }
                }
            }
        }
    }

    fun saveNewPassword() {
        val s = _uiState.value
        val token = resetToken
        if (!s.newPasswordOk || s.loading || token == null) return
        request {
            try {
                service.resetPassword(token, NewPasswordRequest(s.password))
                // The account's sessions are closed server-side: back to sign in.
                resetToken = null
                _uiState.update { it.copy(step = IamStep.RESET_DONE, password = "", confirmPassword = "") }
            } catch (e: Exception) {
                val failure = e.asFailure()
                when {
                    failure.status == 404 -> _uiState.update { it.copy(step = IamStep.LINK_EXPIRED) }
                    failure.status == 422 && failure.problem?.reason == "user.password.reused" ->
                        _uiState.update { it.copy(passwordReused = true) }
                    failure.status == 400 -> _uiState.update { it.copy(passwordWeak = true) }
                    else -> _uiState.update { it.copy(error = failure.toError()) }
                }
            }
        }
    }

    // ---- Internals ------------------------------------------------------------------------

    private fun request(block: suspend () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            try {
                block()
            } finally {
                _uiState.update { it.copy(loading = false) }
            }
        }
    }

    private suspend fun saveSession(session: AuthSession) = sessionStore.save(session.toStoredSession(clock))

    private fun startCountdown(seconds: Int) {
        countdownJob?.cancel()
        _uiState.update { it.copy(resendSeconds = seconds) }
        countdownJob = viewModelScope.launch {
            var left = seconds
            while (left > 0) {
                delay(1_000)
                left--
                _uiState.update { it.copy(resendSeconds = left) }
            }
        }
    }

    private fun stopCountdown() {
        countdownJob?.cancel()
        _uiState.update { it.copy(resendSeconds = 0) }
    }

    private fun Failure.toError(): IamError = when {
        isOffline -> IamError.OFFLINE
        status == 429 -> IamError.WAIT
        else -> IamError.GENERIC
    }

    private fun Throwable.asFailure(): Failure {
        if (this is CancellationException) throw this
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

    private companion object {
        const val PRODUCER_ROLE = "ROLE_PRODUCER"
    }
}
