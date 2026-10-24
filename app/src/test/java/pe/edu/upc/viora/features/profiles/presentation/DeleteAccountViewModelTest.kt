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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.harvestsettlement.presentation.FakePlotsForSettle
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsUseCase
import pe.edu.upc.viora.features.profiles.application.usecase.CheckMembershipUseCase
import pe.edu.upc.viora.features.profiles.application.usecase.DeleteAccountUseCase
import pe.edu.upc.viora.features.profiles.application.usecase.ObserveProfileUseCase
import pe.edu.upc.viora.features.profiles.domain.entity.UserProfile
import pe.edu.upc.viora.features.profiles.domain.repository.ProfileRepository
import pe.edu.upc.viora.features.profiles.domain.valueobject.ProfileId
import pe.edu.upc.viora.features.profiles.presentation.state.DeleteAccountError
import pe.edu.upc.viora.features.profiles.presentation.viewmodel.DeleteAccountViewModel

private class FakeProfiles : ProfileRepository {
    val profile = MutableStateFlow<UserProfile?>(UserProfile(ProfileId("p-1"), "u-1", "Teodoro Mamani", "PE", "+51952184337"))
    var deleteResult: AppResult<Unit> = AppResult.Success(Unit)
    val passwords = mutableListOf<String>()

    override fun observeProfile(): Flow<UserProfile?> = profile
    override suspend fun refreshProfile(): AppResult<UserProfile> = AppResult.Failure(AppError.Offline)
    override suspend fun updateProfile(fullName: String, country: String, nationalNumber: String) = AppResult.Failure(AppError.Offline)
    override suspend fun changePassword(currentPassword: String, newPassword: String) = AppResult.Failure(AppError.Offline)
    override suspend fun hasActiveMembership(): AppResult<Boolean> = AppResult.Success(true)
    override suspend fun signOut() = Unit
    override suspend fun deleteAccount(password: String): AppResult<Unit> {
        passwords += password
        if (deleteResult is AppResult.Success) profile.value = null
        return deleteResult
    }
}

/** P100: the button waits for the password and the box, the sheet confirms, and errors keep the account. */
@OptIn(ExperimentalCoroutinesApi::class)
class DeleteAccountViewModelTest {

    private val profiles = FakeProfiles()

    @Before fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = DeleteAccountViewModel(
        deleteAccount = DeleteAccountUseCase(profiles),
        checkMembership = CheckMembershipUseCase(profiles),
        observePlots = ObservePlotsUseCase(FakePlotsForSettle()),
        observeProfile = ObserveProfileUseCase(profiles),
    )

    @Test
    fun `nothing is asked until the password is typed and the box is ticked`() {
        val vm = viewModel()
        vm.askConfirmation()
        assertFalse(vm.uiState.value.isConfirming)

        vm.onPasswordChange("Olivo2025")
        vm.askConfirmation()
        assertFalse(vm.uiState.value.isConfirming)

        vm.onUnderstoodChange(true)
        vm.askConfirmation()
        assertTrue(vm.uiState.value.isConfirming)
        assertTrue(profiles.passwords.isEmpty())
    }

    @Test
    fun `confirming deletes the account and keeps the first name for the farewell`() {
        val vm = viewModel()
        assertEquals(true, vm.uiState.value.hasActivePlan)
        vm.onPasswordChange("Olivo2025")
        vm.onUnderstoodChange(true)
        vm.askConfirmation()

        vm.confirm()

        assertEquals(listOf("Olivo2025"), profiles.passwords)
        assertTrue(vm.uiState.value.isDeleted)
        assertEquals("Teodoro", vm.uiState.value.firstName)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `a wrong password or the demo account keep the account and say why`() {
        val vm = viewModel()
        vm.onPasswordChange("equivocada1")
        vm.onUnderstoodChange(true)

        profiles.deleteResult = AppResult.Failure(AppError.Validation("x", ProfileRepository.WRONG_CURRENT_PASSWORD))
        vm.confirm()
        assertEquals(DeleteAccountError.WRONG_PASSWORD, vm.uiState.value.error)
        assertFalse(vm.uiState.value.isDeleted)

        profiles.deleteResult = AppResult.Failure(AppError.Forbidden())
        vm.confirm()
        assertEquals(DeleteAccountError.PROTECTED_ACCOUNT, vm.uiState.value.error)

        vm.onPasswordChange("otra")
        assertNull(vm.uiState.value.error)
    }
}
