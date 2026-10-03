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
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsLastRefreshUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.RefreshPlotsUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.entity.NewPlot
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.repository.PlotRepository
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId
import pe.edu.upc.viora.features.plotmanagement.presentation.state.PlotsUiState
import pe.edu.upc.viora.features.plotmanagement.presentation.viewmodel.PlotsViewModel

private class FakePlotRepository : PlotRepository {
    val plots = MutableStateFlow<List<Plot>>(emptyList())
    val lastRefresh = MutableStateFlow<Instant?>(null)
    var refreshCalls = 0
    var pendingRefresh: CompletableDeferred<AppResult<Unit>> = CompletableDeferred(AppResult.Success(Unit))

    override fun observePlots(): Flow<List<Plot>> = plots
    override fun observePlot(id: PlotId): Flow<Plot?> = MutableStateFlow(null)
    override fun observeLastRefresh(): Flow<Instant?> = lastRefresh
    override suspend fun register(newPlot: NewPlot): AppResult<Plot> = AppResult.Failure(AppError.Offline)

    override suspend fun refresh(): AppResult<Unit> {
        refreshCalls++
        return pendingRefresh.await()
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
}
