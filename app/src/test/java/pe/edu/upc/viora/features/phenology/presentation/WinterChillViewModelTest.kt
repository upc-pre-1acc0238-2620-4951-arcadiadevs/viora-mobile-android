package pe.edu.upc.viora.features.phenology.presentation

import androidx.lifecycle.SavedStateHandle
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.phenology.application.usecase.ObserveChillTrackerUseCase
import pe.edu.upc.viora.features.phenology.application.usecase.RefreshChillTrackerUseCase
import pe.edu.upc.viora.features.phenology.domain.entity.ChillTracker
import pe.edu.upc.viora.features.phenology.domain.entity.EnsoRiskLevel
import pe.edu.upc.viora.features.phenology.domain.entity.WinterSeasonState
import pe.edu.upc.viora.features.phenology.domain.repository.ChillRepository
import pe.edu.upc.viora.features.phenology.presentation.state.WinterChillUiState
import pe.edu.upc.viora.features.phenology.presentation.viewmodel.WinterChillViewModel
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotUseCase

private class FakeChillViewModelRepo : ChillRepository {
    val tracker = MutableStateFlow<ChillTracker?>(null)
    val lastSync = MutableStateFlow<Instant?>(null)
    var refreshResult: AppResult<Unit> = AppResult.Success(Unit)
    var refreshCalls = 0

    override fun observeChillTracker(plotId: String): Flow<ChillTracker?> = tracker
    override fun observeLastSync(plotId: String): Flow<Instant?> = lastSync
    override suspend fun refresh(plotId: String): AppResult<Unit> {
        refreshCalls++
        return refreshResult
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class WinterChillViewModelTest {

    private val repository = FakeChillViewModelRepo()
    private val clock = Clock.fixed(Instant.parse("2026-10-07T12:00:00Z"), ZoneOffset.UTC)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = WinterChillViewModel(
        savedStateHandle = SavedStateHandle(mapOf("plotId" to "plot-1", "plotName" to "Cuartel Norte")),
        observeChillTracker = ObserveChillTrackerUseCase(repository),
        observePlot = ObservePlotUseCase(FakePlots()),
        refreshChillTracker = RefreshChillTrackerUseCase(repository),
        chillRepository = repository,
        clock = clock,
    )

    private fun WinterChillViewModel.content(): WinterChillUiState.Content {
        val seen = mutableListOf<WinterChillUiState>()
        val job = uiState.onEach { seen += it }.launchIn(kotlinx.coroutines.CoroutineScope(Dispatchers.Main))
        job.cancel()
        return seen.last() as WinterChillUiState.Content
    }

    @Test
    fun startsLoadingAndRefreshesOnceOnStart() = runTest {
        val vm = viewModel()
        assertEquals(1, repository.refreshCalls)
    }

    @Test
    fun showsCachedContentWithPortionsAndState() = runTest {
        repository.tracker.value = ChillTracker(
            plotId = "plot-1",
            accumulatedPortions = 24.0,
            thresholdPortions = 30.0,
            daysAbove24Celsius = 4,
            seasonState = WinterSeasonState.ACCUMULATING,
            projectedCompletionDate = LocalDate.of(2026, 8, 14),
            previousWinterCompletionDate = LocalDate.of(2025, 8, 5),
            ensoRisk = EnsoRiskLevel.NEUTRAL,
            curvePoints = emptyList(),
            syncedAt = clock.instant(),
        )

        val content = viewModel().content()
        assertEquals(24, content.accumulatedPortions)
        assertEquals(30, content.thresholdPortions)
        assertEquals(6, content.portionsRemaining)
        assertEquals(4, content.daysAbove24Celsius)
        assertEquals(WinterSeasonState.ACCUMULATING, content.seasonState)
        assertEquals(EnsoRiskLevel.NEUTRAL, content.ensoRisk)
        assertEquals(LocalDate.of(2026, 8, 14), content.projectedCompletionDate)
        assertEquals(LocalDate.of(2025, 8, 5), content.previousWinterCompletionDate)
        assertFalse(content.offline)
    }

    @Test
    fun offlineRefreshWithCacheMarksStateAsOffline() = runTest {
        repository.tracker.value = ChillTracker(
            plotId = "plot-1",
            accumulatedPortions = 24.0,
            thresholdPortions = 30.0,
            daysAbove24Celsius = 4,
            seasonState = WinterSeasonState.CHILL_HALTED,
            projectedCompletionDate = null,
            previousWinterCompletionDate = null,
            ensoRisk = EnsoRiskLevel.ACTIVE,
            curvePoints = emptyList(),
            syncedAt = clock.instant(),
        )
        repository.refreshResult = AppResult.Failure(AppError.Offline)

        val content = viewModel().content()
        assertTrue(content.offline)
        assertEquals(WinterSeasonState.CHILL_HALTED, content.seasonState)
        assertEquals(EnsoRiskLevel.ACTIVE, content.ensoRisk)
    }

    @Test
    fun failedInitialRefreshWithNothingCachedEmitsError() = runTest {
        repository.refreshResult = AppResult.Failure(AppError.Offline)
        val vm = viewModel()
        val states = mutableListOf<WinterChillUiState>()
        val job = vm.uiState.onEach { states += it }.launchIn(kotlinx.coroutines.CoroutineScope(Dispatchers.Main))

        assertTrue(states.last() is WinterChillUiState.Error)
        assertEquals(AppError.Offline, (states.last() as WinterChillUiState.Error).error)
        job.cancel()
    }
}
