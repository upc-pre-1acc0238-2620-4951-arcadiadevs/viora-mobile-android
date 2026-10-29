package pe.edu.upc.viora.features.telemetry.presentation

import androidx.lifecycle.SavedStateHandle
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.phenology.application.usecase.ObserveChillTrackerUseCase
import pe.edu.upc.viora.features.phenology.application.usecase.RefreshChillTrackerUseCase
import pe.edu.upc.viora.features.phenology.chillTracker
import pe.edu.upc.viora.features.phenology.application.published.ChillTracker
import pe.edu.upc.viora.features.phenology.application.published.WinterSeasonState
import pe.edu.upc.viora.features.phenology.domain.repository.ChillRepository
import pe.edu.upc.viora.features.telemetry.FakeWarmWinterIncidents
import pe.edu.upc.viora.features.telemetry.application.usecase.CompleteMitigationStepUseCase
import pe.edu.upc.viora.features.telemetry.application.usecase.GetIncidentDetailUseCase
import pe.edu.upc.viora.features.telemetry.application.usecase.PostponeIncidentUseCase
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentType
import pe.edu.upc.viora.features.telemetry.presentation.state.AlertDetailUiState
import pe.edu.upc.viora.features.telemetry.presentation.state.ChillFact
import pe.edu.upc.viora.features.telemetry.presentation.state.warmStreakDays
import pe.edu.upc.viora.features.telemetry.presentation.viewmodel.AlertDetailViewModel
import pe.edu.upc.viora.features.telemetry.warmWinterDetail

private class FakeAlertChill : ChillRepository {
    val tracker = MutableStateFlow<ChillTracker?>(null)
    var refreshCalls = 0

    override fun observeChillTracker(plotId: String): Flow<ChillTracker?> = tracker
    override fun observeLastSync(plotId: String): Flow<Instant?> = MutableStateFlow(null)
    override suspend fun refresh(plotId: String): AppResult<Unit> {
        refreshCalls++
        return AppResult.Success(Unit)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class WarmWinterAlertTest {

    private val incidents = FakeWarmWinterIncidents()
    private val chill = FakeAlertChill()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = AlertDetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf("incidentId" to "ww-1")),
        getIncidentDetail = GetIncidentDetailUseCase(incidents),
        postponeIncidentUseCase = PostponeIncidentUseCase(incidents),
        completeMitigationStepUseCase = CompleteMitigationStepUseCase(incidents),
        observeChillTracker = ObserveChillTrackerUseCase(chill),
        refreshChillTracker = RefreshChillTrackerUseCase(chill),
    )

    private fun AlertDetailViewModel.content() = uiState.value as AlertDetailUiState.Content

    @Test
    fun `the streak counts the warm days back from the newest one`() {
        assertEquals(4, warmStreakDays(warmWinterDetail().weeklyTrend, 24.0))
        assertEquals(0, warmStreakDays(warmWinterDetail(listOf(25.0, 26.0, 23.0)).weeklyTrend, 24.0))
    }

    @Test
    fun `a day exactly at the threshold is not warm`() {
        assertEquals(1, warmStreakDays(warmWinterDetail(listOf(25.0, 24.0, 25.0)).weeklyTrend, 24.0))
    }

    @Test
    fun `a warm winter alert reads the chill of its plot`() = runTest {
        incidents.detail = AppResult.Success(warmWinterDetail())
        chill.tracker.value = chillTracker(accumulated = 21.4, seasonState = WinterSeasonState.CHILL_HALTED)
            .copy(currentWarmStreakDays = 4)

        val vm = viewModel()

        assertEquals(1, chill.refreshCalls)
        assertEquals(ChillFact.Halted(days = 4, portions = 21, threshold = 30), vm.content().chillFact)
    }

    @Test
    fun `a chill still counting is at risk, not halted`() = runTest {
        incidents.detail = AppResult.Success(warmWinterDetail())
        chill.tracker.value = chillTracker(accumulated = 12.9)

        assertEquals(ChillFact.AtRisk(days = 4, portions = 12, threshold = 30), viewModel().content().chillFact)
    }

    @Test
    fun `without the chill only the warm days are said`() = runTest {
        incidents.detail = AppResult.Success(warmWinterDetail())

        assertEquals(ChillFact.Unknown(days = 4), viewModel().content().chillFact)
    }

    @Test
    fun `other alerts never read the chill`() = runTest {
        incidents.detail = AppResult.Success(warmWinterDetail().copy(type = IncidentType.HEAT_WAVE))
        chill.tracker.value = chillTracker()

        val vm = viewModel()

        assertEquals(0, chill.refreshCalls)
        assertNull(vm.content().chill)
    }
}
