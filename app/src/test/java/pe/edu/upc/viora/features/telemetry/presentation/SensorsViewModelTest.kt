package pe.edu.upc.viora.features.telemetry.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
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
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.repository.PlotRepository
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId
import pe.edu.upc.viora.features.telemetry.application.usecase.ObserveSensorNodesUseCase
import pe.edu.upc.viora.features.telemetry.application.usecase.RefreshSensorNodesUseCase
import pe.edu.upc.viora.features.telemetry.domain.entity.NewSensorNode
import pe.edu.upc.viora.features.telemetry.domain.entity.SensorNode
import pe.edu.upc.viora.features.telemetry.domain.entity.SensorStatus
import pe.edu.upc.viora.features.telemetry.domain.entity.SensorType
import pe.edu.upc.viora.features.telemetry.domain.repository.SensorRepository
import pe.edu.upc.viora.features.telemetry.presentation.navigation.SensorsRoute
import pe.edu.upc.viora.features.telemetry.presentation.state.SensorsUiState
import pe.edu.upc.viora.features.telemetry.presentation.viewmodel.SensorsViewModel

private class FakeSensorRepository : SensorRepository {
    val nodesFlow = MutableStateFlow<List<SensorNode>>(emptyList())
    var refreshCalls = 0
    var refreshResult: AppResult<Unit> = AppResult.Success(Unit)

    override fun observeNodes(plotId: String): Flow<List<SensorNode>> = nodesFlow
    override suspend fun refresh(plotId: String): AppResult<Unit> {
        refreshCalls++
        return refreshResult
    }
    override suspend fun linkNode(newNode: NewSensorNode): AppResult<SensorNode> =
        AppResult.Failure(AppError.Offline)
}

private class FakePlotRepo : PlotRepository {
    override fun observePlot(id: PlotId): Flow<Plot?> = MutableStateFlow(
        Plot(
            id = id,
            name = "La Yarada 02",
            variety = OliveVariety.SEVILLANA,
            areaHectares = 2.5,
            treesPerHectare = 72,
            rowSpacingMeters = 7.0,
            treeSpacingMeters = 5.0,
            outline = emptyList(),
            lastPruningDate = null,
            isActive = true,
            revision = 1,
        )
    )
    override fun observePlots(): Flow<List<Plot>> = MutableStateFlow(emptyList())
    override fun observeArchivedPlots(): Flow<List<Plot>> = MutableStateFlow(emptyList())
    override fun observeLastRefresh(): Flow<java.time.Instant?> = MutableStateFlow(null)
    override suspend fun refresh(): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun refreshArchived(): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun register(newPlot: pe.edu.upc.viora.features.plotmanagement.domain.entity.NewPlot) = AppResult.Failure(AppError.Offline)
    override suspend fun update(id: PlotId, changes: pe.edu.upc.viora.features.plotmanagement.domain.entity.PlotChanges) = AppResult.Failure(AppError.Offline)
    override suspend fun archive(id: PlotId) = AppResult.Failure(AppError.Offline)
    override suspend fun restore(id: PlotId) = AppResult.Failure(AppError.Offline)
}

@OptIn(ExperimentalCoroutinesApi::class)
class SensorsViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val fakeRepo = FakeSensorRepository()
    private val fakePlotRepo = FakePlotRepo()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `shows empty state when plot has no sensor nodes`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("plotId" to "plot-1", "plotName" to "La Yarada 02"))
        val viewModel = SensorsViewModel(
            savedStateHandle = savedStateHandle,
            observeNodes = ObserveSensorNodesUseCase(fakeRepo),
            observePlot = ObservePlotUseCase(fakePlotRepo),
            refreshNodes = RefreshSensorNodesUseCase(fakeRepo),
        )

        val states = mutableListOf<SensorsUiState>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { states += it }
        }

        assertTrue(states.any { it is SensorsUiState.Empty })
        job.cancel()
    }

    @Test
    fun `shows content when nodes exist`() = runTest {
        fakeRepo.nodesFlow.value = listOf(
            SensorNode(
                id = "node-1",
                plotId = "plot-1",
                name = "Estación La Yarada",
                type = SensorType.MICROCLIMA,
                depthCm = null,
                status = SensorStatus.ACTIVE,
                lastReadingAt = "2026-10-04T05:00:00Z",
                lastTemperatureCelsius = 27.4,
                lastHumidityPercent = 38.0,
            )
        )

        val savedStateHandle = SavedStateHandle(mapOf("plotId" to "plot-1", "plotName" to "La Yarada 02"))
        val viewModel = SensorsViewModel(
            savedStateHandle = savedStateHandle,
            observeNodes = ObserveSensorNodesUseCase(fakeRepo),
            observePlot = ObservePlotUseCase(fakePlotRepo),
            refreshNodes = RefreshSensorNodesUseCase(fakeRepo),
        )

        val states = mutableListOf<SensorsUiState>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { states += it }
        }

        val content = states.filterIsInstance<SensorsUiState.Content>().last()
        assertEquals("La Yarada 02", content.plotName)
        assertEquals(1, content.nodes.size)
        assertEquals("Estación La Yarada", content.nodes[0].name)
        job.cancel()
    }
}
