package pe.edu.upc.viora.features.croploadregulation.infrastructure.repository

import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.network.ApiCaller
import pe.edu.upc.viora.features.croploadregulation.domain.entity.CachedPrescription
import pe.edu.upc.viora.features.croploadregulation.domain.entity.CampaignPlan
import pe.edu.upc.viora.features.croploadregulation.domain.entity.ThinningExecution
import pe.edu.upc.viora.features.croploadregulation.domain.entity.ThinningPrescription
import pe.edu.upc.viora.features.croploadregulation.domain.repository.ThinningPrescriptionRepository
import pe.edu.upc.viora.features.croploadregulation.infrastructure.local.ThinningPrescriptionDao
import pe.edu.upc.viora.features.croploadregulation.infrastructure.mapper.toCached
import pe.edu.upc.viora.features.croploadregulation.infrastructure.mapper.toCampaignPlan
import pe.edu.upc.viora.features.croploadregulation.infrastructure.mapper.toDomain
import pe.edu.upc.viora.features.croploadregulation.infrastructure.mapper.toEntity
import pe.edu.upc.viora.features.croploadregulation.infrastructure.mapper.toExecution
import pe.edu.upc.viora.features.croploadregulation.infrastructure.mapper.toJson
import pe.edu.upc.viora.features.croploadregulation.infrastructure.remote.ConfirmExecutionRequestDto
import pe.edu.upc.viora.features.croploadregulation.infrastructure.remote.ExecutionConfirmationResponseDto
import pe.edu.upc.viora.features.croploadregulation.infrastructure.remote.PrescriptionResponseDto
import pe.edu.upc.viora.features.croploadregulation.infrastructure.remote.RecordFullBloomRequestDto
import pe.edu.upc.viora.features.croploadregulation.infrastructure.remote.ThinningPrescriptionService

class ThinningPrescriptionRepositoryImpl @Inject constructor(
    private val service: ThinningPrescriptionService,
    private val dao: ThinningPrescriptionDao,
    private val apiCaller: ApiCaller,
    private val clock: Clock,
) : ThinningPrescriptionRepository {

    override fun observePrescription(plotId: String, campaignYear: Int): Flow<CachedPrescription?> =
        dao.observe(plotId, campaignYear).map { it?.toCached() }

    override fun observeCampaign(campaignYear: Int): Flow<List<CampaignPlan>> =
        dao.observeCampaign(campaignYear).map { rows -> rows.map { it.toCampaignPlan() } }

    override suspend fun refreshCampaign(campaignYear: Int): AppResult<Unit> {
        val plans = when (val result = apiCaller.call { service.getPrescriptions(campaignYear) }) {
            is AppResult.Success -> result.value
            is AppResult.Failure -> return result
        }
        val saved = guarded { plans.forEach { save(it, it.plotId, campaignYear) } }
        if (saved is AppResult.Failure) return saved
        // Thinnings recorded elsewhere are fetched too, so the list shows the real share. Best effort.
        plans.filter { it.status == EXECUTED && dao.find(it.plotId, campaignYear)?.executionJson == null }
            .forEach { refreshExecution(it.plotId, campaignYear, it.id) }
        return saved
    }

    override suspend fun refreshPrescription(plotId: String, campaignYear: Int): AppResult<Unit> {
        // 404 is an answer, not a failure: nothing was sampled yet, so the plot has no plan for the campaign.
        val remote: PrescriptionResponseDto? = when (val result = apiCaller.call { service.getPrescription(plotId, campaignYear) }) {
            is AppResult.Success -> result.value
            is AppResult.Failure -> if (result.error is AppError.NotFound) null else return result
        }
        val saved = guarded { save(remote, plotId, campaignYear) }
        // Recorded elsewhere (another phone, a reinstall): fetch it so P61 shows the real share. Best effort.
        if (saved is AppResult.Success && remote?.status == EXECUTED && dao.find(plotId, campaignYear)?.executionJson == null) {
            refreshExecution(plotId, campaignYear, remote.id)
        }
        return saved
    }

    override suspend fun recordFullBloom(
        plotId: String,
        campaignYear: Int,
        observedOn: LocalDate,
    ): AppResult<ThinningPrescription> {
        val request = RecordFullBloomRequestDto(campaignYear = campaignYear, observedOn = observedOn.toString())
        return when (val result = apiCaller.call { service.recordFullBloom(plotId, request) }) {
            is AppResult.Failure -> result
            is AppResult.Success -> {
                val saved = guarded { save(result.value, plotId, campaignYear) }
                if (saved is AppResult.Failure) saved else AppResult.Success(result.value.toDomain())
            }
        }
    }

    override fun observeExecution(plotId: String, campaignYear: Int): Flow<ThinningExecution?> =
        dao.observe(plotId, campaignYear).map { it?.toExecution() }

    override suspend fun confirmExecution(
        plotId: String,
        campaignYear: Int,
        prescriptionId: String,
        executedOn: LocalDate,
        removalPercentage: Double,
        notes: String?,
    ): AppResult<ThinningExecution> {
        val request = ConfirmExecutionRequestDto(
            executedDate = executedOn.toString(),
            actualRemovalPercentage = removalPercentage,
            notes = notes?.trim()?.takeIf { it.isNotEmpty() },
        )
        val remote = when (val result = apiCaller.call { service.confirmExecution(prescriptionId, request) }) {
            is AppResult.Success -> result.value
            // 409: it was already recorded (e.g. from another phone, or the answer of a first try was lost).
            is AppResult.Failure -> if (result.error is AppError.Conflict) {
                when (val existing = apiCaller.call { service.getExecution(prescriptionId) }) {
                    is AppResult.Success -> existing.value
                    is AppResult.Failure -> return result
                }
            } else {
                return result
            }
        }
        val execution = remote.toDomain() ?: return AppResult.Failure(AppError.Unknown())
        val saved = guarded { saveExecution(remote, plotId, campaignYear) }
        if (saved is AppResult.Failure) return saved
        // Best effort: the plan turns EXECUTED; without signal P61 catches up on its next refresh.
        refreshPrescription(plotId, campaignYear)
        return AppResult.Success(execution)
    }

    override suspend fun refreshExecution(plotId: String, campaignYear: Int, prescriptionId: String): AppResult<Unit> =
        when (val result = apiCaller.call { service.getExecution(prescriptionId) }) {
            is AppResult.Success -> guarded { saveExecution(result.value, plotId, campaignYear) }
            is AppResult.Failure -> if (result.error is AppError.NotFound) AppResult.Success(Unit) else result
        }

    /** Writes the plan keeping the thinning already cached for it. */
    private suspend fun save(remote: PrescriptionResponseDto?, plotId: String, campaignYear: Int) {
        val executionJson = dao.find(plotId, campaignYear)?.executionJson
        dao.upsert(remote.toEntity(plotId, campaignYear, clock.millis(), executionJson))
    }

    /** The row exists once the plan was downloaded; otherwise one without plan holds the thinning until it is. */
    private suspend fun saveExecution(remote: ExecutionConfirmationResponseDto, plotId: String, campaignYear: Int) {
        if (dao.find(plotId, campaignYear) == null) {
            dao.upsert(null.toEntity(plotId, campaignYear, clock.millis(), remote.toJson()))
        } else {
            dao.updateExecution(plotId, campaignYear, remote.toJson())
        }
    }

    private suspend fun guarded(block: suspend () -> Unit): AppResult<Unit> = try {
        block()
        AppResult.Success(Unit)
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (throwable: Throwable) {
        AppResult.Failure(AppError.Unknown(throwable))
    }

    private companion object {
        const val EXECUTED = "EXECUTED"
    }
}
