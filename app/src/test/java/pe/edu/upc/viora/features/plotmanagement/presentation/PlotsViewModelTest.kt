package pe.edu.upc.viora.features.plotmanagement.presentation

import java.time.Instant
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObserveArchivedPlotsUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsLastRefreshUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.RefreshArchivedPlotsUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.RefreshPlotsUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.RestorePlotUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.entity.NewPlot
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.entity.PlotChanges
import pe.edu.upc.viora.features.plotmanagement.domain.repository.PlotRepository
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId
import pe.edu.upc.viora.features.plotmanagement.presentation.state.PlotsFilter
import pe.edu.upc.viora.features.plotmanagement.presentation.state.PlotsUiState
import pe.edu.upc.viora.features.plotmanagement.presentation.viewmodel.PlotsViewModel

private class FakePlotRepository : PlotRepository {
    val plots = MutableStateFlow<List<Plot>>(emptyList())
    val lastRefresh = MutableStateFlow<Instant?>(null)
    var refreshCalls = 0
    var archivedRefreshCalls = 0
    val archived = MutableStateFlow<List<Plot>>(emptyList())
    val restored = mutableListOf<PlotId>()
    var restoreResult: (Plot) -> AppResult<Plot> = { AppResult.Success(it) }
    var pendingRefresh: CompletableDeferred<AppResult<Unit>> = CompletableDeferred(AppResult.Success(Unit))

    override fun observePlots(): Flow<List<Plot>> = plots
    override fun observePlot(id: PlotId): Flow<Plot?> = MutableStateFlow(null)
    override fun observeArchivedPlots(): Flow<List<Plot>> = archived
    override fun observeLastRefresh(): Flow<Instant?> = lastRefresh
    override suspend fun register(newPlot: NewPlot): AppResult<Plot> = AppResult.Failure(AppError.Offline)
    override suspend fun update(id: PlotId, changes: PlotChanges): AppResult<Plot> = AppResult.Failure(AppError.Offline)
    override suspend fun archive(id: PlotId): AppResult<Unit> = AppResult.Failure(AppError.Offline)
    override suspend fun restore(id: PlotId): AppResult<Plot> {
        restored += id
        val plot = archived.value.first { it.id == id }
        return restoreResult(plot).also { result ->
            // The real repository stores the restored plot, which moves it between the two lists.
            if (result is AppResult.Success) {
                archived.value = archived.value - plot
                plots.value = plots.value + plot.copy(isActive = true)
            }
        }
    }

    override suspend fun refresh(): AppResult<Unit> {
        refreshCalls++
        return pendingRefresh.await()
    }

    override suspend fun refreshArchived(): AppResult<Unit> {
        archivedRefreshCalls++
        return AppResult.Success(Unit)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class PlotsViewModelTest {

    private val repository = FakePlotRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun plot(name: String) = Plot(
        id = PlotId(name),
        name = name,
        variety = OliveVariety.SEVILLANA,
        areaHectares = 2.5,
        treesPerHectare = 72,
        rowSpacingMeters = 7.0,
        treeSpacingMeters = 5.0,
        outline = emptyList(),
        lastPruningDate = null,
        isActive = true,
        revision = 0,
    )

    private fun viewModel() = PlotsViewModel(
        observePlots = ObservePlotsUseCase(repository),
        observeLastRefresh = ObservePlotsLastRefreshUseCase(repository),
        refreshPlots = RefreshPlotsUseCase(repository),
        observeArchivedPlots = ObserveArchivedPlotsUseCase(repository),
        refreshArchivedPlots = RefreshArchivedPlotsUseCase(repository),
        restorePlot = RestorePlotUseCase(repository),
    )

    /** Keeps the StateFlow hot (it uses WhileSubscribed) for the duration of the test. */
    private fun kotlinx.coroutines.test.TestScope.collecting(vm: PlotsViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect { } }
    }

    @Test
    fun `shows loading while nothing is cached and the first download runs`() = runTest {
        repository.pendingRefresh = CompletableDeferred()

        val vm = viewModel()
        collecting(vm)

        assertEquals(PlotsUiState.Loading, vm.uiState.value)
    }

    @Test
    fun `an empty account shows the empty state after a successful download`() = runTest {
        val vm = viewModel()
        collecting(vm)

        assertEquals(PlotsUiState.Empty, vm.uiState.value)
    }

    @Test
    fun `no cache and no connection shows the error state`() = runTest {
        repository.pendingRefresh = CompletableDeferred(AppResult.Failure(AppError.Offline))

        val vm = viewModel()
        collecting(vm)

        assertEquals(PlotsUiState.Error(AppError.Offline), vm.uiState.value)
    }

    @Test
    fun `cached plots are shown and flag the download failure without hiding them`() = runTest {
        repository.plots.value = listOf(plot("Alfa"))
        repository.pendingRefresh = CompletableDeferred(AppResult.Failure(AppError.Timeout))

        val vm = viewModel()
        collecting(vm)

        val content = vm.uiState.value as PlotsUiState.Content
        assertEquals(listOf("Alfa"), content.plots.map { it.name })
        assertEquals(AppError.Timeout, content.refreshError)
        assertEquals(false, content.isRefreshing)
    }

    @Test
    fun `content exposes the last refresh time and reacts to cache updates`() = runTest {
        val vm = viewModel()
        collecting(vm)

        repository.plots.value = listOf(plot("Alfa"), plot("Beta"))
        repository.lastRefresh.value = Instant.parse("2026-10-01T12:00:00Z")

        val content = vm.uiState.value as PlotsUiState.Content
        assertEquals(2, content.plots.size)
        assertEquals(Instant.parse("2026-10-01T12:00:00Z"), content.lastRefresh)
    }

    @Test
    fun `refresh is ignored while a download is running and works again afterwards`() = runTest {
        repository.plots.value = listOf(plot("Alfa"))
        repository.pendingRefresh = CompletableDeferred()
        val vm = viewModel()
        collecting(vm)
        assertTrue((vm.uiState.value as PlotsUiState.Content).isRefreshing)

        vm.refresh()
        vm.refresh()
        assertEquals(1, repository.refreshCalls)

        repository.pendingRefresh.complete(AppResult.Success(Unit))
        assertEquals(false, (vm.uiState.value as PlotsUiState.Content).isRefreshing)

        repository.pendingRefresh = CompletableDeferred(AppResult.Success(Unit))
        vm.refresh()
        assertEquals(2, repository.refreshCalls)
    }

    private fun archivedPlot(name: String) = plot(name).copy(isActive = false)

    private fun PlotsViewModel.content() = uiState.value as PlotsUiState.Content

    @Test
    fun `reads the archived plots together with the active ones on every refresh`() = runTest {
        repository.plots.value = listOf(plot("Alfa"))
        val vm = viewModel()
        collecting(vm)
        assertEquals(1, repository.archivedRefreshCalls)

        vm.refresh()

        assertEquals(2, repository.archivedRefreshCalls)
    }

    @Test
    fun `lists the archived plots next to the active ones and starts on the active ones`() = runTest {
        repository.plots.value = listOf(plot("Alfa"))
        repository.archived.value = listOf(archivedPlot("Beta"))
        val vm = viewModel()
        collecting(vm)

        val content = vm.content()

        assertEquals(PlotsFilter.ACTIVE, content.filter)
        assertEquals(listOf("Alfa"), content.plots.map { it.name })
        assertEquals(listOf("Beta"), content.archivedPlots.map { it.name })
    }

    @Test
    fun `a producer with only archived plots still gets the list`() = runTest {
        repository.archived.value = listOf(archivedPlot("Beta"))
        val vm = viewModel()
        collecting(vm)

        assertEquals(emptyList<Plot>(), vm.content().plots)
        assertEquals(1, vm.content().archivedPlots.size)
    }

    @Test
    fun `selecting the archived filter shows them and with nothing archived it stays on the active ones`() = runTest {
        repository.plots.value = listOf(plot("Alfa"))
        val vm = viewModel()
        collecting(vm)

        vm.selectFilter(PlotsFilter.ARCHIVED)
        assertEquals(PlotsFilter.ACTIVE, vm.content().filter)

        repository.archived.value = listOf(archivedPlot("Beta"))
        assertEquals(PlotsFilter.ARCHIVED, vm.content().filter)
    }

    @Test
    fun `restoring a plot moves it to the active list and returns to it when it was the last archived one`() = runTest {
        repository.plots.value = listOf(plot("Alfa"))
        repository.archived.value = listOf(archivedPlot("Beta"))
        val vm = viewModel()
        collecting(vm)
        vm.selectFilter(PlotsFilter.ARCHIVED)

        vm.restore(PlotId("Beta"))

        assertEquals(listOf(PlotId("Beta")), repository.restored)
        val content = vm.content()
        assertEquals(PlotsFilter.ACTIVE, content.filter)
        assertEquals(listOf("Alfa", "Beta"), content.plots.map { it.name })
        assertEquals(null, content.restoringId)
        assertEquals(null, content.restoreError)
    }

    @Test
    fun `a failed restore keeps the plot archived and shows why`() = runTest {
        repository.plots.value = listOf(plot("Alfa"))
        repository.archived.value = listOf(archivedPlot("Beta"))
        repository.restoreResult = { AppResult.Failure(AppError.Offline) }
        val vm = viewModel()
        collecting(vm)
        vm.selectFilter(PlotsFilter.ARCHIVED)

        vm.restore(PlotId("Beta"))

        val content = vm.content()
        assertEquals(PlotsFilter.ARCHIVED, content.filter)
        assertEquals(AppError.Offline, content.restoreError)
        assertEquals(null, content.restoringId)

        vm.selectFilter(PlotsFilter.ACTIVE)
        assertEquals(null, vm.content().restoreError)
    }
}
