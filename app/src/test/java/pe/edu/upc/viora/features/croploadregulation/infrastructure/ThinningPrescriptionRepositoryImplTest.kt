package pe.edu.upc.viora.features.croploadregulation.infrastructure

import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.network.ApiCaller
import pe.edu.upc.viora.core.network.ApiErrorMapper
import pe.edu.upc.viora.features.croploadregulation.REAL_EXECUTION_JSON
import pe.edu.upc.viora.features.croploadregulation.REAL_PLAN_WITHOUT_BLOOM_JSON
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.CaliberProjectionStatus
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.LoadState
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.PrescriptionBlocker
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.PrescriptionStatus
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.YieldEstimateStatus
import pe.edu.upc.viora.features.croploadregulation.infrastructure.local.ThinningPrescriptionDao
import pe.edu.upc.viora.features.croploadregulation.infrastructure.local.ThinningPrescriptionEntity
import pe.edu.upc.viora.features.croploadregulation.infrastructure.remote.ConfirmExecutionRequestDto
import pe.edu.upc.viora.features.croploadregulation.infrastructure.remote.ExecutionConfirmationResponseDto
import pe.edu.upc.viora.features.croploadregulation.infrastructure.remote.PrescriptionResponseDto
import pe.edu.upc.viora.features.croploadregulation.infrastructure.remote.RecordFullBloomRequestDto
import pe.edu.upc.viora.features.croploadregulation.infrastructure.remote.ThinningPrescriptionService
import pe.edu.upc.viora.features.croploadregulation.infrastructure.repository.ThinningPrescriptionRepositoryImpl
import retrofit2.Response

private class FakePrescriptionDao : ThinningPrescriptionDao {
    val rows = MutableStateFlow<Map<Pair<String, Int>, ThinningPrescriptionEntity>>(emptyMap())

    override fun observe(plotId: String, campaignYear: Int): Flow<ThinningPrescriptionEntity?> =
        rows.map { it[plotId to campaignYear] }

    override fun observeCampaign(campaignYear: Int): Flow<List<ThinningPrescriptionEntity>> =
        rows.map { all -> all.values.filter { it.campaignYear == campaignYear } }

    override suspend fun find(plotId: String, campaignYear: Int): ThinningPrescriptionEntity? = rows.value[plotId to campaignYear]

    override suspend fun upsert(entity: ThinningPrescriptionEntity) {
        rows.value = rows.value + ((entity.plotId to entity.campaignYear) to entity)
    }

    override suspend fun updateExecution(plotId: String, campaignYear: Int, executionJson: String?) {
        val row = rows.value[plotId to campaignYear] ?: return
        rows.value = rows.value + ((plotId to campaignYear) to row.copy(executionJson = executionJson))
    }
}

private class FakePrescriptionService : ThinningPrescriptionService {
    var getResponse: () -> Response<PrescriptionResponseDto> = { error("not stubbed") }
    var bloomResponse: () -> Response<PrescriptionResponseDto> = { error("not stubbed") }
    var lastBloomRequest: RecordFullBloomRequestDto? = null

    override suspend fun getPrescription(plotId: String, campaignYear: Int) = getResponse()

    var listResponse: () -> Response<List<PrescriptionResponseDto>> = { error("not stubbed") }

    override suspend fun getPrescriptions(campaignYear: Int) = listResponse()

    override suspend fun recordFullBloom(plotId: String, request: RecordFullBloomRequestDto): Response<PrescriptionResponseDto> {
        lastBloomRequest = request
        return bloomResponse()
    }

    var confirmResponse: () -> Response<ExecutionConfirmationResponseDto> = { error("not stubbed") }
    var executionResponse: () -> Response<ExecutionConfirmationResponseDto> = { error("not stubbed") }
    var lastConfirmRequest: ConfirmExecutionRequestDto? = null

    override suspend fun confirmExecution(prescriptionId: String, request: ConfirmExecutionRequestDto): Response<ExecutionConfirmationResponseDto> {
        lastConfirmRequest = request
        return confirmResponse()
    }

    override suspend fun getExecution(prescriptionId: String) = executionResponse()
}

@OptIn(ExperimentalCoroutinesApi::class)
class ThinningPrescriptionRepositoryImplTest {

    private val json = Json { ignoreUnknownKeys = true }
    private val realPlan = json.decodeFromString(PrescriptionResponseDto.serializer(), REAL_PLAN_WITHOUT_BLOOM_JSON)
    private val dao = FakePrescriptionDao()
    private val service = FakePrescriptionService()
    private val clock = Clock.fixed(Instant.parse("2026-11-18T13:40:00Z"), ZoneOffset.UTC)
    private val repository = ThinningPrescriptionRepositoryImpl(
        service = service,
        dao = dao,
        apiCaller = ApiCaller(ApiErrorMapper(json), UnconfinedTestDispatcher()),
        clock = clock,
    )

    private val plotId = "2975dae0-0df6-402e-9809-1579a88bb900"
    private val realExecution = json.decodeFromString(ExecutionConfirmationResponseDto.serializer(), REAL_EXECUTION_JSON)

    private fun problem(status: Int) = Response.error<ExecutionConfirmationResponseDto>(
        status,
        "{\"status\":$status}".toResponseBody("application/json".toMediaType()),
    )

    @Test
    fun `the real production answer is cached and read back whole`() = runTest {
        service.getResponse = { Response.success(realPlan) }

        assertEquals(AppResult.Success(Unit), repository.refreshPrescription(plotId, 2026))

        val cached = repository.observePrescription(plotId, 2026).first()!!
        val plan = cached.prescription!!
        assertEquals(Instant.parse("2026-11-18T13:40:00Z"), cached.syncedAt)
        assertEquals(PrescriptionStatus.SAMPLING_IN_PROGRESS, plan.status)
        assertEquals(setOf(PrescriptionBlocker.FULL_BLOOM_MISSING), plan.blockers)
        assertEquals(5, plan.sampledTrees)
        assertEquals(LocalDate.of(2026, 10, 9), plan.lastSampledOn)
        assertEquals(14, plan.windowOpensDaysAfterBloom)
        assertNull(plan.currentLoad)
        assertEquals(YieldEstimateStatus.LOAD_UNKNOWN, plan.yieldEstimate!!.status)
        assertEquals(751.3, plan.yieldEstimate!!.sustainableKgPerHa!!, 0.0)
    }

    @Test
    fun `a plot with nothing sampled is cached as having no plan`() = runTest {
        val body = "{\"status\":404}".toResponseBody("application/json".toMediaType())
        service.getResponse = { Response.error(404, body) }

        assertEquals(AppResult.Success(Unit), repository.refreshPrescription(plotId, 2026))

        val cached = repository.observePrescription(plotId, 2026).first()
        assertNotNull(cached)
        assertNull(cached!!.prescription)
    }

    @Test
    fun `without signal the saved plan stays and the failure is reported`() = runTest {
        service.getResponse = { Response.success(realPlan) }
        repository.refreshPrescription(plotId, 2026)
        service.getResponse = { throw IOException("no route") }

        val result = repository.refreshPrescription(plotId, 2026)

        assertEquals(AppError.Offline, (result as AppResult.Failure).error)
        assertNotNull(repository.observePrescription(plotId, 2026).first()!!.prescription)
    }

    @Test
    fun `the full bloom is sent with its campaign and the new plan replaces the cache`() = runTest {
        val issued = realPlan.copy(
            status = "PRESCRIBED",
            percentageToRemove = 30.0,
            blockers = emptyList(),
            fullBloomOn = "2026-10-20",
            windowOpensOn = "2026-11-03",
            windowClosesOn = "2026-12-08",
        )
        service.bloomResponse = { Response.success(issued) }

        val result = repository.recordFullBloom(plotId, 2026, LocalDate.of(2026, 10, 20))

        assertEquals(RecordFullBloomRequestDto(campaignYear = 2026, observedOn = "2026-10-20"), service.lastBloomRequest)
        assertTrue((result as AppResult.Success).value.isIssued)
        assertEquals(LocalDate.of(2026, 11, 3), repository.observePrescription(plotId, 2026).first()!!.prescription!!.windowOpensOn)
    }

    @Test
    fun `a payload that no longer parses reads as never downloaded`() = runTest {
        dao.upsert(ThinningPrescriptionEntity(plotId, 2026, "{broken", 0L))

        assertNull(repository.observePrescription(plotId, 2026).first())
    }

    @Test
    fun `a recorded thinning is sent without kilos, cached and the plan downloaded again`() = runTest {
        service.confirmResponse = { Response.success(realExecution) }
        service.getResponse = { Response.success(realPlan.copy(status = "EXECUTED")) }

        val result = repository.confirmExecution(plotId, 2026, "p-1", LocalDate.of(2026, 11, 18), 30.0, "  ")

        assertEquals(ConfirmExecutionRequestDto(executedDate = "2026-11-18", actualRemovalPercentage = 30.0, notes = null), service.lastConfirmRequest)
        val execution = (result as AppResult.Success).value
        assertEquals("101/110", execution.caliberProjection!!.mostLikelySizeGrade)
        assertEquals("121/140", execution.caliberProjection!!.smallFruitSizeGrade)
        assertEquals("91/100", execution.caliberProjection!!.bigFruitSizeGrade)
        assertEquals(LoadState.BALANCED, execution.loadBalance!!.loadState)
        assertNull(execution.notes)
        assertEquals(execution, repository.observeExecution(plotId, 2026).first())
        assertEquals(PrescriptionStatus.EXECUTED, repository.observePrescription(plotId, 2026).first()!!.prescription!!.status)
    }

    @Test
    fun `downloading the plan again keeps the recorded thinning`() = runTest {
        service.getResponse = { Response.success(realPlan) }
        repository.refreshPrescription(plotId, 2026)
        service.executionResponse = { Response.success(realExecution) }
        repository.refreshExecution(plotId, 2026, "p-1")

        repository.refreshPrescription(plotId, 2026)

        assertNotNull(repository.observeExecution(plotId, 2026).first())
    }

    @Test
    fun `an already recorded thinning is read back instead of failing`() = runTest {
        service.confirmResponse = { problem(409) }
        service.executionResponse = { Response.success(realExecution.copy(isOpportune = false)) }
        service.getResponse = { throw IOException("no route") }

        val result = repository.confirmExecution(plotId, 2026, "p-1", LocalDate.of(2026, 11, 18), 30.0, null)

        assertEquals(false, (result as AppResult.Success).value.isOpportune)
    }

    @Test
    fun `a thinning not recorded yet is not an error, and an unknown status shows no caliber`() = runTest {
        service.executionResponse = { problem(404) }
        assertEquals(AppResult.Success(Unit), repository.refreshExecution(plotId, 2026, "p-1"))
        assertNull(repository.observeExecution(plotId, 2026).first())

        val future = realExecution.copy(caliberProjection = realExecution.caliberProjection!!.copy(status = "SOMETHING_NEW"))
        service.executionResponse = { Response.success(future) }
        repository.refreshExecution(plotId, 2026, "p-1")

        val projection = repository.observeExecution(plotId, 2026).first()!!.caliberProjection!!
        assertEquals(CaliberProjectionStatus.NOT_APPLICABLE, projection.status)
    }

    @Test
    fun `a plan recorded from another phone brings its thinning along`() = runTest {
        service.getResponse = { Response.success(realPlan.copy(status = "EXECUTED")) }
        service.executionResponse = { Response.success(realExecution) }

        repository.refreshPrescription(plotId, 2026)

        assertEquals(30.0, repository.observeExecution(plotId, 2026).first()!!.removalPercentage, 0.0)
    }

    @Test
    fun `the campaign list caches each plan and keeps their recorded thinnings`() = runTest {
        service.executionResponse = { Response.success(realExecution) }
        repository.refreshExecution(plotId, 2026, "p-1")
        val other = realPlan.copy(id = "p-2", plotId = "plot-2")
        service.listResponse = { Response.success(listOf(realPlan, other)) }

        assertEquals(AppResult.Success(Unit), repository.refreshCampaign(2026))

        val plans = repository.observeCampaign(2026).first().associateBy { it.plotId }
        assertEquals(setOf(plotId, "plot-2"), plans.keys)
        assertNotNull(plans.getValue(plotId).execution)
        assertNull(plans.getValue("plot-2").execution)
    }
}
