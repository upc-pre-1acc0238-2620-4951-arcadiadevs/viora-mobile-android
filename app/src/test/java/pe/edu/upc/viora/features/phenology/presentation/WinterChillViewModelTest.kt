package pe.edu.upc.viora.features.phenology.presentation

import androidx.lifecycle.SavedStateHandle
import java.time.Instant
import java.time.LocalDate
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
import pe.edu.upc.viora.features.phenology.application.usecase.ObserveChillTrackerUseCase
import pe.edu.upc.viora.features.phenology.application.usecase.RefreshChillTrackerUseCase
import pe.edu.upc.viora.features.phenology.chillTracker
import pe.edu.upc.viora.features.phenology.domain.entity.ChillCurvePoint
import pe.edu.upc.viora.features.phenology.domain.entity.ChillTracker
import pe.edu.upc.viora.features.phenology.domain.entity.PreviousChillSeason
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
    )

    private fun WinterChillViewModel.lastState(): WinterChillUiState {
        val seen = mutableListOf<WinterChillUiState>()
        val job = uiState.onEach { seen += it }.launchIn(CoroutineScope(Dispatchers.Main))
        job.cancel()
        return seen.last()
    }

    private fun WinterChillViewModel.content(): WinterChillUiState.Content = lastState() as WinterChillUiState.Content

    @Test
    fun refreshesOnceOnStart() = runTest {
        viewModel()
        assertEquals(1, repository.refreshCalls)
    }

    @Test
    fun showsTheCachedChillWithTheBackendThreshold() = runTest {
        repository.tracker.value = chillTracker(accumulated = 24.43, threshold = 30.0)

        val content = viewModel().content()

        assertEquals(24, content.accumulatedPortions)
        assertEquals(30, content.thresholdPortions)
        assertEquals(6, content.portionsRemaining)
        assertEquals(WinterSeasonState.ACCUMULATING, content.seasonState)
        assertFalse(content.offline)
    }

    @Test
    fun neverRoundsPortionsUpToTheThreshold() = runTest {
        repository.tracker.value = chillTracker(accumulated = 29.6)

        val content = viewModel().content()

        assertEquals(29, content.accumulatedPortions)
        assertEquals(1, content.portionsRemaining)
    }

    @Test
    fun offlineRefreshWithCacheMarksStateAsOffline() = runTest {
        repository.tracker.value = chillTracker(seasonState = WinterSeasonState.CHILL_HALTED)
        repository.refreshResult = AppResult.Failure(AppError.Offline)

        val content = viewModel().content()

        assertTrue(content.offline)
        assertEquals(WinterSeasonState.CHILL_HALTED, content.seasonState)
    }

    @Test
    fun failedInitialRefreshWithNothingCachedEmitsError() = runTest {
        repository.refreshResult = AppResult.Failure(AppError.Offline)

        val state = viewModel().lastState()

        assertTrue(state is WinterChillUiState.Error)
        assertEquals(AppError.Offline, (state as WinterChillUiState.Error).error)
    }

    @Test
    fun noChillFromTheBackendIsAnEmptyStateNotZeroPortions() = runTest {
        val state = viewModel().lastState()

        assertTrue(state is WinterChillUiState.Empty)
    }

    @Test
    fun comparesWithThePreviousWinterOnTheSameDay() = runTest {
        repository.tracker.value = chillTracker(
            accumulated = 12.0,
            evaluatedThrough = LocalDate.of(2026, 7, 15),
            curvePoints = listOf(ChillCurvePoint(LocalDate.of(2026, 7, 15), 12.0, 10.1)),
        )

        assertEquals(1.9, viewModel().content().differenceWithPreviousWinter!!, 0.001)
    }

    @Test
    fun noComparisonWhenThePreviousWinterHasNoData() = runTest {
        repository.tracker.value = chillTracker(previousSeason = null)

        assertNull(viewModel().content().differenceWithPreviousWinter)
        assertNull(viewModel().content().daysAheadOfPreviousWinter)
    }

    @Test
    fun countsTheDaysAheadOfThePreviousCompletion() = runTest {
        repository.tracker.value = chillTracker(
            accumulated = 30.2,
            seasonState = WinterSeasonState.COMPLETED,
            completionDate = LocalDate.of(2026, 8, 4),
            previousSeason = PreviousChillSeason(2025, 31.0, LocalDate.of(2025, 8, 18)),
        )

        assertEquals(14L, viewModel().content().daysAheadOfPreviousWinter)
    }

    @Test
    fun seasonProgressFollowsTheLastEvaluatedDay() = runTest {
        val curve = (0L until 92L).map { day -> ChillCurvePoint(LocalDate.of(2026, 6, 1).plusDays(day), null, null) }
        repository.tracker.value = chillTracker(evaluatedThrough = LocalDate.of(2026, 7, 16), curvePoints = curve)

        assertEquals(46f / 92f, viewModel().content().seasonProgress, 0.001f)
    }
}
