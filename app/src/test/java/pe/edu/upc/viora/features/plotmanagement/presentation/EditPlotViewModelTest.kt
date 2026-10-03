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
import org.junit.Assert.assertNull
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
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlantationFrame
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotName
import pe.edu.upc.viora.features.plotmanagement.presentation.state.EditFailure
import pe.edu.upc.viora.features.plotmanagement.presentation.state.EditPlotUiState
import pe.edu.upc.viora.features.plotmanagement.presentation.viewmodel.EditPlotViewModel

private class EditablePlotsRepository : PlotRepository {
    val plots = MutableStateFlow<List<Plot>>(emptyList())
    var updateResult: (PlotChanges) -> AppResult<Plot> = { AppResult.Failure(AppError.Offline) }
    val updates = mutableListOf<Pair<PlotId, PlotChanges>>()

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
        updates += id to changes
        return updateResult(changes)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class EditPlotViewModelTest {

    private val repository = EditablePlotsRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val original = Plot(
        id = PlotId("p1"),
        name = "La Yarada 03",
        variety = OliveVariety.SEVILLANA,
        areaHectares = 0.92,
        treesPerHectare = 286,
        rowSpacingMeters = 7.0,
        treeSpacingMeters = 5.0,
        outline = emptyList(),
        lastPruningDate = null,
        isActive = true,
        revision = 2,
    )

    private fun viewModel(plotId: String = "p1") = EditPlotViewModel(
        savedStateHandle = SavedStateHandle(mapOf("plotId" to plotId)),
        observePlot = ObservePlotUseCase(repository),
        updatePlot = UpdatePlotUseCase(repository),
    )

    private fun EditPlotViewModel.editing() = uiState.value as EditPlotUiState.Editing

    private fun openedViewModel(): EditPlotViewModel {
        repository.plots.value = listOf(original)
        return viewModel()
    }

    @Test
    fun `starts from the data the plot already has`() = runTest {
        val vm = openedViewModel()

        val state = vm.editing()

        assertEquals("La Yarada 03", state.name)
        assertEquals(OliveVariety.SEVILLANA, state.variety)
        assertEquals(7.0, state.rowSpacingMeters!!, 0.0)
        assertEquals(5.0, state.treeSpacingMeters!!, 0.0)
        assertFalse(state.hasChanges)
    }

    @Test
    fun `a plot that is not cached is not found`() = runTest {
        assertEquals(EditPlotUiState.NotFound, viewModel("missing").uiState.value)
    }

    @Test
    fun `any edit marks the form as changed and going back to the original clears it`() = runTest {
        val vm = openedViewModel()

        vm.setVariety(OliveVariety.ARBEQUINA)
        assertTrue(vm.editing().hasChanges)

        vm.setVariety(OliveVariety.SEVILLANA)
        assertFalse(vm.editing().hasChanges)
    }

    @Test
    fun `distances are typed with a dot or a comma and anything else is ignored`() = runTest {
        val vm = openedViewModel()

        vm.setRowSpacing("7,5")
        vm.setTreeSpacing("a4.5 m")

        assertEquals(7.5, vm.editing().rowSpacingMeters!!, 0.0)
        assertEquals("4.5", vm.editing().treeSpacingText)
        assertEquals(4.5, vm.editing().treeSpacingMeters!!, 0.0)
    }

    @Test
    fun `the form starts with the distances written the way a producer would`() = runTest {
        repository.plots.value = listOf(original.copy(rowSpacingMeters = 7.0, treeSpacingMeters = 4.5))
        val vm = viewModel()

        assertEquals("7", vm.editing().rowSpacingText)
        assertEquals("4.5", vm.editing().treeSpacingText)
    }

    @Test
    fun `a too dense frame is not viable and blocks saving`() = runTest {
        val vm = openedViewModel()
        vm.setRowSpacing("4")
        vm.setTreeSpacing("4")

        vm.save()

        assertEquals(PlantationFrame.Error.TOO_DENSE, vm.editing().frameError)
        assertEquals(625, vm.editing().rawDensity)
        assertNull(vm.editing().estimatedTrees)
        assertFalse(vm.editing().isValid)
        assertTrue(repository.updates.isEmpty())
    }

    @Test
    fun `an empty distance is not a frame yet`() = runTest {
        val vm = openedViewModel()
        vm.setRowSpacing("")

        assertEquals(PlantationFrame.Error.NOT_POSITIVE, vm.editing().frameError)
        assertFalse(vm.editing().isValid)
    }

    @Test
    fun `a viable frame shows the new density and the trees of the whole plot`() = runTest {
        val vm = openedViewModel()
        vm.setRowSpacing("10")
        vm.setTreeSpacing("10")

        assertEquals(100, vm.editing().treesPerHectare)
        assertEquals(92, vm.editing().estimatedTrees)
    }

    @Test
    fun `a short name blocks saving`() = runTest {
        val vm = openedViewModel()
        vm.setName("ab")

        vm.save()

        assertTrue(repository.updates.isEmpty())
        assertEquals(PlotName.Error.TOO_SHORT, vm.editing().nameError)
    }

    @Test
    fun `saving sends the trimmed name, variety and frame and finishes saved`() = runTest {
        val vm = openedViewModel()
        repository.updateResult = { AppResult.Success(original.copy(name = "Norte")) }
        vm.setName("  Norte  ")
        vm.setVariety(OliveVariety.ARBEQUINA)
        vm.setRowSpacing("8")

        vm.save()

        val (id, changes) = repository.updates.single()
        assertEquals(PlotId("p1"), id)
        assertEquals("Norte", changes.name.value)
        assertEquals(OliveVariety.ARBEQUINA, changes.variety)
        assertEquals(8.0, changes.frame.rowSpacingMeters, 0.0)
        assertEquals(5.0, changes.frame.treeSpacingMeters, 0.0)
        assertTrue(vm.editing().isSaved)
        assertFalse(vm.editing().isSaving)
    }

    @Test
    fun `a taken name keeps the form open with the name error`() = runTest {
        val vm = openedViewModel()
        repository.updateResult = { AppResult.Failure(AppError.Conflict()) }
        vm.setName("Norte")

        vm.save()

        assertEquals(EditFailure.NameTaken, vm.editing().failure)
        assertFalse(vm.editing().isSaved)
    }

    @Test
    fun `typing a new name clears the previous failure`() = runTest {
        val vm = openedViewModel()
        repository.updateResult = { AppResult.Failure(AppError.Conflict()) }
        vm.setName("Norte")
        vm.save()

        vm.setName("Norte 2")

        assertNull(vm.editing().failure)
    }

    @Test
    fun `a plot changed on another device is reported as outdated`() = runTest {
        val vm = openedViewModel()
        repository.updateResult = { AppResult.Failure(AppError.PreconditionFailed()) }
        vm.setName("Norte")

        vm.save()

        assertEquals(EditFailure.Outdated, vm.editing().failure)
    }

    @Test
    fun `a rejected edit and a network failure map to their own failures`() = runTest {
        val vm = openedViewModel()
        vm.setName("Norte")

        repository.updateResult = { AppResult.Failure(AppError.Validation("bad frame")) }
        vm.save()
        assertEquals(EditFailure.Rejected("bad frame"), vm.editing().failure)

        repository.updateResult = { AppResult.Failure(AppError.Offline) }
        vm.save()
        assertEquals(EditFailure.Other(AppError.Offline), vm.editing().failure)
    }
}
