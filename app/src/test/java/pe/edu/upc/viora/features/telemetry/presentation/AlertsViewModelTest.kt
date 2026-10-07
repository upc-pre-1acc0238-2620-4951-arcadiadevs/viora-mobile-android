package pe.edu.upc.viora.features.telemetry.presentation

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.telemetry.application.usecase.ObserveIncidentsLastRefreshUseCase
import pe.edu.upc.viora.features.telemetry.application.usecase.ObserveIncidentsUseCase
import pe.edu.upc.viora.features.telemetry.application.usecase.RefreshIncidentsUseCase
import pe.edu.upc.viora.features.telemetry.domain.entity.AgroclimaticIncident
import pe.edu.upc.viora.features.telemetry.domain.entity.AlertsSummary
import pe.edu.upc.viora.features.telemetry.domain.entity.IncidentDetail
import pe.edu.upc.viora.features.telemetry.domain.repository.IncidentRepository
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentSeverity
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentStatus
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentType
import pe.edu.upc.viora.features.telemetry.presentation.state.AlertsUiState
import pe.edu.upc.viora.features.telemetry.presentation.viewmodel.AlertsViewModel

private class FakeIncidentRepo : IncidentRepository {
    val incidentsFlow = MutableStateFlow<List<AgroclimaticIncident>>(emptyList())
    val observedPlotIds = mutableListOf<String?>()
    val refreshedPlotIds = mutableListOf<String?>()
    val lastRefresh = MutableStateFlow<Long?>(null)

    /** When set, [refresh] suspends until it completes. */
    var gate: CompletableDeferred<Unit>? = null

    override fun observeLastRefresh(plotId: String?): Flow<Long?> = lastRefresh

    override fun observeIncidents(plotId: String?): Flow<List<AgroclimaticIncident>> {
        observedPlotIds += plotId
        return if (plotId == null) {
            incidentsFlow
        } else {
            incidentsFlow.map { list -> list.filter { it.plotId == plotId } }
        }
    }

    override suspend fun refresh(
        plotId: String?,
        status: String?,
        severity: String?,
    ): AppResult<AlertsSummary> {
        refreshedPlotIds += plotId
        gate?.await()
        return AppResult.Success(AlertsSummary(0, 0, 0, 0))
    }

    override suspend fun getIncidentDetail(incidentId: String): AppResult<IncidentDetail> =
        AppResult.Failure(AppError.Offline)

    override suspend fun postponeIncident(incidentId: String, durationHours: Int): AppResult<Unit> =
        AppResult.Success(Unit)

    override suspend fun completeMitigationStep(incidentId: String, stepId: String): AppResult<Unit> =
        AppResult.Success(Unit)

    override suspend fun getSummary(): AppResult<AlertsSummary> =
        AppResult.Success(AlertsSummary(0, 0, 0, 0))
}

@OptIn(ExperimentalCoroutinesApi::class)
class AlertsViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val fakeRepo = FakeIncidentRepo()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun incident(
        id: String,
        plotId: String,
        severity: IncidentSeverity = IncidentSeverity.CRITICAL,
        status: IncidentStatus = IncidentStatus.ACTIVE,
    ) = AgroclimaticIncident(
        id = id,
        plotId = plotId,
        plotName = "Plot $plotId",
        plotVariety = "Sevillana",
        type = IncidentType.HEAT_WAVE,
        severity = severity,
        status = status,
        headlineKey = "heat_stress",
        metricName = "temperature",
        currentValue = 36.0,
        thresholdValue = 32.0,
        unit = "°C",
        triggeredAt = "2026-10-06T10:00:00Z",
        stressDurationMinutes = 45,
    )

    private fun collecting(vm: AlertsViewModel) = CoroutineScope(dispatcher).launch { vm.uiState.collect {} }

    @Test
    fun `a plot without cached alerts that was synced before is empty at once, not loading`() = runTest {
        fakeRepo.lastRefresh.value = 1_000L
        fakeRepo.gate = CompletableDeferred()
        val vm = AlertsViewModel(
            savedStateHandle = SavedStateHandle(mapOf("plotId" to "plot-1")),
            observeIncidents = ObserveIncidentsUseCase(fakeRepo),
            refreshIncidents = RefreshIncidentsUseCase(fakeRepo),
            observeLastRefresh = ObserveIncidentsLastRefreshUseCase(fakeRepo),
        )
        val job = collecting(vm)

        assertTrue(vm.uiState.value is AlertsUiState.Empty)
        job.cancel()
    }

    @Test
    fun `a plot never synced keeps loading until the first refresh answers`() = runTest {
        fakeRepo.gate = CompletableDeferred()
        val vm = AlertsViewModel(
            savedStateHandle = SavedStateHandle(mapOf("plotId" to "plot-1")),
            observeIncidents = ObserveIncidentsUseCase(fakeRepo),
            refreshIncidents = RefreshIncidentsUseCase(fakeRepo),
            observeLastRefresh = ObserveIncidentsLastRefreshUseCase(fakeRepo),
        )
        val job = collecting(vm)

        assertEquals(AlertsUiState.Loading, vm.uiState.value)
        fakeRepo.gate?.complete(Unit)
        assertTrue(vm.uiState.value is AlertsUiState.Empty)
        job.cancel()
    }

    @Test
    fun `when plotId is provided, observes and refreshes incidents for that plot only`() = runTest {
        fakeRepo.incidentsFlow.value = listOf(
            incident("1", "plot-1", IncidentSeverity.CRITICAL),
            incident("2", "plot-2", IncidentSeverity.WARNING),
        )

        val handle = SavedStateHandle(mapOf("plotId" to "plot-1"))
        val viewModel = AlertsViewModel(
            savedStateHandle = handle,
            observeIncidents = ObserveIncidentsUseCase(fakeRepo),
            refreshIncidents = RefreshIncidentsUseCase(fakeRepo),
        )

        assertEquals("plot-1", viewModel.plotId)
        assertTrue(fakeRepo.observedPlotIds.contains("plot-1"))
        assertTrue(fakeRepo.refreshedPlotIds.contains("plot-1"))

        val states = mutableListOf<AlertsUiState>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { states += it }
        }

        val lastState = states.last()
        assertTrue(lastState is AlertsUiState.Content)
        val content = lastState as AlertsUiState.Content
        assertEquals(1, content.incidents.size)
        assertEquals("plot-1", content.incidents.first().plotId)

        job.cancel()
    }

    @Test
    fun `when plotId is null, observes and refreshes incidents for all plots`() = runTest {
        fakeRepo.incidentsFlow.value = listOf(
            incident("1", "plot-1", IncidentSeverity.CRITICAL),
            incident("2", "plot-2", IncidentSeverity.WARNING),
        )

        val handle = SavedStateHandle()
        val viewModel = AlertsViewModel(
            savedStateHandle = handle,
            observeIncidents = ObserveIncidentsUseCase(fakeRepo),
            refreshIncidents = RefreshIncidentsUseCase(fakeRepo),
        )

        assertEquals(null, viewModel.plotId)
        assertTrue(fakeRepo.observedPlotIds.contains(null))
        assertTrue(fakeRepo.refreshedPlotIds.contains(null))

        val states = mutableListOf<AlertsUiState>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { states += it }
        }

        val lastState = states.last()
        assertTrue(lastState is AlertsUiState.Content)
        val content = lastState as AlertsUiState.Content
        assertEquals(2, content.incidents.size)

        job.cancel()
    }
}
