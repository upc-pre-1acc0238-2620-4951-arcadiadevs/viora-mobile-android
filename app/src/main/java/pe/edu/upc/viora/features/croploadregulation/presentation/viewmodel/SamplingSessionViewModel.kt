package pe.edu.upc.viora.features.croploadregulation.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import java.time.Year
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.domain.fold
import pe.edu.upc.viora.core.domain.isConnectivity
import pe.edu.upc.viora.core.network.ConnectivityObserver
import pe.edu.upc.viora.features.croploadregulation.application.usecase.ClearDraftSamplesUseCase
import pe.edu.upc.viora.features.croploadregulation.application.usecase.ObserveDraftSamplesUseCase
import pe.edu.upc.viora.features.croploadregulation.application.usecase.RecordTreeSampleUseCase
import pe.edu.upc.viora.features.croploadregulation.application.usecase.SampleSyncOutcome
import pe.edu.upc.viora.features.croploadregulation.application.usecase.SyncDraftSamplesUseCase
import pe.edu.upc.viora.features.croploadregulation.domain.entity.DraftSample
import pe.edu.upc.viora.features.croploadregulation.domain.entity.SamplingSummary
import pe.edu.upc.viora.features.croploadregulation.domain.entity.TreeSample
import pe.edu.upc.viora.features.croploadregulation.domain.repository.ThinningRepository
import pe.edu.upc.viora.features.croploadregulation.presentation.state.SamplingSessionUiState

@HiltViewModel
class SamplingSessionViewModel @Inject constructor(
    private val recordTreeSample: RecordTreeSampleUseCase,
    private val syncDraftSamples: SyncDraftSamplesUseCase,
    private val observeDrafts: ObserveDraftSamplesUseCase,
    private val clearDraftSamples: ClearDraftSamplesUseCase,
    private val thinningRepository: ThinningRepository,
    private val connectivity: ConnectivityObserver,
    private val clock: Clock,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SamplingSessionUiState())
    val uiState: StateFlow<SamplingSessionUiState> = _uiState.asStateFlow()

    private fun isDeviceOffline(): Boolean = !connectivity.isOnline()

    /** Follows the round on screen, so what the background worker sends or loses shows up here too. */
    private var roundJob: Job? = null
    private var followedRound: Pair<String, Int>? = null

    /** Trees whose draft this screen has seen kept on the phone. */
    private var seenDraftTrees: Set<String> = emptySet()

    /** Sends started by this screen; their result is applied by the screen itself. */
    private var ownSends = 0

    private suspend fun sendFromScreen(drafts: List<DraftSample>): SampleSyncOutcome {
        ownSends++
        return try {
            syncDraftSamples(drafts)
        } finally {
            ownSends--
        }
    }

    private fun followRound(plotId: String, campaignYear: Int) {
        if (followedRound == plotId to campaignYear) return
        roundJob?.cancel()
        followedRound = plotId to campaignYear
        seenDraftTrees = emptySet()
        roundJob = viewModelScope.launch {
            launch { observeDrafts(plotId, campaignYear).collect(::reconcile) }
            launch {
                syncDraftSamples.observeRejection(plotId, campaignYear).filterNotNull().collect { reason ->
                    _uiState.update { it.copy(rejectedMessage = reason) }
                    syncDraftSamples.acknowledgeRejection(plotId, campaignYear)
                }
            }
        }
    }

    /**
     * Brings the trees on screen in line with the drafts kept on the phone: a draft that became synced marks its
     * tree as sent, and a pending draft that left the phone (refused for good) takes its tree off the screen.
     * When nobody on this screen sent them, the background worker did: the summary is read again.
     */
    private fun reconcile(drafts: List<DraftSample>) {
        val byTree = drafts.associateBy { it.treeIdentifier }
        val dropped = seenDraftTrees - byTree.keys
        seenDraftTrees = seenDraftTrees + byTree.keys

        fun merge(samples: List<TreeSample>): List<TreeSample> = samples.mapNotNull { sample ->
            val draft = byTree[sample.treeIdentifier]
            when {
                sample.isSynced -> sample
                draft?.isSynced == true -> sample.copy(isSynced = true)
                draft == null && sample.treeIdentifier in dropped -> null
                else -> sample
            }
        }

        val before = _uiState.value
        val sentInBackground = before.samples.count { !it.isSynced && byTree[it.treeIdentifier]?.isSynced == true }
        _uiState.update { it.copy(samples = merge(it.samples)) }

        if (sentInBackground > 0 && ownSends == 0 && !before.isSubmitting) {
            _uiState.update {
                it.copy(
                    isOffline = false,
                    hasRecoveredConnection = before.isOffline,
                    syncedOnResumeCount = if (before.isOffline) sentInBackground else it.syncedOnResumeCount,
                )
            }
            viewModelScope.launch {
                thinningRepository.getSamplingSummary(before.plotId, before.campaignYear).fold(
                    onSuccess = { summary -> _uiState.update { it.copy(submissionSummary = summary) } },
                    onFailure = { },
                )
            }
        }
    }

    fun initSession(plotId: String, plotName: String, campaignYear: Int = Year.now(clock).value) {
        val currentlyOffline = isDeviceOffline()
        val isNewPlot = _uiState.value.plotId != plotId
        val wasOffline = if (isNewPlot) currentlyOffline else (_uiState.value.isOffline || currentlyOffline)

        if (!isNewPlot && _uiState.value.samples.isNotEmpty() && _uiState.value.submissionSummary != null && !wasOffline) {
            // Already active in this session and online with samples and summary loaded; avoid redundant fetch
            return
        }

        if (isNewPlot) {
            _uiState.value = SamplingSessionUiState(
                plotId = plotId,
                plotName = plotName,
                campaignYear = campaignYear,
                samples = emptyList(),
                targetTreesCount = 5,
                isLoading = !currentlyOffline,
                isOffline = currentlyOffline,
                hasRecoveredConnection = false,
            )
        } else {
            _uiState.update {
                it.copy(
                    isLoading = if (!currentlyOffline && it.submissionSummary == null) true else it.isLoading,
                    isOffline = currentlyOffline,
                    hasRecoveredConnection = false,
                )
            }
        }

        if (isNewPlot || wasOffline || _uiState.value.submissionSummary == null) {
            viewModelScope.launch {
                followRound(plotId, campaignYear)
                val drafts = observeDrafts(plotId, campaignYear).first()
                if (drafts.isNotEmpty()) {
                    val domainSamples = drafts.map { it.sample }
                    _uiState.update { it.copy(samples = domainSamples) }

                    val unsynced = drafts.filter { !it.isSynced }
                    if (unsynced.isNotEmpty()) {
                        if (isDeviceOffline()) {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    isOffline = true,
                                    hasRecoveredConnection = false,
                                )
                            }
                            return@launch
                        }

                        val outcome = sendFromScreen(unsynced)
                        _uiState.update { current ->
                            val synced = outcome.syncedTrees
                            val updated = current.samples
                                .filterNot { it.treeIdentifier in outcome.droppedTrees }
                                .map { if (it.treeIdentifier in synced) it.copy(isSynced = true) else it }
                            current.copy(
                                isLoading = false,
                                samples = updated,
                                submissionSummary = outcome.summary ?: current.submissionSummary,
                                isOffline = outcome.offline || (outcome.summary == null && current.isOffline),
                                hasRecoveredConnection = synced.isNotEmpty(),
                                syncedOnResumeCount = synced.size,
                                rejectedMessage = outcome.rejectedMessage ?: current.rejectedMessage,
                            )
                        }
                    } else {
                        if (!isDeviceOffline() && _uiState.value.submissionSummary == null) {
                            thinningRepository.getSamplingSummary(plotId, campaignYear).fold(
                                onSuccess = { summary ->
                                    _uiState.update { it.copy(isLoading = false, submissionSummary = summary) }
                                },
                                onFailure = {
                                    _uiState.update { it.copy(isLoading = false) }
                                },
                            )
                        } else {
                            _uiState.update { it.copy(isLoading = false) }
                        }
                    }
                } else if (!isDeviceOffline()) {
                    thinningRepository.getSamplingDetailed(plotId, campaignYear).fold(
                        onSuccess = { report ->
                            _uiState.update { current ->
                                current.copy(
                                    isLoading = false,
                                    samples = report.trees,
                                    submissionSummary = report.summary,
                                    targetTreesCount = (report.summary.evaluatedTreesCount + report.summary.treesNeeded).coerceAtLeast(5),
                                    isOffline = false,
                                )
                            }
                        },
                        onFailure = {
                            _uiState.update { it.copy(isLoading = false) }
                        },
                    )
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun addSample(
        treeIdentifier: String,
        shootsCount: Int,
        fruitSetCount: Int,
        trunkCircumferenceCm: Double?,
    ) {
        val currentState = _uiState.value
        val sample = TreeSample(
            treeIdentifier = treeIdentifier.ifBlank { currentState.nextTreeIdentifier },
            shootsCount = shootsCount,
            fruitSetCount = fruitSetCount,
            trunkCircumferenceCm = trunkCircumferenceCm,
            observedOn = LocalDate.now(clock),
            isSynced = false,
        )
        _uiState.update { current ->
            current.copy(
                samples = current.samples + sample,
                hasRecoveredConnection = false,
            )
        }
        viewModelScope.launch {
            val draft = recordTreeSample(currentState.plotId, currentState.plotName, currentState.campaignYear, sample)

            if (isDeviceOffline()) {
                _uiState.update { it.copy(isOffline = true, hasRecoveredConnection = false) }
                return@launch
            }

            val outcome = sendFromScreen(listOf(draft))
            _uiState.update { current ->
                current.copy(
                    samples = current.samples
                        .filterNot { it.treeIdentifier in outcome.droppedTrees }
                        .map { if (it.treeIdentifier in outcome.syncedTrees) it.copy(isSynced = true) else it },
                    submissionSummary = outcome.summary ?: current.submissionSummary,
                    isOffline = outcome.offline || (outcome.summary == null && current.isOffline),
                    hasRecoveredConnection = false,
                    rejectedMessage = outcome.rejectedMessage ?: current.rejectedMessage,
                )
            }
        }
    }

    fun submitCurrentBatch(
        onSuccess: (SamplingSummary) -> Unit,
        onOffline: (() -> Unit)? = null,
    ) {
        val state = _uiState.value
        if (state.samples.isEmpty() || state.plotId.isBlank()) return

        _uiState.update { it.copy(isSubmitting = true, error = null) }

        viewModelScope.launch {
            // Only the trees the server has not taken yet are sent, each with its own batch id: what is already
            // synced is never sent again, so no round is counted twice.
            val unsynced = observeDrafts(state.plotId, state.campaignYear).first().filter { !it.isSynced }
            val outcome = sendFromScreen(unsynced)
            val result = when {
                outcome.offline -> null
                unsynced.isEmpty() || outcome.summary == null ->
                    thinningRepository.getSamplingSummary(state.plotId, state.campaignYear)
                else -> AppResult.Success(outcome.summary)
            }
            when {
                result is AppResult.Success -> {
                    clearDraftSamples(state.plotId, state.campaignYear)
                    val hadRecovered = _uiState.value.isOffline
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            submissionSummary = result.value,
                            isOffline = false,
                            hasRecoveredConnection = hadRecovered,
                            error = null,
                            rejectedMessage = outcome.rejectedMessage ?: it.rejectedMessage,
                        )
                    }
                    onSuccess(result.value)
                }
                else -> {
                    val error = (result as? AppResult.Failure)?.error ?: AppError.Offline
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            isOffline = error.isConnectivity || it.isOffline,
                            error = error,
                            rejectedMessage = outcome.rejectedMessage ?: it.rejectedMessage,
                        )
                    }
                    if (error.isConnectivity && onOffline != null) {
                        onOffline()
                    }
                }
            }
        }
    }

    fun setOffline(isOffline: Boolean) {
        _uiState.update { it.copy(isOffline = isOffline) }
    }

    fun setRecoveredConnection(hasRecovered: Boolean) {
        _uiState.update { it.copy(hasRecoveredConnection = hasRecovered) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
