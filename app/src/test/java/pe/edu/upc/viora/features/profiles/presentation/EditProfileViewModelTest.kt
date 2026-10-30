package pe.edu.upc.viora.features.profiles.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.profiles.application.usecase.ObserveProfileUseCase
import pe.edu.upc.viora.features.profiles.application.usecase.UpdateProfileUseCase
import pe.edu.upc.viora.features.profiles.domain.entity.UserProfile
import pe.edu.upc.viora.features.profiles.domain.repository.ProfileRepository
import pe.edu.upc.viora.features.profiles.domain.valueobject.ProfileId
import pe.edu.upc.viora.features.profiles.presentation.state.SaveProfileError
import pe.edu.upc.viora.features.profiles.presentation.viewmodel.EditProfileViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class EditProfileViewModelTest {

    private val kept = UserProfile(ProfileId("p-1"), "u-1", "Teodoro Mamani", "PE", "+51952184337", "teodoro@viora.pe")

    private inner class Profiles : ProfileRepository {
        val profile = MutableStateFlow<UserProfile?>(kept)
        var answer: AppResult<UserProfile> = AppResult.Success(kept)
        val sent = mutableListOf<Triple<String, String, String>>()

        override fun observeProfile(): Flow<UserProfile?> = profile
        override suspend fun refreshProfile(): AppResult<UserProfile> = AppResult.Failure(AppError.Offline)
        override suspend fun updateProfile(fullName: String, country: String, nationalNumber: String): AppResult<UserProfile> {
            sent += Triple(fullName, country, nationalNumber)
            return answer
        }
    }

    private val profiles = Profiles()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = EditProfileViewModel(ObserveProfileUseCase(profiles), UpdateProfileUseCase(profiles))

    @Test
    fun `the form starts from the kept profile, with the national digits of the mobile`() {
        val state = viewModel().uiState.value

        assertEquals("Teodoro Mamani", state.fullName)
        assertEquals("952184337", state.phone)
        assertEquals("teodoro@viora.pe", state.email)
        assertFalse(state.canSave)
    }

    @Test
    fun `a changed name is saved trimmed with the country and the national number`() {
        val vm = viewModel()
        vm.onFullNameChange("  Teodoro Mamani Quispe ")

        vm.saveChanges()

        assertEquals(listOf(Triple("Teodoro Mamani Quispe", "PE", "952184337")), profiles.sent)
        assertTrue(vm.uiState.value.isSaved)
    }

    @Test
    fun `a single letter is not saved and the error shows`() {
        val vm = viewModel()
        vm.onFullNameChange("T")

        vm.saveChanges()

        assertTrue(profiles.sent.isEmpty())
        assertTrue(vm.uiState.value.showNameError)
    }

    @Test
    fun `only digits reach the mobile, up to the country's length`() {
        val vm = viewModel()

        vm.onPhoneChange("987 654 321 99")

        assertEquals("987654321", vm.uiState.value.phone)
    }

    @Test
    fun `a failed save says whether it was the connection`() {
        val vm = viewModel()
        vm.onFullNameChange("Teodoro Mamani Quispe")

        profiles.answer = AppResult.Failure(AppError.Offline)
        vm.saveChanges()
        assertEquals(SaveProfileError.OFFLINE, vm.uiState.value.error)

        profiles.answer = AppResult.Failure(AppError.Server(500))
        vm.saveChanges()
        assertEquals(SaveProfileError.GENERIC, vm.uiState.value.error)
        assertFalse(vm.uiState.value.isSaved)
    }
}
