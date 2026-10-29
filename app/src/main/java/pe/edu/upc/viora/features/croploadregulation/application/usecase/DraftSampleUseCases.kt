package pe.edu.upc.viora.features.croploadregulation.application.usecase

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.domain.IdGenerator
import pe.edu.upc.viora.core.domain.isConnectivity
import pe.edu.upc.viora.features.croploadregulation.domain.entity.DraftSample
import pe.edu.upc.viora.features.croploadregulation.domain.entity.SamplingSummary
import pe.edu.upc.viora.features.croploadregulation.domain.entity.TreeSample
import pe.edu.upc.viora.features.croploadregulation.domain.repository.ThinningRepository
import pe.edu.upc.viora.features.croploadregulation.domain.service.SamplingSyncScheduler

/**
 * P63 · Registrar árbol: keeps the sample on the phone with the batch id it will always be sent with, and asks
 * for a background send in case the screen cannot send it now.
 */
class RecordTreeSampleUseCase @Inject constructor(
    private val repository: ThinningRepository,
    private val idGenerator: IdGenerator,
    private val scheduler: SamplingSyncScheduler,
) {
    suspend operator fun invoke(plotId: String, plotName: String, campaignYear: Int, sample: TreeSample): DraftSample {
        val draft = repository.saveDraftSample(plotId, plotName, campaignYear, sample, idGenerator.newUuid().value)
        scheduler.schedule()
        return draft
    }
}

/** The samples kept on the phone for a plot's round, oldest first. */
class ObserveDraftSamplesUseCase @Inject constructor(private val repository: ThinningRepository) {
    operator fun invoke(plotId: String, campaignYear: Int): Flow<List<DraftSample>> =
        repository.observeDrafts(plotId, campaignYear)
}

/** The round is finished: its kept samples leave the phone. */
class ClearDraftSamplesUseCase @Inject constructor(private val repository: ThinningRepository) {
    suspend operator fun invoke(plotId: String, campaignYear: Int) = repository.clearDrafts(plotId, campaignYear)
}

/** What sending some drafts left behind. */
data class SampleSyncOutcome(
    val syncedTrees: Set<String> = emptySet(),
    val droppedTrees: Set<String> = emptySet(),
    val summary: SamplingSummary? = null,
    val offline: Boolean = false,
    val rejectedMessage: String? = null,
    /** Some draft is still waiting (offline or another failure): it goes again later with the same batch id. */
    val pendingLeft: Boolean = false,
)

/**
 * Sends drafts one at a time, each with the batch id it keeps, under one lock shared by the screen and the
 * background worker, so two sends never overlap. A draft the server takes is marked synced and never sent
 * again; one it refuses for good (409, such as an already issued prescription) is dropped and its reason
 * kept; losing the connection stops the rest, which stay for the next attempt.
 *
 * A refusal is also kept per round until a screen of that round takes it, so the reason is not lost when the
 * background worker is the one that got it, nor when the screen opens after it happened.
 */
@Singleton
class SyncDraftSamplesUseCase @Inject constructor(
    private val repository: ThinningRepository,
    private val submitSamplingBatch: SubmitSamplingBatchUseCase,
    private val idGenerator: IdGenerator,
    private val scheduler: SamplingSyncScheduler,
) {
    /** One plot's sampling round of a campaign. */
    private data class SamplingRound(val plotId: String, val campaignYear: Int)

    private val sendLock = Mutex()
    private val rejections = MutableStateFlow<Map<SamplingRound, String>>(emptyMap())

    /** Why the server refused a draft of this round for good, until [acknowledgeRejection] takes it. */
    fun observeRejection(plotId: String, campaignYear: Int): Flow<String?> =
        rejections.map { it[SamplingRound(plotId, campaignYear)] }.distinctUntilChanged()

    /** The screen of this round now shows the refusal. */
    fun acknowledgeRejection(plotId: String, campaignYear: Int) =
        rejections.update { it - SamplingRound(plotId, campaignYear) }

    suspend operator fun invoke(drafts: List<DraftSample>): SampleSyncOutcome = sendLock.withLock {
        // Read again under the lock: a draft another send took meanwhile is not sent twice.
        val stillPending = repository.unsyncedDrafts().map { it.id }.toSet()
        val outcome = send(drafts.filter { it.id in stillPending })
        if (outcome.pendingLeft) scheduler.schedule()
        outcome
    }

    /** Every draft of any plot not sent yet; for the background worker. */
    suspend fun syncAllPending(): SampleSyncOutcome = sendLock.withLock { send(repository.unsyncedDrafts()) }

    private suspend fun send(drafts: List<DraftSample>): SampleSyncOutcome {
        var outcome = SampleSyncOutcome()
        for (draft in drafts) {
            val batchId = draft.batchId ?: idGenerator.newUuid().value.also { repository.assignBatchId(draft.id, it) }
            when (val result = submitSamplingBatch(draft.plotId, draft.campaignYear, batchId, listOf(draft.sample))) {
                is AppResult.Success -> {
                    repository.markSynced(draft.id)
                    outcome = outcome.copy(syncedTrees = outcome.syncedTrees + draft.treeIdentifier, summary = result.value)
                }
                is AppResult.Failure -> {
                    val error = result.error
                    when {
                        error.isConnectivity -> return outcome.copy(offline = true, pendingLeft = true)
                        error is AppError.Conflict -> {
                            repository.deleteDraft(draft.id)
                            error.detail?.let { reason ->
                                rejections.update { it + (SamplingRound(draft.plotId, draft.campaignYear) to reason) }
                            }
                            outcome = outcome.copy(
                                droppedTrees = outcome.droppedTrees + draft.treeIdentifier,
                                rejectedMessage = error.detail ?: outcome.rejectedMessage,
                            )
                        }
                        // Any other failure keeps the draft, to be sent again later with the same batch id.
                        else -> return outcome.copy(pendingLeft = true)
                    }
                }
            }
        }
        return outcome
    }
}
