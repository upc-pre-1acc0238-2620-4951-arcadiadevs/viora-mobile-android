package pe.edu.upc.viora.features.croploadregulation.presentation

import androidx.lifecycle.SavedStateHandle
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
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
import pe.edu.upc.viora.features.croploadregulation.application.usecase.ObserveThinningExecutionUseCase
import pe.edu.upc.viora.features.croploadregulation.application.usecase.ObserveThinningPrescriptionUseCase
import pe.edu.upc.viora.features.croploadregulation.application.usecase.RecordFullBloomUseCase
import pe.edu.upc.viora.features.croploadregulation.application.usecase.RefreshThinningPrescriptionUseCase
import pe.edu.upc.viora.features.croploadregulation.blockedPlan
import pe.edu.upc.viora.features.croploadregulation.domain.entity.CachedPrescription
import pe.edu.upc.viora.features.croploadregulation.domain.entity.ThinningPrescription
import pe.edu.upc.viora.features.croploadregulation.domain.repository.ThinningPrescriptionRepository
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.PrescriptionBlocker
import pe.edu.upc.viora.features.croploadregulation.issuedPlan
import pe.edu.upc.viora.features.croploadregulation.presentation.state.PlanStage
import pe.edu.upc.viora.features.croploadregulation.presentation.state.ThinningPlanUiState
import pe.edu.upc.viora.features.croploadregulation.presentation.viewmodel.ThinningPlanViewModel
import pe.edu.upc.viora.features.phenology.presentation.FakePlots
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class ThinningPlanViewModelTest {

    private val repository = FakePlanRepository()
    private val clock = Clock.fixed(Instant.parse("2026-11-04T15:00:00Z"), ZoneOffset.UTC)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = ThinningPlanViewModel(
        savedStateHandle = SavedStateHandle(mapOf("plotId" to "plot-1", "plotName" to "La Yarada 02", "campaignYear" to 0)),
        observePrescription = ObserveThinningPrescriptionUseCase(repository),
        observeExecution = ObserveThinningExecutionUseCase(repository),
        observePlot = ObservePlotUseCase(FakePlots()),
        refreshPrescription = RefreshThinningPrescriptionUseCase(repository),
        recordFullBloom = RecordFullBloomUseCase(repository),
        clock = clock,
    )

    private fun ThinningPlanViewModel.lastState(): ThinningPlanUiState {
        val seen = mutableListOf<ThinningPlanUiState>()
        val job = uiState.onEach { seen += it }.launchIn(CoroutineScope(Dispatchers.Main))
        job.cancel()
        return seen.last()
    }

    @Test
    fun `it reads the current campaign and refreshes once`() = runTest {
        val vm = viewModel()
        assertEquals(2026, vm.campaignYear)
        assertEquals(1, repository.refreshCalls)
    }

    @Test
    fun `without a saved plan a failure is an error, with one it is the offline plan`() = runTest {
        repository.refreshResult = AppResult.Failure(AppError.Offline)
        val vm = viewModel()
        assertEquals(ThinningPlanUiState.Error(AppError.Offline), vm.lastState())

        repository.cached.value = CachedPrescription(issuedPlan(), Instant.EPOCH)
        val content = vm.lastState() as ThinningPlanUiState.Content
        assertTrue(content.offline)
        assertEquals("La Yarada 02", content.plotName)
        assertEquals(2.5, content.areaHectares!!, 0.0)
        assertEquals(PlanStage.PRESCRIBED, content.stage)
    }

    @Test
    fun `the bloom sheet opens on today and never goes past it`() = runTest {
        repository.cached.value = CachedPrescription(blockedPlan(PrescriptionBlocker.FULL_BLOOM_MISSING), Instant.EPOCH)
        val vm = viewModel()
        vm.lastState()

        vm.openFullBloomSheet()
        assertEquals(LocalDate.of(2026, 11, 4), vm.sheet.value!!.date)
        assertFalse(vm.sheet.value!!.canStepForward)

        vm.stepFullBloom(+1)
        assertEquals(LocalDate.of(2026, 11, 4), vm.sheet.value!!.date)
        repeat(15) { vm.stepFullBloom(-1) }
        assertEquals(LocalDate.of(2026, 10, 20), vm.sheet.value!!.date)
    }

    @Test
    fun `the sheet opens on the recorded bloom when correcting it`() = runTest {
        repository.cached.value = CachedPrescription(issuedPlan(), Instant.EPOCH)
        val vm = viewModel()
        vm.lastState()

        vm.openFullBloomSheet()

        assertEquals(LocalDate.of(2026, 10, 20), vm.sheet.value!!.date)
    }

    @Test
    fun `saving records the date and closes the sheet on the issued plan`() = runTest {
        repository.cached.value = CachedPrescription(blockedPlan(PrescriptionBlocker.FULL_BLOOM_MISSING), Instant.EPOCH)
        val vm = viewModel()
        vm.lastState()
        vm.openFullBloomSheet()
        repeat(15) { vm.stepFullBloom(-1) }

        vm.saveFullBloom()

        assertEquals(listOf(LocalDate.of(2026, 10, 20)), repository.bloomDates)
        assertNull(vm.sheet.value)
        assertEquals(PlanStage.PRESCRIBED, (vm.lastState() as ThinningPlanUiState.Content).stage)
    }

    @Test
    fun `a rejected bloom keeps the sheet open with the error`() = runTest {
        repository.cached.value = CachedPrescription(blockedPlan(PrescriptionBlocker.FULL_BLOOM_MISSING), Instant.EPOCH)
        repository.bloomResult = AppResult.Failure(AppError.Validation("fuera de la campaña"))
        val vm = viewModel()
        vm.lastState()
        vm.openFullBloomSheet()

        vm.saveFullBloom()

        val sheet = vm.sheet.value!!
        assertFalse(sheet.isSaving)
        assertEquals(AppError.Validation("fuera de la campaña"), sheet.error)
    }
}
