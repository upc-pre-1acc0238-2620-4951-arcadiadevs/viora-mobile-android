package pe.edu.upc.viora.features.plotmanagement.presentation

import androidx.lifecycle.SavedStateHandle
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.phenology.application.usecase.ObserveBearingIndexUseCase
import pe.edu.upc.viora.features.phenology.application.usecase.ObserveHarvestHistoryUseCase
import pe.edu.upc.viora.features.phenology.application.usecase.ObserveHarvestLastRefreshUseCase
import pe.edu.upc.viora.features.phenology.application.usecase.RefreshHarvestHistoryUseCase
import pe.edu.upc.viora.features.phenology.domain.entity.BearingIndex
import pe.edu.upc.viora.features.phenology.presentation.FakeHarvestRepository
import pe.edu.upc.viora.features.phenology.presentation.figmaRecords
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ArchivePlotUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.entity.NewPlot
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.entity.PlotChanges
import pe.edu.upc.viora.features.plotmanagement.domain.repository.PlotRepository
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId
import pe.edu.upc.viora.features.plotmanagement.presentation.state.ArchiveState
import pe.edu.upc.viora.features.plotmanagement.presentation.state.LotHarvest
import pe.edu.upc.viora.features.plotmanagement.presentation.state.PlotDetailUiState
import pe.edu.upc.viora.features.plotmanagement.presentation.viewmodel.PlotDetailViewModel

private class CachedPlotsRepository : PlotRepository {
    val plots = MutableStateFlow<List<Plot>>(emptyList())

    override fun observePlots(): Flow<List<Plot>> = plots
    override fun observePlot(id: PlotId): Flow<Plot?> = plots.map { all -> all.firstOrNull { it.id == id } }
    override fun observeArchivedPlots(): Flow<List<Plot>> = MutableStateFlow(emptyList())
    override fun observeLastRefresh(): Flow<Instant?> = MutableStateFlow(null)
    override suspend fun refresh(): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun refreshArchived(): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun register(newPlot: NewPlot): AppResult<Plot> = AppResult.Failure(AppError.Offline)
    override suspend fun update(id: PlotId, changes: PlotChanges): AppResult<Plot> = AppResult.Failure(AppError.Offline)
    var archiveResult: AppResult<Unit> = AppResult.Success(Unit)
    val archived = mutableListOf<PlotId>()

    override suspend fun archive(id: PlotId): AppResult<Unit> {
        archived += id
        return archiveResult
    }
    override suspend fun restore(id: PlotId): AppResult<Plot> = AppResult.Failure(AppError.Offline)
}

@OptIn(ExperimentalCoroutinesApi::class)
class PlotDetailViewModelTest {

    private val repository = CachedPlotsRepository()
    private val harvests = FakeHarvestRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun plot(id: String) = Plot(
        id = PlotId(id),
        name = "La Yarada 03",
        variety = OliveVariety.SEVILLANA,
        areaHectares = 0.92,
        treesPerHectare = 204,
        rowSpacingMeters = 7.0,
        treeSpacingMeters = 5.0,
        outline = emptyList(),
        lastPruningDate = null,
        isActive = true,
        revision = 0,
    )

    private fun viewModel(plotId: String, justSaved: Boolean = false) = PlotDetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf("plotId" to plotId, "justSaved" to justSaved)),
        observePlot = ObservePlotUseCase(repository),
        observePlots = ObservePlotsUseCase(repository),
        archivePlot = ArchivePlotUseCase(repository),
        observeHarvests = ObserveHarvestHistoryUseCase(harvests),
        observeBearingIndex = ObserveBearingIndexUseCase(harvests),
        observeHarvestRefresh = ObserveHarvestLastRefreshUseCase(harvests),
        refreshHarvests = RefreshHarvestHistoryUseCase(harvests),
    )

    private fun contentOf(vm: PlotDetailViewModel, scope: kotlinx.coroutines.test.TestScope): PlotDetailUiState.Content {
        scope.backgroundScope.launch(UnconfinedTestDispatcher(scope.testScheduler)) { vm.uiState.collect {} }
        return vm.uiState.value as PlotDetailUiState.Content
    }

    @Test
    fun `the alternation card has no line until the history is known`() = runTest {
        repository.plots.value = listOf(plot("p1"))

        assertNull(contentOf(viewModel("p1"), this).harvest)
    }

    @Test
    fun `with fewer than three campaigns the card says how many are missing`() = runTest {
        repository.plots.value = listOf(plot("p1"))
        harvests.records.value = figmaRecords().takeLast(2)
        harvests.lastRefresh.value = Instant.parse("2026-10-01T00:00:00Z")

        val harvest = contentOf(viewModel("p1"), this).harvest

        assertEquals(LotHarvest(index = null, missingCampaigns = 1), harvest)
    }

    @Test
    fun `with enough campaigns the card shows the server index`() = runTest {
        repository.plots.value = listOf(plot("p1"))
        harvests.records.value = figmaRecords()
        harvests.index.value = BearingIndex(0.51, 4, null)
        harvests.lastRefresh.value = Instant.parse("2026-10-01T00:00:00Z")

        val harvest = contentOf(viewModel("p1"), this).harvest

        assertEquals(LotHarvest(index = 0.51, missingCampaigns = 0), harvest)
    }

    @Test
    fun `shows the cached plot`() = runTest {
        repository.plots.value = listOf(plot("p1"), plot("p2"))
        val vm = viewModel("p2")
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        val state = vm.uiState.value as PlotDetailUiState.Content

        assertEquals(PlotId("p2"), state.plot.id)
        assertEquals(false, state.showSavedNotice)
    }

    @Test
    fun `reports a plot that is not cached as not found`() = runTest {
        val vm = viewModel("missing")
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        assertEquals(PlotDetailUiState.NotFound, vm.uiState.value)
    }

    @Test
    fun `follows the cache when the plot changes`() = runTest {
        repository.plots.value = listOf(plot("p1"))
        val vm = viewModel("p1")
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        repository.plots.value = listOf(plot("p1").copy(name = "Renamed"))

        assertEquals("Renamed", (vm.uiState.value as PlotDetailUiState.Content).plot.name)
    }

    @Test
    fun `the saved notice shows right after registering and goes away by itself`() = runTest {
        repository.plots.value = listOf(plot("p1"))
        val vm = viewModel("p1", justSaved = true)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        assertTrue((vm.uiState.value as PlotDetailUiState.Content).showSavedNotice)

        advanceTimeBy(3_001)

        assertEquals(false, (vm.uiState.value as PlotDetailUiState.Content).showSavedNotice)
    }

    @Test
    fun `archiving asks the repository and finishes as Done`() = runTest {
        repository.plots.value = listOf(plot("p1"))
        val vm = viewModel("p1")

        assertEquals(ArchiveState.Idle, vm.archiveState.value)
        vm.archive()

        assertEquals(listOf(PlotId("p1")), repository.archived)
        assertEquals(ArchiveState.Done, vm.archiveState.value)
    }

    @Test
    fun `a failed archive can be dismissed and tried again`() = runTest {
        repository.plots.value = listOf(plot("p1"))
        repository.archiveResult = AppResult.Failure(AppError.Offline)
        val vm = viewModel("p1")

        vm.archive()
        assertEquals(ArchiveState.Failed, vm.archiveState.value)

        vm.dismissArchiveFailure()
        assertEquals(ArchiveState.Idle, vm.archiveState.value)

        repository.archiveResult = AppResult.Success(Unit)
        vm.archive()
        assertEquals(ArchiveState.Done, vm.archiveState.value)
    }

    @Test
    fun `reports the hectares of all the active plots to show what archiving frees`() = runTest {
        repository.plots.value = listOf(plot("p1").copy(areaHectares = 1.5), plot("p2").copy(areaHectares = 2.5))
        val vm = viewModel("p2")
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        assertEquals(4.0, (vm.uiState.value as PlotDetailUiState.Content).activeHectares, 0.0)
    }
}
