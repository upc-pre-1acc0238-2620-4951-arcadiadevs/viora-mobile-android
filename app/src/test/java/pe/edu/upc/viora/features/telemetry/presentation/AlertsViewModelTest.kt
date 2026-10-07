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
import pe.edu.upc.viora.features.telemetry.presentation.state.AlertsFilter
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
    fun `a plotId argument preselects that plot but every plot is still observed and refreshed`() = runTest {
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

        assertEquals(listOf<String?>(null), fakeRepo.observedPlotIds)
        assertEquals(listOf<String?>(null), fakeRepo.refreshedPlotIds)

        val states = mutableListOf<AlertsUiState>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { states += it }
        }

        val lastState = states.last()
        assertTrue(lastState is AlertsUiState.Content)
        val content = lastState as AlertsUiState.Content
        assertEquals(1, content.incidents.size)
        assertEquals("plot-1", content.incidents.first().plotId)
        assertEquals("plot-1", content.selectedPlotId)
        assertEquals(2, content.plotOptions.size)

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

    private fun contentOf(vm: AlertsViewModel): AlertsUiState.Content = vm.uiState.value as AlertsUiState.Content

    @Test
    fun `choosing a plot narrows the alerts and counts the capsules of that plot only`() = runTest {
        fakeRepo.incidentsFlow.value = listOf(
            incident("1", "plot-1", IncidentSeverity.CRITICAL),
            incident("2", "plot-2", IncidentSeverity.WARNING),
            incident("3", "plot-2", IncidentSeverity.CRITICAL),
        )
        val vm = AlertsViewModel(
            savedStateHandle = SavedStateHandle(),
            observeIncidents = ObserveIncidentsUseCase(fakeRepo),
            refreshIncidents = RefreshIncidentsUseCase(fakeRepo),
        )
        val job = collecting(vm)

        assertEquals(3L, contentOf(vm).summary.activeCount)
        assertTrue(contentOf(vm).canFilterByPlot)

        vm.selectPlot("plot-2")
        val content = contentOf(vm)
        assertEquals(listOf("2", "3"), content.incidents.map { it.id })
        assertEquals(2L, content.summary.activeCount)
        assertEquals(1L, content.summary.criticalCount)
        assertEquals(1L, content.summary.warningCount)
        assertEquals(listOf("Plot plot-2"), content.affectedPlotNames)

        vm.selectPlot(null)
        assertEquals(3, contentOf(vm).incidents.size)
        job.cancel()
    }

    @Test
    fun `the severity capsules filter inside the chosen plot`() = runTest {
        fakeRepo.incidentsFlow.value = listOf(
            incident("1", "plot-1", IncidentSeverity.CRITICAL),
            incident("2", "plot-2", IncidentSeverity.WARNING),
            incident("3", "plot-2", IncidentSeverity.CRITICAL),
        )
        val vm = AlertsViewModel(
            savedStateHandle = SavedStateHandle(),
            observeIncidents = ObserveIncidentsUseCase(fakeRepo),
            refreshIncidents = RefreshIncidentsUseCase(fakeRepo),
        )
        val job = collecting(vm)

        vm.selectPlot("plot-2")
        vm.setFilter(AlertsFilter.CRITICAL)

        assertEquals(listOf("3"), contentOf(vm).incidents.map { it.id })
        job.cancel()
    }

    @Test
    fun `plot options put the plots with critical alerts first`() = runTest {
        fakeRepo.incidentsFlow.value = listOf(
            incident("1", "plot-a", IncidentSeverity.WARNING),
            incident("2", "plot-b", IncidentSeverity.CRITICAL),
        )
        val vm = AlertsViewModel(
            savedStateHandle = SavedStateHandle(),
            observeIncidents = ObserveIncidentsUseCase(fakeRepo),
            refreshIncidents = RefreshIncidentsUseCase(fakeRepo),
        )
        val job = collecting(vm)

        val options = contentOf(vm).plotOptions
        assertEquals(listOf("plot-b", "plot-a"), options.map { it.plotId })
        assertEquals(IncidentSeverity.CRITICAL, options.first().worstSeverity)
        job.cancel()
    }

    @Test
    fun `a chosen plot without alerts falls back to every plot`() = runTest {
        fakeRepo.incidentsFlow.value = listOf(
            incident("1", "plot-1", IncidentSeverity.CRITICAL),
            incident("2", "plot-2", IncidentSeverity.WARNING),
        )
        val vm = AlertsViewModel(
            savedStateHandle = SavedStateHandle(mapOf("plotId" to "plot-9")),
            observeIncidents = ObserveIncidentsUseCase(fakeRepo),
            refreshIncidents = RefreshIncidentsUseCase(fakeRepo),
        )
        val job = collecting(vm)

        assertEquals(null, contentOf(vm).selectedPlotId)
        assertEquals(2, contentOf(vm).incidents.size)
        job.cancel()
    }

    @Test
    fun `a single plot with alerts hides the plot filter button`() = runTest {
        fakeRepo.incidentsFlow.value = listOf(incident("1", "plot-1", IncidentSeverity.CRITICAL))
        val vm = AlertsViewModel(
            savedStateHandle = SavedStateHandle(),
            observeIncidents = ObserveIncidentsUseCase(fakeRepo),
            refreshIncidents = RefreshIncidentsUseCase(fakeRepo),
        )
        val job = collecting(vm)

        assertTrue(!contentOf(vm).canFilterByPlot)
        job.cancel()
    }
}
