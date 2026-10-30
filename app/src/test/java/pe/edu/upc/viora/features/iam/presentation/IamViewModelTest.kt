package pe.edu.upc.viora.features.iam.presentation

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.iam.InMemoryPreferences
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
import pe.edu.upc.viora.features.iam.domain.repository.AuthRepository
import pe.edu.upc.viora.features.iam.domain.valueobject.OnboardingAnswers
import pe.edu.upc.viora.features.iam.infrastructure.local.OnboardingStore
import pe.edu.upc.viora.features.iam.presentation.state.CodeError
import pe.edu.upc.viora.features.iam.presentation.state.IamError
import pe.edu.upc.viora.features.iam.presentation.state.IamStep
import pe.edu.upc.viora.features.iam.presentation.viewmodel.IamViewModel

/** Only verifying the email answers; the other calls are not part of these tests. */
private class VerifyingAuthRepository : AuthRepository {
    override suspend fun verifyEmail(email: String, code: String): AuthResult<Unit> = AuthResult.Success(Unit)

    override suspend fun signIn(email: String, password: String): AuthResult<Unit> = error("not used")
    override suspend fun signUp(registration: Registration): AuthResult<String> = error("not used")
    override suspend fun resendCode(email: String): AuthResult<Unit> = error("not used")
    override suspend fun requestPasswordReset(email: String): AuthResult<Unit> = error("not used")
    override suspend fun checkPasswordResetLink(token: String): AuthResult<Unit> = error("not used")
    override suspend fun resetPassword(token: String, newPassword: String): AuthResult<Unit> = error("not used")
    override suspend fun changePassword(currentPassword: String, newPassword: String): AppResult<Unit> = error("not used")
    override suspend fun signOut() = error("not used")
    override suspend fun deleteAccount(password: String): AppResult<Unit> = error("not used")
}

/** Answers each call with what the test set and keeps what it was sent. */
private class ScriptedAuthRepository : AuthRepository {
    var signUpResult: AuthResult<String> = AuthResult.Success("teodoro@viora.ag")
    var verifyResult: AuthResult<Unit> = AuthResult.Success(Unit)
    var resendResult: AuthResult<Unit> = AuthResult.Success(Unit)
    var resetRequestResult: AuthResult<Unit> = AuthResult.Success(Unit)
    var linkResult: AuthResult<Unit> = AuthResult.Success(Unit)
    var resetResult: AuthResult<Unit> = AuthResult.Success(Unit)
    val registrations = mutableListOf<Registration>()
    val resentTo = mutableListOf<String>()
    val newPasswords = mutableListOf<Pair<String, String>>()

    override suspend fun signUp(registration: Registration): AuthResult<String> = signUpResult.also { registrations += registration }
    override suspend fun verifyEmail(email: String, code: String): AuthResult<Unit> = verifyResult
    override suspend fun resendCode(email: String): AuthResult<Unit> = resendResult.also { resentTo += email }
    override suspend fun requestPasswordReset(email: String): AuthResult<Unit> = resetRequestResult
    override suspend fun checkPasswordResetLink(token: String): AuthResult<Unit> = linkResult
    override suspend fun resetPassword(token: String, newPassword: String): AuthResult<Unit> =
        resetResult.also { newPasswords += token to newPassword }

    override suspend fun signIn(email: String, password: String): AuthResult<Unit> = error("not used")
    override suspend fun changePassword(currentPassword: String, newPassword: String): AppResult<Unit> = error("not used")
    override suspend fun signOut() = error("not used")
    override suspend fun deleteAccount(password: String): AppResult<Unit> = error("not used")
}

@OptIn(ExperimentalCoroutinesApi::class)
class IamViewModelTest {

    private lateinit var onboardingStore: OnboardingStore
    private lateinit var viewModel: IamViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        onboardingStore = OnboardingStore(InMemoryPreferences())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun verifyEmailAfterOnboarding(cooperativeCode: String?, hectares: Int? = null) {
        runBlocking { onboardingStore.save(OnboardingAnswers(fullName = "Teodoro Mamani", cooperativeCode = cooperativeCode, hectares = hectares)) }
        val auth = VerifyingAuthRepository()
        viewModel = IamViewModel(
            signInUseCase = SignInUseCase(auth),
            signUpUseCase = SignUpUseCase(auth),
            verifyEmailUseCase = VerifyEmailUseCase(auth),
            resendCodeUseCase = ResendCodeUseCase(auth),
            requestPasswordReset = RequestPasswordResetUseCase(auth),
            checkPasswordResetLink = CheckPasswordResetLinkUseCase(auth),
            resetPassword = ResetPasswordUseCase(auth),
            onboardingStore = onboardingStore,
            savedStateHandle = SavedStateHandle(),
        )
        viewModel.onCode("123456")
    }

    @Test
    fun `verifying the email hands over the code saved in the onboarding, then forgets the answers`() {
        verifyEmailAfterOnboarding(cooperativeCode = "COOP-7K2Q")

        val state = viewModel.uiState.value
        assertTrue(state.authenticated)
        assertEquals("COOP-7K2Q", state.cooperativeCodeToRedeem)
        assertFalse(state.buysPlan)
        assertNull(runBlocking { onboardingStore.answers().cooperativeCode })
    }

    @Test
    fun `verifying the email without a saved code goes on to pay the plan with the onboarding hectares`() {
        verifyEmailAfterOnboarding(cooperativeCode = null, hectares = 7)

        val state = viewModel.uiState.value
        assertTrue(state.authenticated)
        assertNull(state.cooperativeCodeToRedeem)
        assertTrue(state.buysPlan)
        assertEquals(7, state.planHectares)
    }

    // ---- Sign-up, code, back and recovery --------------------------------------------------

    private val auth = ScriptedAuthRepository()

    private fun iam(createAccount: Boolean = true, token: String? = null) = IamViewModel(
        signInUseCase = SignInUseCase(auth),
        signUpUseCase = SignUpUseCase(auth),
        verifyEmailUseCase = VerifyEmailUseCase(auth),
        resendCodeUseCase = ResendCodeUseCase(auth),
        requestPasswordReset = RequestPasswordResetUseCase(auth),
        checkPasswordResetLink = CheckPasswordResetLinkUseCase(auth),
        resetPassword = ResetPasswordUseCase(auth),
        onboardingStore = onboardingStore,
        savedStateHandle = SavedStateHandle(listOfNotNull("createAccount" to createAccount, token?.let { "token" to it }).toMap()),
    )

    private fun IamViewModel.fillAccount() {
        onEmail("teodoro@viora.ag")
        onPassword("Olivo2025")
    }

    @Test
    fun `signing up sends the onboarding answers with the terms accepted under the legal notice`() {
        runBlocking {
            onboardingStore.save(OnboardingAnswers(fullName = "Teodoro Mamani", countryIso = "PE", nationalPhone = "952184337"))
            onboardingStore.acceptTerms()
        }
        val viewModel = iam()

        viewModel.fillAccount()
        viewModel.signUp()

        val sent = auth.registrations.single()
        assertEquals("Teodoro Mamani", sent.fullName)
        assertEquals("952184337", sent.nationalPhone)
        assertTrue(sent.termsAccepted)
        assertEquals(IamStep.VERIFY, viewModel.uiState.value.step)
        assertEquals("teodoro@viora.ag", viewModel.uiState.value.pendingEmail)
    }

    @Test
    fun `answers saved after the acceptance keep it`() {
        runBlocking {
            onboardingStore.acceptTerms()
            onboardingStore.save(OnboardingAnswers(fullName = "Teodoro Mamani"))
        }

        assertTrue(runBlocking { onboardingStore.answers().termsAccepted })
    }

    @Test
    fun `an email already registered is flagged on the field and the step stays`() {
        auth.signUpResult = AuthResult.Failure(AuthError.EmailTaken)
        val viewModel = iam()

        viewModel.fillAccount()
        viewModel.signUp()

        assertTrue(viewModel.uiState.value.emailTaken)
        assertEquals(IamStep.CREATE_ACCOUNT, viewModel.uiState.value.step)
    }

    @Test
    fun `back from the code leaves to sign-in with the email filled in, never to create the account again`() {
        val viewModel = iam()
        viewModel.fillAccount()
        viewModel.signUp()

        assertTrue(viewModel.back())

        assertEquals(IamStep.SIGN_IN, viewModel.uiState.value.step)
        assertEquals("teodoro@viora.ag", viewModel.uiState.value.email)
        assertFalse(iam().back())
    }

    @Test
    fun `a wrong code says how many attempts are left and a dead one says why`() {
        val viewModel = iam()
        viewModel.fillAccount()
        viewModel.signUp()

        auth.verifyResult = AuthResult.Failure(AuthError.InvalidCode(remainingAttempts = 2))
        viewModel.onCode("111111")
        assertEquals(CodeError.Wrong(2), viewModel.uiState.value.codeError)

        auth.verifyResult = AuthResult.Failure(AuthError.CodeExpired)
        viewModel.onCode("222222")
        assertEquals(CodeError.Expired, viewModel.uiState.value.codeError)

        auth.verifyResult = AuthResult.Failure(AuthError.CodeAttemptsExceeded)
        viewModel.onCode("333333")
        assertEquals(CodeError.Blocked, viewModel.uiState.value.codeError)
        assertFalse(viewModel.uiState.value.authenticated)
    }

    @Test
    fun `a dead code can be replaced at once and the new one is announced`() {
        val viewModel = iam()
        viewModel.fillAccount()
        viewModel.signUp()
        auth.verifyResult = AuthResult.Failure(AuthError.CodeExpired)
        viewModel.onCode("111111")

        viewModel.resend()

        assertEquals(listOf("teodoro@viora.ag"), auth.resentTo)
        assertNull(viewModel.uiState.value.codeError)
        assertTrue(viewModel.uiState.value.resendSeconds > 0)
    }

    @Test
    fun `a resend while the server still waits only restarts the countdown`() {
        val viewModel = iam()
        viewModel.fillAccount()
        viewModel.signUp()
        auth.verifyResult = AuthResult.Failure(AuthError.NoActiveCode)
        viewModel.onCode("111111")
        auth.resendResult = AuthResult.Failure(AuthError.CodeNotYetResendable(retryAfterSeconds = 30))

        viewModel.resend()

        assertEquals(30, viewModel.uiState.value.resendSeconds)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `asking for the recovery link without connection says so and stays on the form`() {
        auth.resetRequestResult = AuthResult.Failure(AuthError.Offline)
        val viewModel = iam(createAccount = false)
        viewModel.goTo(IamStep.FORGOT)
        viewModel.onEmail("teodoro@viora.ag")

        viewModel.requestResetLink()

        assertEquals(IamStep.FORGOT, viewModel.uiState.value.step)
        assertEquals(IamError.OFFLINE, viewModel.uiState.value.error)
    }

    @Test
    fun `the recovery link opens the new password and saving it ends at sign-in`() {
        val viewModel = iam(createAccount = false, token = "reset-token")
        assertEquals(IamStep.NEW_PASSWORD, viewModel.uiState.value.step)

        viewModel.onPassword("Olivo2026")
        viewModel.onConfirmPassword("Olivo2026")
        viewModel.saveNewPassword()

        assertEquals(listOf("reset-token" to "Olivo2026"), auth.newPasswords)
        assertEquals(IamStep.RESET_DONE, viewModel.uiState.value.step)
        assertTrue(viewModel.back())
        assertEquals(IamStep.SIGN_IN, viewModel.uiState.value.step)
    }

    @Test
    fun `an expired recovery link shows it, and a reused password is flagged on the field`() {
        auth.linkResult = AuthResult.Failure(AuthError.ResetLinkExpired)
        assertEquals(IamStep.LINK_EXPIRED, iam(createAccount = false, token = "old").uiState.value.step)

        auth.linkResult = AuthResult.Success(Unit)
        auth.resetResult = AuthResult.Failure(AuthError.PasswordReused)
        val viewModel = iam(createAccount = false, token = "reset-token")
        viewModel.onPassword("Olivo2025")
        viewModel.onConfirmPassword("Olivo2025")
        viewModel.saveNewPassword()

        assertTrue(viewModel.uiState.value.passwordReused)
        assertEquals(IamStep.NEW_PASSWORD, viewModel.uiState.value.step)
    }
}
