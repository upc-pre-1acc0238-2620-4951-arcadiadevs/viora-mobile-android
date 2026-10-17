package pe.edu.upc.viora.features.croploadregulation.presentation

import androidx.lifecycle.SavedStateHandle
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.croploadregulation.application.usecase.ObserveThinningExecutionUseCase
import pe.edu.upc.viora.features.croploadregulation.application.usecase.RefreshThinningExecutionUseCase
import pe.edu.upc.viora.features.croploadregulation.domain.entity.ThinningExecution
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.CaliberProjectionStatus
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.LoadState
import pe.edu.upc.viora.features.croploadregulation.presentation.state.CaliberCard
import pe.edu.upc.viora.features.croploadregulation.presentation.state.ResultVoice
import pe.edu.upc.viora.features.croploadregulation.presentation.state.ThinningResultUiState
import pe.edu.upc.viora.features.croploadregulation.presentation.viewmodel.ThinningResultViewModel
import pe.edu.upc.viora.features.croploadregulation.recordedThinning
import pe.edu.upc.viora.features.phenology.presentation.FakePlots
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class ThinningResultTest {

    private val repository = FakePlanRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = ThinningResultViewModel(
        savedStateHandle = SavedStateHandle(mapOf("plotId" to "plot-1", "prescriptionId" to "p-1", "campaignYear" to 2026)),
        observeExecution = ObserveThinningExecutionUseCase(repository),
        observePlot = ObservePlotUseCase(FakePlots()),
        refreshExecution = RefreshThinningExecutionUseCase(repository),
        clock = Clock.fixed(Instant.parse("2026-11-18T15:00:00Z"), ZoneOffset.UTC),
    )

    private fun ThinningResultViewModel.lastState(): ThinningResultUiState {
        val seen = mutableListOf<ThinningResultUiState>()
        val job = uiState.onEach { seen += it }.launchIn(CoroutineScope(Dispatchers.Main))
        job.cancel()
        return seen.last()
    }

    private fun content(execution: ThinningExecution) =
        ThinningResultUiState.Content(plotName = "La Yarada 02", variety = null, campaignYear = 2026, execution = execution)

    @Test
    fun `each caliber status picks its Figma card`() {
        assertEquals(CaliberCard.ESTIMATED, content(recordedThinning(CaliberProjectionStatus.ESTIMATED)).caliberCard)
        assertEquals(CaliberCard.LEARNING, content(recordedThinning(CaliberProjectionStatus.NOT_CALIBRATED)).caliberCard)
        assertEquals(CaliberCard.LATE, content(recordedThinning(CaliberProjectionStatus.NOT_ESTIMATED_LATE)).caliberCard)
        assertEquals(CaliberCard.OUT_OF_RANGE, content(recordedThinning(CaliberProjectionStatus.OUTSIDE_CALIBRATION_RANGE)).caliberCard)
        assertNull(content(recordedThinning(CaliberProjectionStatus.NOT_APPLICABLE)).caliberCard)
    }

    @Test
    fun `an estimate without its grade shows no card instead of an empty figure`() {
        val thinning = recordedThinning().let { it.copy(caliberProjection = it.caliberProjection!!.copy(mostLikelySizeGrade = null)) }

        assertNull(content(thinning).caliberCard)
    }

    @Test
    fun `the Viora line says late first, then a load still too high`() {
        assertEquals(ResultVoice.GOOD_JOB, content(recordedThinning()).voice)
        assertEquals(ResultVoice.LATE, content(recordedThinning(isOpportune = false, loadState = LoadState.SEVERE_OVERLOAD)).voice)
        val overloaded = content(recordedThinning(CaliberProjectionStatus.OUTSIDE_CALIBRATION_RANGE, loadState = LoadState.MODERATE_OVERLOAD))
        assertEquals(ResultVoice.STILL_OVERLOADED, overloaded.voice)
        assertTrue(overloaded.stillOverloaded)
    }

    @Test
    fun `the cached thinning shows even when it cannot be downloaded again`() {
        repository.execution.value = recordedThinning()
        repository.refreshExecutionResult = AppResult.Failure(AppError.Offline)

        val state = viewModel().lastState()

        assertTrue(state is ThinningResultUiState.Content)
        assertEquals(1, repository.refreshExecutionCalls)
    }

    @Test
    fun `without a recorded thinning it is an error that can be retried`() {
        repository.refreshExecutionResult = AppResult.Failure(AppError.Offline)

        val state = viewModel().lastState()

        assertEquals(ThinningResultUiState.Error(AppError.Offline), state)
    }
}
