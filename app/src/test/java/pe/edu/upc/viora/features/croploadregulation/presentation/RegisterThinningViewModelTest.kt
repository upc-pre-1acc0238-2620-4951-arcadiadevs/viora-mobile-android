package pe.edu.upc.viora.features.croploadregulation.presentation

import androidx.lifecycle.SavedStateHandle
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
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
import pe.edu.upc.viora.features.croploadregulation.application.usecase.ConfirmThinningExecutionUseCase
import pe.edu.upc.viora.features.croploadregulation.application.usecase.ObserveThinningPrescriptionUseCase
import pe.edu.upc.viora.features.croploadregulation.domain.entity.CachedPrescription
import pe.edu.upc.viora.features.croploadregulation.issuedPlan
import pe.edu.upc.viora.features.croploadregulation.presentation.state.WindowCheck
import pe.edu.upc.viora.features.croploadregulation.presentation.viewmodel.RegisterThinningViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterThinningViewModelTest {

    private val repository = FakePlanRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** The window of [issuedPlan] runs from 3 to 30 November 2026. */
    private fun viewModel(today: String = "2026-11-18") = RegisterThinningViewModel(
        savedStateHandle = SavedStateHandle(mapOf("plotId" to "plot-1", "plotName" to "La Yarada 02", "campaignYear" to 2026)),
        observePrescription = ObserveThinningPrescriptionUseCase(repository),
        confirmExecution = ConfirmThinningExecutionUseCase(repository),
        clock = Clock.fixed(Instant.parse("${today}T15:00:00Z"), ZoneOffset.UTC),
    )

    private fun withPlan(percentage: Double = 30.0) {
        repository.cached.value = CachedPrescription(issuedPlan(percentage = percentage), Instant.EPOCH)
    }

    @Test
    fun `it opens on today with the prescribed share preloaded`() {
        withPlan(percentage = 29.6)

        val state = viewModel().uiState.value

        assertEquals(LocalDate.of(2026, 11, 18), state.executedOn)
        assertEquals(30, state.percentage)
        assertEquals(30, state.prescribedPercentage)
        assertEquals(LocalDate.of(2026, 11, 3), state.earliest)
        assertEquals(WindowCheck.Within(daysLeft = 12), state.windowCheck)
        assertTrue(state.canSave)
        assertFalse(state.hasChanges)
    }

    @Test
    fun `the share moves in steps of five and stays within 0 and 100`() {
        withPlan(percentage = 95.0)
        val viewModel = viewModel()

        viewModel.stepPercentage(+1)
        viewModel.stepPercentage(+1)
        assertEquals(100, viewModel.uiState.value.percentage)

        repeat(25) { viewModel.stepPercentage(-1) }
        assertEquals(0, viewModel.uiState.value.percentage)
        assertTrue(viewModel.uiState.value.hasChanges)
    }

    @Test
    fun `the day cannot leave the window opening nor pass today`() {
        withPlan()
        val viewModel = viewModel()

        viewModel.onDateChange(LocalDate.of(2026, 11, 2))
        viewModel.onDateChange(LocalDate.of(2026, 11, 19))
        assertEquals(LocalDate.of(2026, 11, 18), viewModel.uiState.value.executedOn)

        viewModel.onDateChange(LocalDate.of(2026, 11, 3))
        assertEquals(LocalDate.of(2026, 11, 3), viewModel.uiState.value.executedOn)
    }

    @Test
    fun `a day after the window closes is late but can still be saved`() {
        withPlan()
        val viewModel = viewModel(today = "2026-12-09")

        val state = viewModel.uiState.value

        assertEquals(WindowCheck.Late(LocalDate.of(2026, 11, 30)), state.windowCheck)
        assertTrue(state.canSave)
    }

    @Test
    fun `before the window opens nothing can be saved`() {
        withPlan()

        val state = viewModel(today = "2026-11-01").uiState.value

        assertEquals(WindowCheck.NotOpenYet(LocalDate.of(2026, 11, 3)), state.windowCheck)
        assertFalse(state.canSave)
    }

    @Test
    fun `without a prescription there is nothing to record`() {
        val state = viewModel().uiState.value

        assertNull(state.prescriptionId)
        assertFalse(state.canSave)
    }

    @Test
    fun `saving sends the day, share and note and then opens the result`() {
        withPlan()
        val viewModel = viewModel()
        var opened: String? = null
        viewModel.stepPercentage(-1)
        viewModel.onNoteChange("Empecé por el lado norte")

        viewModel.save { opened = it }

        assertEquals(
            FakePlanRepository.Confirmation("p-1", LocalDate.of(2026, 11, 18), 25.0, "Empecé por el lado norte"),
            repository.confirmations.single(),
        )
        assertEquals("p-1", opened)
    }

    @Test
    fun `a rejected save keeps the form with the error`() {
        withPlan()
        repository.confirmResult = AppResult.Failure(AppError.Offline)
        val viewModel = viewModel()
        var opened = false

        viewModel.save { opened = true }

        assertFalse(opened)
        assertEquals(AppError.Offline, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isSaving)
    }
}
