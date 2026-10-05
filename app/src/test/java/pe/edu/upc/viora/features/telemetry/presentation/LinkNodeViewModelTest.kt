package pe.edu.upc.viora.features.telemetry.presentation

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.telemetry.application.usecase.LinkSensorNodeUseCase
import pe.edu.upc.viora.features.telemetry.domain.entity.NewSensorNode
import pe.edu.upc.viora.features.telemetry.domain.entity.SensorNode
import pe.edu.upc.viora.features.telemetry.domain.entity.SensorStatus
import pe.edu.upc.viora.features.telemetry.domain.entity.SensorType
import pe.edu.upc.viora.features.telemetry.domain.repository.SensorRepository
import pe.edu.upc.viora.features.telemetry.presentation.viewmodel.LinkNodeViewModel

private class LinkFakeSensorRepo : SensorRepository {
    var linkedNode: NewSensorNode? = null
    var shouldSucceed = true

    override fun observeNodes(plotId: String): Flow<List<SensorNode>> = MutableStateFlow(emptyList())
    override suspend fun refresh(plotId: String): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun updateNode(node: SensorNode): AppResult<SensorNode> = AppResult.Success(node)
    override suspend fun unlinkNode(plotId: String, nodeId: String): AppResult<Unit> = AppResult.Success(Unit)

    override suspend fun linkNode(newNode: NewSensorNode): AppResult<SensorNode> {
        linkedNode = newNode
        return if (shouldSucceed) {
            AppResult.Success(
                SensorNode(
                    id = "created-id",
                    plotId = newNode.plotId,
                    name = newNode.name,
                    type = newNode.type,
                    depthCm = newNode.depthCm,
                    status = SensorStatus.ACTIVE,
                    lastReadingAt = null,
                    lastTemperatureCelsius = null,
                    lastHumidityPercent = null,
                )
            )
        } else {
            AppResult.Failure(pe.edu.upc.viora.core.domain.AppError.Offline)
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class LinkNodeViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val fakeRepo = LinkFakeSensorRepo()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `cannot submit when name is blank`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("plotId" to "plot-1"))
        val viewModel = LinkNodeViewModel(savedStateHandle, LinkSensorNodeUseCase(fakeRepo))

        assertFalse(viewModel.uiState.value.canSubmit)
        var successCalled = false
        viewModel.submit { successCalled = true }

        assertFalse(successCalled)
        assertTrue(viewModel.uiState.value.nameError)
    }

    @Test
    fun `submits new soil probe node with depth`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("plotId" to "plot-1"))
        val viewModel = LinkNodeViewModel(savedStateHandle, LinkSensorNodeUseCase(fakeRepo))

        viewModel.onNameChange("Sonda Sector Oeste")
        viewModel.onTypeChange(SensorType.SONDA_SUELO)
        viewModel.onDepthChange(60)

        assertTrue(viewModel.uiState.value.canSubmit)

        var successCalled = false
        viewModel.submit { successCalled = true }

        assertTrue(successCalled)
        assertEquals("plot-1", fakeRepo.linkedNode?.plotId)
        assertEquals("Sonda Sector Oeste", fakeRepo.linkedNode?.name)
        assertEquals(SensorType.SONDA_SUELO, fakeRepo.linkedNode?.type)
        assertEquals(60, fakeRepo.linkedNode?.depthCm)
    }
}
