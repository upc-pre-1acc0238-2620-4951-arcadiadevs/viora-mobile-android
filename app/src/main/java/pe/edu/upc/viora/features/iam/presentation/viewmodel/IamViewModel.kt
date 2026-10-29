package pe.edu.upc.viora.features.iam.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.viora.features.iam.application.usecase.CheckPasswordResetLinkUseCase
import pe.edu.upc.viora.features.iam.application.usecase.RequestPasswordResetUseCase
import pe.edu.upc.viora.features.iam.application.usecase.ResendCodeUseCase
import pe.edu.upc.viora.features.iam.application.usecase.ResetPasswordUseCase
import pe.edu.upc.viora.features.iam.application.usecase.SignInUseCase
import pe.edu.upc.viora.features.iam.application.usecase.SignUpUseCase
import pe.edu.upc.viora.features.iam.application.usecase.VerifyEmailUseCase
import pe.edu.upc.viora.features.iam.domain.model.AuthError
import pe.edu.upc.viora.features.iam.domain.model.AuthResult
import pe.edu.upc.viora.features.iam.domain.model.Registration
import pe.edu.upc.viora.features.iam.domain.repository.OnboardingAnswersRepository
import pe.edu.upc.viora.features.iam.presentation.state.CodeError
import pe.edu.upc.viora.features.iam.presentation.state.IamError
import pe.edu.upc.viora.features.iam.presentation.state.IamStep
import pe.edu.upc.viora.features.iam.presentation.state.IamUiState

private const val RESEND_COOLDOWN_SECONDS = 45
private const val CODE_LENGTH = 6

@HiltViewModel
class IamViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val verifyEmailUseCase: VerifyEmailUseCase,
    private val resendCodeUseCase: ResendCodeUseCase,
    private val requestPasswordReset: RequestPasswordResetUseCase,
    private val checkPasswordResetLink: CheckPasswordResetLinkUseCase,
    private val resetPassword: ResetPasswordUseCase,
    private val onboardingStore: OnboardingAnswersRepository,
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

    private var countdownJob: Job? = null
    private var resetToken: String? = null

    init {
        linkToken?.let(::openResetLink)
        viewModelScope.launch {
            val firstName = onboardingStore.answers().firstName
            _uiState.update { it.copy(firstName = firstName) }
        }
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
            when (val result = signInUseCase(s.email, s.password)) {
                is AuthResult.Success -> _uiState.update { it.copy(authenticated = true) }
                is AuthResult.Failure -> when (result.error) {
                    AuthError.EmailNotVerified -> {
                        // Right password, unverified email: continue at T04a and send a fresh code.
                        _uiState.update {
                            it.copy(step = IamStep.VERIFY, pendingEmail = s.email.trim(), code = "", codeError = null)
                        }
                        resend()
                    }
                    AuthError.InvalidCredentials -> _uiState.update { it.copy(credentialsError = true) }
                    else -> _uiState.update { it.copy(error = result.error.toIamError()) }
                }
            }
        }
    }

    // ---- Sign up and email verification ---------------------------------------------------

    fun signUp() {
        val s = _uiState.value
        if (!s.canCreateAccount || s.loading) return
        request {
            val answers = onboardingStore.answers()
            val registration = Registration(
                email = s.email,
                password = s.password,
                fullName = answers.fullName,
                countryIso = answers.countryIso,
                nationalPhone = answers.nationalPhone,
            )
            when (val result = signUpUseCase(registration)) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(step = IamStep.VERIFY, pendingEmail = result.value, code = "", codeError = null, notice = null)
                    }
                    startCountdown(RESEND_COOLDOWN_SECONDS)
                }
                is AuthResult.Failure -> when (val error = result.error) {
                    AuthError.EmailTaken -> _uiState.update { it.copy(emailTaken = true) }
                    AuthError.PhoneRejected -> _uiState.update { it.copy(error = IamError.PHONE_REJECTED) }
                    AuthError.WeakPassword -> _uiState.update { it.copy(passwordWeak = true) }
                    is AuthError.CodeNotYetResendable -> {
                        // The pending account already got a code less than 45 s ago: wait for it.
                        _uiState.update { it.copy(step = IamStep.VERIFY, pendingEmail = s.email.trim(), code = "") }
                        startCountdown(error.retryAfterSeconds ?: RESEND_COOLDOWN_SECONDS)
                    }
                    else -> _uiState.update { it.copy(error = error.toIamError()) }
                }
            }
        }
    }

    private fun verify() {
        val s = _uiState.value
        if (s.code.length != CODE_LENGTH || s.loading) return
        request {
            when (val result = verifyEmailUseCase(s.pendingEmail, s.code)) {
                is AuthResult.Success -> {
                    // Onboarding answers live only until the email is verified (guide, section 3.1); the
                    // cooperative code goes on to P05, which redeems it now that there is a session, and
                    // without a code the hectares of T02d go on to P02, the Plan Productor to pay.
                    val answers = onboardingStore.answers()
                    onboardingStore.clear()
                    _uiState.update {
                        it.copy(authenticated = true, cooperativeCodeToRedeem = answers.cooperativeCode,
                            buysPlan = answers.cooperativeCode == null, planHectares = answers.hectares,
                            password = "", confirmPassword = "")
                    }
                }
                is AuthResult.Failure -> {
                    val codeError = when (val error = result.error) {
                        is AuthError.InvalidCode -> CodeError.Wrong(error.remainingAttempts)
                        AuthError.CodeAttemptsExceeded -> CodeError.Blocked
                        AuthError.CodeExpired -> CodeError.Expired
                        AuthError.NoActiveCode -> CodeError.Missing
                        else -> null
                    }
                    if (codeError != null) {
                        // A dead code can be replaced right away, no need to wait out the 45 s.
                        if (codeError !is CodeError.Wrong) stopCountdown()
                        _uiState.update { it.copy(codeError = codeError) }
                    } else {
                        _uiState.update { it.copy(error = result.error.toIamError()) }
                    }
                }
            }
        }
    }

    fun resend() {
        val s = _uiState.value
        val target = s.pendingEmail.ifBlank { s.email.trim() }
        if (target.isBlank() || s.resendSeconds > 0) return
        viewModelScope.launch {
            when (val result = resendCodeUseCase(target)) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(code = "", codeError = null, error = null, notice = IamUiState.Notice.CODE_RESENT) }
                    startCountdown(RESEND_COOLDOWN_SECONDS)
                }
                is AuthResult.Failure -> when (val error = result.error) {
                    is AuthError.CodeNotYetResendable -> startCountdown(error.retryAfterSeconds ?: RESEND_COOLDOWN_SECONDS)
                    else -> _uiState.update { it.copy(error = error.toIamError()) }
                }
            }
        }
    }

    // ---- Password recovery (T06-T09) ------------------------------------------------------

    fun requestResetLink(isResend: Boolean = false) {
        val s = _uiState.value
        if (!s.emailValid || s.loading) return
        request {
            when (val result = requestPasswordReset(s.email)) {
                is AuthResult.Success -> _uiState.update {
                    it.copy(step = IamStep.LINK_SENT, notice = if (isResend) IamUiState.Notice.LINK_RESENT else null)
                }
                is AuthResult.Failure -> _uiState.update { it.copy(error = result.error.toIamError()) }
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
            when (val result = checkPasswordResetLink(token)) {
                is AuthResult.Success -> _uiState.update { it.copy(step = IamStep.NEW_PASSWORD, error = null) }
                is AuthResult.Failure -> if (result.error == AuthError.ResetLinkExpired) {
                    _uiState.update { it.copy(step = IamStep.LINK_EXPIRED) }
                } else {
                    _uiState.update { it.copy(error = result.error.toIamError()) }
                }
            }
        }
    }

    fun saveNewPassword() {
        val s = _uiState.value
        val token = resetToken
        if (!s.newPasswordOk || s.loading || token == null) return
        request {
            when (val result = resetPassword(token, s.password)) {
                is AuthResult.Success -> {
                    // The account's sessions are closed server-side: back to sign in.
                    resetToken = null
                    _uiState.update { it.copy(step = IamStep.RESET_DONE, password = "", confirmPassword = "") }
                }
                is AuthResult.Failure -> when (result.error) {
                    AuthError.ResetLinkExpired -> _uiState.update { it.copy(step = IamStep.LINK_EXPIRED) }
                    AuthError.PasswordReused -> _uiState.update { it.copy(passwordReused = true) }
                    AuthError.WeakPassword -> _uiState.update { it.copy(passwordWeak = true) }
                    else -> _uiState.update { it.copy(error = result.error.toIamError()) }
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

    private fun AuthError.toIamError(): IamError = when (this) {
        AuthError.Offline -> IamError.OFFLINE
        AuthError.TooManyRequests -> IamError.WAIT
        else -> IamError.GENERIC
    }
}
