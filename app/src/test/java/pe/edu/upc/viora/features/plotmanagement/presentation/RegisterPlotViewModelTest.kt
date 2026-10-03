package pe.edu.upc.viora.features.plotmanagement.presentation

import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
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
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.RegisterPlotUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.entity.NewPlot
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.repository.PlotRepository
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlantationFrame
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotOutline
import pe.edu.upc.viora.features.plotmanagement.presentation.state.RegisterPlotStep
import pe.edu.upc.viora.features.plotmanagement.presentation.state.SaveFailure
import pe.edu.upc.viora.features.plotmanagement.presentation.viewmodel.RegisterPlotViewModel

private class RegistrationRepository : PlotRepository {
    val plots = MutableStateFlow<List<Plot>>(emptyList())
    val registered = mutableListOf<NewPlot>()
    var registerResult: AppResult<Plot> = AppResult.Failure(AppError.Offline)

    override fun observePlots(): Flow<List<Plot>> = plots
    override fun observePlot(id: PlotId): Flow<Plot?> = MutableStateFlow(null)
    override fun observeLastRefresh(): Flow<Instant?> = MutableStateFlow(null)
    override suspend fun refresh(): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun register(newPlot: NewPlot): AppResult<Plot> {
        registered += newPlot
        return registerResult
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterPlotViewModelTest {

    private val repository = RegistrationRepository()

    private val a = GeoPoint(latitude = -18.050, longitude = -70.250)
    private val b = GeoPoint(latitude = -18.050, longitude = -70.249)
    private val c = GeoPoint(latitude = -18.051, longitude = -70.249)
    private val d = GeoPoint(latitude = -18.051, longitude = -70.250)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = RegisterPlotViewModel(
        observePlots = ObservePlotsUseCase(repository),
        registerPlot = RegisterPlotUseCase(repository),
    )

    private fun existingPlot() = Plot(
        id = PlotId("p"), name = "Existing", variety = OliveVariety.CRIOLLA, areaHectares = 1.0, treesPerHectare = 100,
        rowSpacingMeters = 10.0, treeSpacingMeters = 10.0, outline = listOf(a, b, c, d),
        lastPruningDate = null, isActive = true, revision = 0,
    )

    /** A view model that already traced a square and filled valid details, ready on REVIEW. */
    private fun readyToSave(): RegisterPlotViewModel {
        val vm = viewModel()
        listOf(a, b, c, d).forEach(vm::addCorner)
        vm.closeOutline()
        vm.setName("  La Yarada 03 ")
        vm.setVariety(OliveVariety.SEVILLANA)
        vm.continueToReview()
        return vm
    }

    // ---- start

    @Test
    fun `starts on the trace step with nothing drawn`() {
        val state = viewModel().uiState.value

        assertEquals(RegisterPlotStep.TRACE, state.step)
        assertTrue(state.corners.isEmpty())
        assertFalse(state.hasWorkInProgress)
    }

    @Test
    fun `the map opens over the existing plots, or nowhere in particular when there are none`() {
        assertNull(viewModel().uiState.value.mapCenter)

        repository.plots.value = listOf(existingPlot())
        val center = viewModel().uiState.value.mapCenter!!
        assertEquals(-18.0505, center.latitude, 1e-9)
        assertEquals(-70.2495, center.longitude, 1e-9)
    }

    // ---- trace

    @Test
    fun `corners can be added and undone`() {
        val vm = viewModel()
        vm.addCorner(a)
        vm.addCorner(b)
        assertEquals(listOf(a, b), vm.uiState.value.corners)
        assertTrue(vm.uiState.value.hasWorkInProgress)

        vm.undoCorner()
        assertEquals(listOf(a), vm.uiState.value.corners)
    }

    @Test
    fun `closing with fewer than three corners explains how many are missing`() {
        val vm = viewModel()
        vm.addCorner(a)
        vm.addCorner(b)

        vm.closeOutline()

        assertEquals(PlotOutline.Error.NotEnoughCorners(2), vm.uiState.value.outlineError)
        assertEquals(RegisterPlotStep.TRACE, vm.uiState.value.step)
    }

    @Test
    fun `corners tapped out of order are arranged so the outline does not cross`() {
        val vm = viewModel()
        // a -> c -> b -> d tapped in this order would be a bow-tie.
        listOf(a, c, b, d).forEach(vm::addCorner)

        val state = vm.uiState.value
        assertEquals(4, state.corners.size)
        assertEquals(null, PlotOutline.check(state.corners))
        assertEquals(null, state.outlineError)
        // Same square as tracing it in order.
        assertEquals(PlotOutline.areaHectares(listOf(a, b, c, d)), state.areaHectares, 1e-9)
    }

    @Test
    fun `undo removes the corner added last even if it was slipped between others`() {
        val vm = viewModel()
        listOf(a, c, b, d).forEach(vm::addCorner)

        vm.undoCorner()

        assertEquals(setOf(a, c, b), vm.uiState.value.corners.toSet())
    }

    @Test
    fun `a corner can be dragged to a new place`() {
        val vm = viewModel()
        listOf(a, b, c, d).forEach(vm::addCorner)
        val id = vm.uiState.value.outline.first { it.point == c }.id
        val moved = GeoPoint(latitude = -18.0515, longitude = -70.2485)

        vm.moveCorner(id, moved)

        assertTrue(moved in vm.uiState.value.corners)
        assertEquals(false, vm.uiState.value.refusedMove)
    }

    @Test
    fun `dragging a corner across the opposite edge is refused and the corner stays`() {
        val vm = viewModel()
        listOf(a, b, c, d).forEach(vm::addCorner)
        val id = vm.uiState.value.outline.first { it.point == c }.id
        // Past the d-a edge: the b-c edge would cut across it.
        vm.moveCorner(id, GeoPoint(latitude = -18.0505, longitude = -70.2510))

        assertTrue(c in vm.uiState.value.corners)
        assertEquals(true, vm.uiState.value.refusedMove)
    }

    @Test
    fun `a valid outline moves to the details and the error clears when editing`() {
        val vm = viewModel()
        vm.addCorner(a)
        vm.closeOutline()
        assertTrue(vm.uiState.value.outlineError != null)
        vm.addCorner(b)
        assertNull(vm.uiState.value.outlineError)
        vm.addCorner(c)

        vm.closeOutline()

        assertEquals(RegisterPlotStep.DETAILS, vm.uiState.value.step)
    }

    // ---- details

    @Test
    fun `details need a valid name, a variety and a sensible frame before the review`() {
        val vm = viewModel()
        listOf(a, b, c).forEach(vm::addCorner)
        vm.closeOutline()

        vm.continueToReview()
        assertEquals(RegisterPlotStep.DETAILS, vm.uiState.value.step)
        assertTrue(vm.uiState.value.showDetailErrors)

        vm.setName("Lote Norte")
        vm.setVariety(OliveVariety.ARBEQUINA)
        vm.continueToReview()
        assertEquals(RegisterPlotStep.REVIEW, vm.uiState.value.step)
    }

    @Test
    fun `spacing moves in half metre steps within limits and drives the density`() {
        val vm = viewModel()
        assertEquals(204, vm.uiState.value.treesPerHectare) // 7 x 7 m, as in the design

        vm.changeRowSpacing(-1)
        assertEquals(6.5, vm.uiState.value.rowSpacingMeters, 0.0)

        vm.changeRowSpacing(-100)
        assertEquals(1.0, vm.uiState.value.rowSpacingMeters, 0.0)
        assertEquals(PlantationFrame.Error.TOO_DENSE, vm.uiState.value.frameError) // 1 x 7 m = 1 429 trees/ha

        vm.changeTreeSpacing(100)
        assertEquals(20.0, vm.uiState.value.treeSpacingMeters, 0.0)
    }

    @Test
    fun `a frame that is too dense blocks the review`() {
        val vm = viewModel()
        listOf(a, b, c).forEach(vm::addCorner)
        vm.closeOutline()
        vm.setName("Lote Norte")
        vm.setVariety(OliveVariety.CRIOLLA)
        vm.changeRowSpacing(-100)

        vm.continueToReview()

        assertEquals(RegisterPlotStep.DETAILS, vm.uiState.value.step)
    }

    // ---- save

    @Test
    fun `saving sends a validated plot with the trimmed name and marks the wizard as saved`() {
        repository.registerResult = AppResult.Success(existingPlot())
        val vm = readyToSave()

        vm.save()

        val sent = repository.registered.single()
        assertEquals("La Yarada 03", sent.name.value)
        assertEquals(OliveVariety.SEVILLANA, sent.variety)
        assertEquals(204, sent.frame.treesPerHectare)
        assertEquals(4, sent.outline.corners.size)
        assertTrue(vm.uiState.value.isSaved)
        assertEquals(PlotId("p"), vm.uiState.value.savedPlotId)
        assertFalse(vm.uiState.value.isSaving)
    }

    @Test
    fun `a taken name sends the producer back to the details to change it`() {
        repository.registerResult = AppResult.Failure(AppError.Conflict("Name already used", "PLOT_CONFLICT"))
        val vm = readyToSave()

        vm.save()

        assertEquals(SaveFailure.NameTaken, vm.uiState.value.saveFailure)
        assertEquals(RegisterPlotStep.DETAILS, vm.uiState.value.step)
        assertFalse(vm.uiState.value.isSaved)

        vm.setName("Another name")
        assertNull(vm.uiState.value.saveFailure)
    }

    @Test
    fun `a rejected plot shows the server explanation and stays on the review`() {
        repository.registerResult = AppResult.Failure(AppError.Validation("Polygon not valid", "VALIDATION_ERROR"))
        val vm = readyToSave()

        vm.save()

        assertEquals(SaveFailure.Rejected("Polygon not valid"), vm.uiState.value.saveFailure)
        assertEquals(RegisterPlotStep.REVIEW, vm.uiState.value.step)
    }

    @Test
    fun `no connection keeps everything so the producer can retry`() {
        val vm = readyToSave()

        vm.save()

        assertEquals(SaveFailure.Other(AppError.Offline), vm.uiState.value.saveFailure)
        assertEquals(4, vm.uiState.value.corners.size)
        assertEquals("  La Yarada 03 ", vm.uiState.value.name)

        vm.dismissSaveFailure()
        assertNull(vm.uiState.value.saveFailure)
    }

    @Test
    fun `saving without a variety sends nothing`() {
        val vm = viewModel()
        listOf(a, b, c).forEach(vm::addCorner)
        vm.closeOutline()
        vm.setName("Lote Norte")

        vm.save()

        assertTrue(repository.registered.isEmpty())
    }

    // ---- back

    @Test
    fun `back walks the steps in reverse and then asks to leave`() {
        val vm = readyToSave()
        assertEquals(RegisterPlotStep.REVIEW, vm.uiState.value.step)

        assertTrue(vm.goBack())
        assertEquals(RegisterPlotStep.DETAILS, vm.uiState.value.step)
        assertTrue(vm.goBack())
        assertEquals(RegisterPlotStep.TRACE, vm.uiState.value.step)
        assertFalse(vm.goBack())
    }
}
