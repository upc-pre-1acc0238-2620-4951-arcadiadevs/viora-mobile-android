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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.entity.NewPlot
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.entity.PlotChanges
import pe.edu.upc.viora.features.plotmanagement.domain.repository.PlotRepository
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId
import pe.edu.upc.viora.features.plotmanagement.presentation.state.PlotDetailUiState
import pe.edu.upc.viora.features.plotmanagement.presentation.viewmodel.PlotDetailViewModel

private class CachedPlotsRepository : PlotRepository {
    val plots = MutableStateFlow<List<Plot>>(emptyList())

    override fun observePlots(): Flow<List<Plot>> = plots
    override fun observePlot(id: PlotId): Flow<Plot?> = plots.map { all -> all.firstOrNull { it.id == id } }
    override fun observeLastRefresh(): Flow<Instant?> = MutableStateFlow(null)
    override suspend fun refresh(): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun register(newPlot: NewPlot): AppResult<Plot> = AppResult.Failure(AppError.Offline)
    override fun observeArchivedPlots(): Flow<List<Plot>> = MutableStateFlow(emptyList())
    override suspend fun refreshArchived(): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun update(id: PlotId, changes: PlotChanges): AppResult<Plot> = AppResult.Failure(AppError.Offline)
    override suspend fun archive(id: PlotId): AppResult<Unit> = AppResult.Failure(AppError.Offline)
    override suspend fun restore(id: PlotId): AppResult<Plot> = AppResult.Failure(AppError.Offline)
}

@OptIn(ExperimentalCoroutinesApi::class)
class PlotDetailViewModelTest {

    private val repository = CachedPlotsRepository()

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
    )

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
}
