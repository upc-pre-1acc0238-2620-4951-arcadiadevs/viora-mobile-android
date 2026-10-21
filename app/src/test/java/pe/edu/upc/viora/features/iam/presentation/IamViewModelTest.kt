package pe.edu.upc.viora.features.iam.presentation

import androidx.lifecycle.SavedStateHandle
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.core.datastore.SessionStore
import pe.edu.upc.viora.features.iam.InMemoryPreferences
import pe.edu.upc.viora.features.iam.domain.valueobject.OnboardingAnswers
import pe.edu.upc.viora.features.iam.infrastructure.local.OnboardingStore
import pe.edu.upc.viora.features.iam.infrastructure.remote.AuthSession
import pe.edu.upc.viora.features.iam.infrastructure.remote.IamService
import pe.edu.upc.viora.features.iam.infrastructure.remote.NewPasswordRequest
import pe.edu.upc.viora.features.iam.infrastructure.remote.PasswordResetRequest
import pe.edu.upc.viora.features.iam.infrastructure.remote.RefreshTokenRequest
import pe.edu.upc.viora.features.iam.infrastructure.remote.ResendCodeRequest
import pe.edu.upc.viora.features.iam.infrastructure.remote.SignInRequest
import pe.edu.upc.viora.features.iam.infrastructure.remote.SignUpRequest
import pe.edu.upc.viora.features.iam.infrastructure.remote.SignUpResponse
import pe.edu.upc.viora.features.iam.infrastructure.remote.VerifyEmailRequest
import pe.edu.upc.viora.features.iam.presentation.viewmodel.IamViewModel

/** Only verifying the email answers; the other calls are not part of these tests. */
private class VerifyingIamService : IamService {
    override suspend fun verifyEmail(request: VerifyEmailRequest) =
        AuthSession(accessToken = "access", refreshToken = "refresh", userId = "producer-1", role = "ROLE_PRODUCER")

    override suspend fun signIn(request: SignInRequest): AuthSession = error("not used")
    override suspend fun signUp(request: SignUpRequest): SignUpResponse = error("not used")
    override suspend fun resendCode(request: ResendCodeRequest) = error("not used")
    override suspend fun requestPasswordReset(request: PasswordResetRequest) = error("not used")
    override suspend fun checkPasswordResetToken(token: String) = error("not used")
    override suspend fun refresh(request: RefreshTokenRequest): AuthSession = error("not used")
    override suspend fun resetPassword(token: String, request: NewPasswordRequest) = error("not used")
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

    private fun verifyEmailAfterOnboarding(cooperativeCode: String?) {
        runBlocking { onboardingStore.save(OnboardingAnswers(fullName = "Teodoro Mamani", cooperativeCode = cooperativeCode)) }
        viewModel = IamViewModel(
            service = VerifyingIamService(),
            sessionStore = SessionStore(InMemoryPreferences()),
            onboardingStore = onboardingStore,
            clock = Clock.fixed(Instant.parse("2026-10-21T12:00:00Z"), ZoneOffset.UTC),
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
        assertNull(runBlocking { onboardingStore.answers().cooperativeCode })
    }

    @Test
    fun `verifying the email without a saved code still signs the producer in`() {
        verifyEmailAfterOnboarding(cooperativeCode = null)

        val state = viewModel.uiState.value
        assertTrue(state.authenticated)
        assertNull(state.cooperativeCodeToRedeem)
    }
}
