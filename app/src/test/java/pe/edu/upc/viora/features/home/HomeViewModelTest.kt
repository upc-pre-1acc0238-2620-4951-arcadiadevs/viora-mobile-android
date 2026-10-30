package pe.edu.upc.viora.features.home

import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.domain.OliveVariety
import pe.edu.upc.viora.core.domain.PlotId
import pe.edu.upc.viora.features.home.application.usecase.ChoosePlotUseCase
import pe.edu.upc.viora.features.home.application.usecase.ObserveChosenPlotUseCase
import pe.edu.upc.viora.features.home.domain.repository.FocusedPlotRepository
import pe.edu.upc.viora.features.home.presentation.state.HomeUiState
import pe.edu.upc.viora.features.home.presentation.viewmodel.HomeViewModel
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsLastRefreshUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.PickFocusedPlotUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.RefreshPlotsUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.entity.NewPlot
import pe.edu.upc.viora.features.plotmanagement.application.published.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.entity.PlotChanges
import pe.edu.upc.viora.features.plotmanagement.domain.repository.PlotRepository
import pe.edu.upc.viora.features.telemetry.application.usecase.ObserveIncidentsUseCase
import pe.edu.upc.viora.features.telemetry.application.usecase.RefreshIncidentsUseCase
import pe.edu.upc.viora.features.telemetry.domain.entity.AgroclimaticIncident
import pe.edu.upc.viora.features.telemetry.domain.entity.AlertsSummary
import pe.edu.upc.viora.features.telemetry.domain.entity.IncidentDetail
import pe.edu.upc.viora.features.telemetry.domain.repository.IncidentRepository
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentSeverity
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentStatus
import pe.edu.upc.viora.features.telemetry.application.published.IncidentType

private class CachedPlots : PlotRepository {
    val plots = MutableStateFlow<List<Plot>>(emptyList())

    override fun observePlots(): Flow<List<Plot>> = plots
    override fun observePlot(id: PlotId): Flow<Plot?> = plots.map { all -> all.firstOrNull { it.id == id } }
    override fun observeArchivedPlots(): Flow<List<Plot>> = MutableStateFlow(emptyList())
    override fun observeLastRefresh(): Flow<Instant?> = MutableStateFlow(null)
    override suspend fun refresh(): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun refreshArchived(): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun register(newPlot: NewPlot): AppResult<Plot> = AppResult.Failure(AppError.Offline)
    override suspend fun update(id: PlotId, changes: PlotChanges): AppResult<Plot> = AppResult.Failure(AppError.Offline)
    override suspend fun archive(id: PlotId): AppResult<Unit> = AppResult.Failure(AppError.Offline)
    override suspend fun restore(id: PlotId): AppResult<Plot> = AppResult.Failure(AppError.Offline)
}

private class FakeFocus : FocusedPlotRepository {
    val chosen = MutableStateFlow<String?>(null)
    override val chosenPlotId: Flow<String?> = chosen
    override suspend fun choose(plotId: String) {
        chosen.value = plotId
    }
}

private class FakeIncidentRepository : IncidentRepository {
    val incidents = MutableStateFlow<List<AgroclimaticIncident>>(emptyList())
    var refreshCalls = 0
    val refreshedPlots = mutableListOf<String?>()

    override fun observeIncidents(plotId: String?): Flow<List<AgroclimaticIncident>> =
        if (plotId == null) incidents else incidents.map { list -> list.filter { it.plotId == plotId } }

    override suspend fun refresh(plotId: String?, status: String?, severity: String?): AppResult<AlertsSummary> {
        refreshCalls++
        refreshedPlots += plotId
        return AppResult.Success(AlertsSummary(0, 0, 0, 0))
    }
    override suspend fun getIncidentDetail(incidentId: String): AppResult<IncidentDetail> = AppResult.Failure(AppError.Offline)
    override suspend fun postponeIncident(incidentId: String, durationHours: Int): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun completeMitigationStep(incidentId: String, stepId: String): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun getSummary(): AppResult<AlertsSummary> = AppResult.Success(AlertsSummary(0, 0, 0, 0))
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val plots = CachedPlots()
    private val focus = FakeFocus()
    private val incidentsRepo = FakeIncidentRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun plot(id: String, name: String, hectares: Double) = Plot(
        id = PlotId(id),
        name = name,
        variety = OliveVariety.SEVILLANA,
        areaHectares = hectares,
        treesPerHectare = 72,
        rowSpacingMeters = 7.0,
        treeSpacingMeters = 4.0,
        outline = emptyList(),
        lastPruningDate = null,
        isActive = true,
        revision = 0,
    )

    private fun viewModel() = HomeViewModel(
        observePlots = ObservePlotsUseCase(plots),
        observeLastRefresh = ObservePlotsLastRefreshUseCase(plots),
        observeIncidents = ObserveIncidentsUseCase(incidentsRepo),
        refreshPlots = RefreshPlotsUseCase(plots),
        refreshIncidents = RefreshIncidentsUseCase(incidentsRepo),
        observeChosenPlot = ObserveChosenPlotUseCase(focus),
        choosePlot = ChoosePlotUseCase(focus),
        pickFocusedPlot = PickFocusedPlotUseCase(),
    )

    private fun TestScope.contentOf(vm: HomeViewModel): HomeUiState.Content {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
        return vm.uiState.value as HomeUiState.Content
    }

    @Test
    fun `with several plots the biggest one is in focus until the producer chooses`() = runTest {
        plots.plots.value = listOf(plot("n", "Lote Norte", 1.5), plot("y", "La Yarada 02", 2.5))
        val vm = viewModel()

        assertEquals("La Yarada 02", contentOf(vm).focusedPlot?.name)

        vm.focusPlot(PlotId("n"))

        assertEquals("Lote Norte", (vm.uiState.value as HomeUiState.Content).focusedPlot?.name)
        assertEquals("n", focus.chosen.value)
    }

    private fun incident(
        id: String,
        plotId: String,
        status: IncidentStatus = IncidentStatus.ACTIVE,
    ) = AgroclimaticIncident(
        id = id,
        plotId = plotId,
        plotName = "Plot $plotId",
        plotVariety = "Sevillana",
        type = IncidentType.HEAT_WAVE,
        severity = IncidentSeverity.WARNING,
        status = status,
        headlineKey = "heat_warning",
        metricName = "temperature",
        currentValue = 35.0,
        thresholdValue = 32.0,
        unit = "°C",
        triggeredAt = "2026-10-06T12:00:00Z",
        stressDurationMinutes = 60,
    )

    @Test
    fun `active alerts count every plot and does not change with the focused plot`() = runTest {
        plots.plots.value = listOf(plot("y", "La Yarada 02", 2.5), plot("n", "Lote Norte", 1.5))
        incidentsRepo.incidents.value = listOf(
            incident("inc-1", "y", IncidentStatus.ACTIVE),
            incident("inc-2", "y", IncidentStatus.SNOOZED),
            incident("inc-3", "n", IncidentStatus.ACTIVE),
        )
        val vm = viewModel()

        // "y" is focused (largest plot), but the alert of "n" counts too: 3 active alerts
        assertEquals(3L, contentOf(vm).activeAlertsCount)

        // Changing focus to "n" keeps the same count
        vm.focusPlot(PlotId("n"))
        assertEquals(3L, (vm.uiState.value as HomeUiState.Content).activeAlertsCount)
    }

    @Test
    fun `the incidents of every plot are refreshed`() = runTest {
        plots.plots.value = listOf(plot("y", "La Yarada 02", 2.5))
        val vm = viewModel()
        contentOf(vm)

        assertEquals(listOf<String?>(null), incidentsRepo.refreshedPlots)
    }
}
