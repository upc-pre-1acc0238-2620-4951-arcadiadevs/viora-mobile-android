package pe.edu.upc.viora.features.croploadregulation.presentation

import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.croploadregulation.domain.entity.CachedPrescription
import pe.edu.upc.viora.features.croploadregulation.domain.entity.ThinningExecution
import pe.edu.upc.viora.features.croploadregulation.domain.entity.ThinningPrescription
import pe.edu.upc.viora.features.croploadregulation.domain.repository.ThinningPrescriptionRepository
import pe.edu.upc.viora.features.croploadregulation.issuedPlan
import pe.edu.upc.viora.features.croploadregulation.recordedThinning

/** In-memory plan and thinning of one plot, recording what the view models asked for. */
internal class FakePlanRepository : ThinningPrescriptionRepository {
    val cached = MutableStateFlow<CachedPrescription?>(null)
    val execution = MutableStateFlow<ThinningExecution?>(null)
    var refreshResult: AppResult<Unit> = AppResult.Success(Unit)
    var bloomResult: AppResult<ThinningPrescription>? = null
    var confirmResult: AppResult<ThinningExecution>? = null
    var refreshExecutionResult: AppResult<Unit> = AppResult.Success(Unit)
    var refreshCalls = 0
    var refreshExecutionCalls = 0
    val bloomDates = mutableListOf<LocalDate>()
    val confirmations = mutableListOf<Confirmation>()

    data class Confirmation(val prescriptionId: String, val executedOn: LocalDate, val percentage: Double, val notes: String?)

    override fun observePrescription(plotId: String, campaignYear: Int): Flow<CachedPrescription?> = cached

    override suspend fun refreshPrescription(plotId: String, campaignYear: Int): AppResult<Unit> {
        refreshCalls++
        return refreshResult
    }

    override suspend fun recordFullBloom(plotId: String, campaignYear: Int, observedOn: LocalDate): AppResult<ThinningPrescription> {
        bloomDates += observedOn
        val result = bloomResult ?: AppResult.Success(issuedPlan())
        if (result is AppResult.Success) cached.value = CachedPrescription(result.value, Instant.EPOCH)
        return result
    }

    override fun observeExecution(plotId: String, campaignYear: Int): Flow<ThinningExecution?> = execution

    override suspend fun confirmExecution(
        plotId: String,
        campaignYear: Int,
        prescriptionId: String,
        executedOn: LocalDate,
        removalPercentage: Double,
        notes: String?,
    ): AppResult<ThinningExecution> {
        confirmations += Confirmation(prescriptionId, executedOn, removalPercentage, notes)
        val result = confirmResult ?: AppResult.Success(recordedThinning())
        if (result is AppResult.Success) execution.value = result.value
        return result
    }

    override suspend fun refreshExecution(plotId: String, campaignYear: Int, prescriptionId: String): AppResult<Unit> {
        refreshExecutionCalls++
        return refreshExecutionResult
    }
}
