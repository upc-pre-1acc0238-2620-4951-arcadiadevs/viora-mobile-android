package pe.edu.upc.viora.features.plotmanagement.presentation

import androidx.lifecycle.SavedStateHandle
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.UpdatePlotUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.entity.NewPlot
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.entity.PlotChanges
import pe.edu.upc.viora.features.plotmanagement.domain.repository.PlotRepository
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotOutline
import pe.edu.upc.viora.features.plotmanagement.presentation.state.AdjustOutlineUiState
import pe.edu.upc.viora.features.plotmanagement.presentation.state.EditFailure
import pe.edu.upc.viora.features.plotmanagement.presentation.viewmodel.AdjustOutlineViewModel

private class OutlinePlotsRepository : PlotRepository {
    val plots = MutableStateFlow<List<Plot>>(emptyList())
    var updateResult: (PlotChanges) -> AppResult<Plot> = { AppResult.Failure(AppError.Offline) }
    val updates = mutableListOf<PlotChanges>()

    override fun observePlots(): Flow<List<Plot>> = plots
    override fun observePlot(id: PlotId): Flow<Plot?> = plots.map { all -> all.firstOrNull { it.id == id } }
    override fun observeArchivedPlots(): Flow<List<Plot>> = MutableStateFlow(emptyList())
    override fun observeLastRefresh(): Flow<Instant?> = MutableStateFlow(null)
    override suspend fun refresh(): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun refreshArchived(): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun register(newPlot: NewPlot): AppResult<Plot> = AppResult.Failure(AppError.Offline)
    override suspend fun archive(id: PlotId): AppResult<Unit> = AppResult.Failure(AppError.Offline)
    override suspend fun restore(id: PlotId): AppResult<Plot> = AppResult.Failure(AppError.Offline)

    override suspend fun update(id: PlotId, changes: PlotChanges): AppResult<Plot> {
        updates += changes
        return updateResult(changes)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AdjustOutlineViewModelTest {

    private val repository = OutlinePlotsRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // A square of about 100 m, corners in order.
    private val south = -18.0500
    private val north = -18.0491
    private val west = -70.2500
    private val east = -70.2491

    private val square = listOf(
        GeoPoint(south, west),
        GeoPoint(south, east),
        GeoPoint(north, east),
        GeoPoint(north, west),
    )

    private val original = Plot(
        id = PlotId("p1"),
        name = "La Yarada 03",
        variety = OliveVariety.SEVILLANA,
        areaHectares = PlotOutline.areaHectares(square),
        treesPerHectare = 204,
        rowSpacingMeters = 7.0,
        treeSpacingMeters = 5.0,
        outline = square,
        lastPruningDate = null,
        isActive = true,
        revision = 2,
    )

    private fun viewModel(plotId: String = "p1") = AdjustOutlineViewModel(
        savedStateHandle = SavedStateHandle(mapOf("plotId" to plotId)),
        observePlot = ObservePlotUseCase(repository),
        updatePlot = UpdatePlotUseCase(repository),
    )

    private fun AdjustOutlineViewModel.adjusting() = uiState.value as AdjustOutlineUiState.Adjusting

    private fun openedViewModel(): AdjustOutlineViewModel {
        repository.plots.value = listOf(original)
        return viewModel()
    }

    @Test
    fun `starts from the outline the plot already has`() = runTest {
        val state = openedViewModel().adjusting()

        assertEquals(square, state.points)
        assertEquals(0, state.movedCount)
        assertFalse(state.hasChanges)
        assertFalse(state.canSave)
        assertFalse(state.canUndo)
    }

    @Test
    fun `a plot that is not cached is not found`() = runTest {
        assertEquals(AdjustOutlineUiState.NotFound, viewModel("missing").uiState.value)
    }

    @Test
    fun `moving a corner recalculates the area and counts it as moved`() = runTest {
        val vm = openedViewModel()

        vm.moveCorner(2, GeoPoint(north + 0.0003, east + 0.0003))

        val state = vm.adjusting()
        assertEquals(1, state.movedCount)
        assertTrue(state.areaHectares > original.areaHectares)
        assertTrue(state.canSave)
    }

    @Test
    fun `dragging one corner is a single undo step`() = runTest {
        val vm = openedViewModel()

        vm.moveCorner(2, GeoPoint(north + 0.0001, east + 0.0001))
        vm.moveCorner(2, GeoPoint(north + 0.0002, east + 0.0002))
        vm.moveCorner(2, GeoPoint(north + 0.0003, east + 0.0003))
        assertEquals(1, vm.adjusting().history.size)

        vm.undo()

        assertEquals(square, vm.adjusting().points)
        assertFalse(vm.adjusting().canUndo)
    }

    @Test
    fun `moving another corner starts a new undo step`() = runTest {
        val vm = openedViewModel()

        vm.moveCorner(2, GeoPoint(north + 0.0001, east + 0.0001))
        vm.moveCorner(3, GeoPoint(north + 0.0001, west - 0.0001))

        assertEquals(2, vm.adjusting().history.size)
        assertEquals(2, vm.adjusting().movedCount)
    }

    @Test
    fun `a move that would cross the edges is refused and the outline stays`() = runTest {
        val vm = openedViewModel()

        // Corner 0 dragged above the top edge makes its edge cross that top edge.
        vm.moveCorner(0, GeoPoint(north + 0.0005, west + 0.0004))

        val state = vm.adjusting()
        assertTrue(state.refusedMove)
        assertEquals(square, state.points)
    }

    @Test
    fun `adding a corner puts it in the outline without crossing edges and can be undone`() = runTest {
        val vm = openedViewModel()

        vm.addCorner(GeoPoint(south - 0.0003, (west + east) / 2))

        assertEquals(5, vm.adjusting().corners.size)
        assertEquals(0, vm.adjusting().movedCount)
        assertTrue(vm.adjusting().canSave)

        vm.undo()

        assertEquals(square, vm.adjusting().points)
    }

    @Test
    fun `saving sends the plot with the same name and frame and the new outline`() = runTest {
        val vm = openedViewModel()
        repository.updateResult = { AppResult.Success(original) }
        val newCorner = GeoPoint(north + 0.0002, east + 0.0002)
        vm.moveCorner(2, newCorner)

        vm.save()

        val changes = repository.updates.single()
        assertEquals("La Yarada 03", changes.name.value)
        assertEquals(OliveVariety.SEVILLANA, changes.variety)
        assertEquals(7.0, changes.frame.rowSpacingMeters, 0.0)
        assertEquals(5.0, changes.frame.treeSpacingMeters, 0.0)
        assertEquals(newCorner, changes.outline!!.corners[2])
        assertTrue(vm.adjusting().isSaved)
    }

    @Test
    fun `nothing is sent when nothing changed`() = runTest {
        val vm = openedViewModel()

        vm.save()

        assertTrue(repository.updates.isEmpty())
    }

    @Test
    fun `a plot changed on another device is reported as outdated`() = runTest {
        val vm = openedViewModel()
        repository.updateResult = { AppResult.Failure(AppError.PreconditionFailed()) }
        vm.moveCorner(2, GeoPoint(north + 0.0002, east + 0.0002))

        vm.save()

        assertEquals(EditFailure.Outdated, vm.adjusting().failure)
        assertFalse(vm.adjusting().isSaved)
        assertNotEquals(square, vm.adjusting().points)
    }
}
