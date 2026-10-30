package pe.edu.upc.viora.features.profiles.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.iam.application.usecase.ChangePasswordUseCase
import pe.edu.upc.viora.features.iam.domain.repository.AuthRepository
import pe.edu.upc.viora.features.profiles.presentation.state.ChangePasswordError
import pe.edu.upc.viora.features.profiles.presentation.viewmodel.ChangePasswordViewModel
import pe.edu.upc.viora.testing.unused

@OptIn(ExperimentalCoroutinesApi::class)
class ChangePasswordViewModelTest {

    private var answer: AppResult<Unit> = AppResult.Success(Unit)
    private val sent = mutableListOf<Pair<String, String>>()

    private val auth = object : AuthRepository by unused<AuthRepository>() {
        override suspend fun changePassword(currentPassword: String, newPassword: String): AppResult<Unit> {
            sent += currentPassword to newPassword
            return answer
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun filled() = ChangePasswordViewModel(ChangePasswordUseCase(auth)).apply {
        onCurrentPasswordChange("Olivo2025")
        onNewPasswordChange("Yarada2026")
        onConfirmPasswordChange("Yarada2026")
    }

    @Test
    fun `a valid change is sent and the screen closes`() {
        val vm = filled()

        vm.submit()

        assertEquals(listOf("Olivo2025" to "Yarada2026"), sent)
        assertTrue(vm.uiState.value.isChanged)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `nothing is sent while the confirmation differs`() {
        val vm = filled()
        vm.onConfirmPasswordChange("Yarada2027")

        vm.submit()

        assertTrue(sent.isEmpty())
    }

    @Test
    fun `a wrong current password marks that field`() {
        answer = AppResult.Failure(AppError.Validation("Wrong", ChangePasswordUseCase.WRONG_CURRENT_PASSWORD))
        val vm = filled()

        vm.submit()

        assertTrue(vm.uiState.value.currentPasswordIncorrect)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `a new password the server refuses shows the server's reason`() {
        answer = AppResult.Failure(AppError.Validation("It is one of your last five passwords"))
        val vm = filled()

        vm.submit()

        assertEquals("It is one of your last five passwords", vm.uiState.value.newPasswordRejection)
    }

    @Test
    fun `without connection the change can be tried again`() {
        answer = AppResult.Failure(AppError.Offline)
        val vm = filled()

        vm.submit()

        assertEquals(ChangePasswordError.OFFLINE, vm.uiState.value.error)
        assertTrue(vm.uiState.value.canSubmit)
    }

    @Test
    fun `typing again clears the wrong password mark`() {
        answer = AppResult.Failure(AppError.Validation("Wrong", ChangePasswordUseCase.WRONG_CURRENT_PASSWORD))
        val vm = filled()
        vm.submit()

        vm.onCurrentPasswordChange("Olivo2026")

        assertFalse(vm.uiState.value.currentPasswordIncorrect)
    }
}
