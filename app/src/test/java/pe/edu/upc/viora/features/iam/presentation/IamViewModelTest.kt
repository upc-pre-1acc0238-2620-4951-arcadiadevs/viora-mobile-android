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
import pe.edu.upc.viora.features.iam.domain.model.AuthResult
import pe.edu.upc.viora.features.iam.domain.model.Registration
import pe.edu.upc.viora.features.iam.domain.repository.AuthRepository
import pe.edu.upc.viora.features.iam.domain.valueobject.OnboardingAnswers
import pe.edu.upc.viora.features.iam.infrastructure.local.OnboardingStore
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
}
