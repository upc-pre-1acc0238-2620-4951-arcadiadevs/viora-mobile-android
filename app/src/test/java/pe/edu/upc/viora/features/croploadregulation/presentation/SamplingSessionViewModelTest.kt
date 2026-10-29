package pe.edu.upc.viora.features.croploadregulation.presentation

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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.domain.RandomIdGenerator
import pe.edu.upc.viora.core.network.ConnectivityObserver
import pe.edu.upc.viora.features.croploadregulation.application.usecase.ClearDraftSamplesUseCase
import pe.edu.upc.viora.features.croploadregulation.application.usecase.ObserveDraftSamplesUseCase
import pe.edu.upc.viora.features.croploadregulation.application.usecase.RecordTreeSampleUseCase
import pe.edu.upc.viora.features.croploadregulation.application.usecase.SubmitSamplingBatchUseCase
import pe.edu.upc.viora.features.croploadregulation.application.usecase.SyncDraftSamplesUseCase
import pe.edu.upc.viora.features.croploadregulation.domain.entity.DraftSample
import pe.edu.upc.viora.features.croploadregulation.domain.entity.PlotSamplingOverview
import pe.edu.upc.viora.features.croploadregulation.domain.entity.SamplingDetailedReport
import pe.edu.upc.viora.features.croploadregulation.domain.entity.SamplingSummary
import pe.edu.upc.viora.features.croploadregulation.domain.entity.ThinningEvent
import pe.edu.upc.viora.features.croploadregulation.domain.entity.TreeSample
import pe.edu.upc.viora.features.croploadregulation.domain.repository.ThinningRepository
import pe.edu.upc.viora.features.croploadregulation.domain.service.SamplingSyncScheduler
import pe.edu.upc.viora.features.croploadregulation.presentation.viewmodel.SamplingSessionViewModel

private class FakeThinningRepo : ThinningRepository {
    var submitResult: AppResult<SamplingSummary> = AppResult.Success(
        SamplingSummary(
            plotId = "1",
            campaignYear = 2026,
            evaluatedTreesCount = 3,
            sampledShootsCount = 120,
            sampledFruitSetCount = 70,
            meanFruitsPerShoot = 0.58,
            isRepresentative = false,
            treesNeeded = 2,
        ),
    )
    var detailedResult: AppResult<SamplingDetailedReport> = AppResult.Success(
        SamplingDetailedReport(
            summary = SamplingSummary(
                plotId = "1",
                campaignYear = 2026,
                evaluatedTreesCount = 0,
                sampledShootsCount = 0,
                sampledFruitSetCount = 0,
                meanFruitsPerShoot = 0.0,
                isRepresentative = false,
                treesNeeded = 5,
            ),
            trees = emptyList(),
        ),
    )
    val submittedBatches = mutableListOf<List<TreeSample>>()
    val submittedBatchIds = mutableListOf<String>()

    override suspend fun getPlotSamplingOverview(campaignYear: Int?): AppResult<List<PlotSamplingOverview>> =
        AppResult.Success(emptyList())

    override suspend fun getSamplingSummary(plotId: String, campaignYear: Int?): AppResult<SamplingSummary> =
        submitResult

    override suspend fun getSamplingDetailed(plotId: String, campaignYear: Int?): AppResult<SamplingDetailedReport> =
        detailedResult

    override suspend fun submitSamplingBatch(
        plotId: String,
        campaignYear: Int,
        batchId: String,
        samples: List<TreeSample>,
    ): AppResult<SamplingSummary> {
        submittedBatches.add(samples)
        submittedBatchIds.add(batchId)
        return submitResult
    }

    override suspend fun getThinningEvents(campaignYear: Int?, plotId: String?): AppResult<List<ThinningEvent>> =
        AppResult.Success(emptyList())

    override fun observeThinningEvents(): Flow<List<ThinningEvent>?> =
        MutableStateFlow(null)

    override fun observeActiveSampling(): Flow<PlotSamplingOverview?> =
        MutableStateFlow(null)

    override fun observePendingDraftSamplesCount(): Flow<Int> =
        MutableStateFlow(drafts.count { !it.isSynced })

    /** Like the Room table: every change reaches whoever observes the drafts. */
    val draftStore = MutableStateFlow<List<DraftSample>>(emptyList())
    val drafts: List<DraftSample> get() = draftStore.value

    fun keep(draft: DraftSample) {
        draftStore.value = draftStore.value.filterNot { it.id == draft.id } + draft
    }

    private fun update(id: String, change: (DraftSample) -> DraftSample) {
        draftStore.value = draftStore.value.map { if (it.id == id) change(it) else it }
    }

    override suspend fun saveDraftSample(plotId: String, plotName: String, campaignYear: Int, sample: TreeSample, batchId: String): DraftSample =
        DraftSample("$plotId-${sample.treeIdentifier}", plotId, plotName, campaignYear, sample, batchId).also(::keep)

    override fun observeDrafts(plotId: String, campaignYear: Int): Flow<List<DraftSample>> =
        draftStore.map { all -> all.filter { it.plotId == plotId && it.campaignYear == campaignYear } }

    override suspend fun unsyncedDrafts(): List<DraftSample> = drafts.filter { !it.isSynced }

    override suspend fun assignBatchId(draftId: String, batchId: String) = update(draftId) { it.copy(batchId = batchId) }

    override suspend fun markSynced(draftId: String) = update(draftId) { it.copy(sample = it.sample.copy(isSynced = true)) }

    override suspend fun deleteDraft(draftId: String) {
        draftStore.value = draftStore.value.filterNot { it.id == draftId }
    }

    override suspend fun clearDrafts(plotId: String, campaignYear: Int) {
        draftStore.value = draftStore.value.filterNot { it.plotId == plotId && it.campaignYear == campaignYear }
    }
}

private class CountingScheduler : SamplingSyncScheduler {
    var scheduled = 0
    override fun schedule() {
        scheduled++
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SamplingSessionViewModelTest {

    private val fakeRepo = FakeThinningRepo()
    private val scheduler = CountingScheduler()
    private val ids = RandomIdGenerator()
    private val sync = SyncDraftSamplesUseCase(fakeRepo, SubmitSamplingBatchUseCase(fakeRepo), ids, scheduler)

    private fun viewModel() = SamplingSessionViewModel(
        recordTreeSample = RecordTreeSampleUseCase(fakeRepo, ids, scheduler),
        syncDraftSamples = sync,
        observeDrafts = ObserveDraftSamplesUseCase(fakeRepo),
        clearDraftSamples = ClearDraftSamplesUseCase(fakeRepo),
        thinningRepository = fakeRepo,
        connectivity = ConnectivityObserver { true },
        clock = java.time.Clock.systemUTC(),
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeRepo.draftStore.value = emptyList()
        fakeRepo.submittedBatches.clear()
        fakeRepo.submittedBatchIds.clear()
        fakeRepo.submitResult = AppResult.Success(
            SamplingSummary(
                plotId = "1",
                campaignYear = 2026,
                evaluatedTreesCount = 3,
                sampledShootsCount = 120,
                sampledFruitSetCount = 70,
                meanFruitsPerShoot = 0.58,
                isRepresentative = false,
                treesNeeded = 2,
            ),
        )
        fakeRepo.detailedResult = AppResult.Success(
            SamplingDetailedReport(
                summary = SamplingSummary(
                    plotId = "1",
                    campaignYear = 2026,
                    evaluatedTreesCount = 0,
                    sampledShootsCount = 0,
                    sampledFruitSetCount = 0,
                    meanFruitsPerShoot = 0.0,
                    isRepresentative = false,
                    treesNeeded = 5,
                ),
                trees = emptyList(),
            ),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initSession syncs unsynced drafts and triggers connection recovered state`() = runTest {
        // Pre-populate Room DB with 2 unsynced drafts
        fakeRepo.keep(
            DraftSample(
                id = "sample-1", plotId = "plot-1", plotName = "Lote Norte", campaignYear = 2026, batchId = null,
                sample = TreeSample(treeIdentifier = "A-01", shootsCount = 40, fruitSetCount = 20, isSynced = false),
            ),
        )
        fakeRepo.keep(
            DraftSample(
                id = "sample-2", plotId = "plot-1", plotName = "Lote Norte", campaignYear = 2026, batchId = null,
                sample = TreeSample(treeIdentifier = "A-02", shootsCount = 38, fruitSetCount = 22, isSynced = false),
            ),
        )

        val viewModel = viewModel()
        viewModel.initSession(plotId = "plot-1", plotName = "Lote Norte", campaignYear = 2026)

        val state = viewModel.uiState.value
        assertEquals(2, state.samples.size)
        assertTrue(state.samples.all { it.isSynced })
        assertTrue(state.hasRecoveredConnection)
        assertEquals(2, state.syncedOnResumeCount)
        assertFalse(state.isOffline)
        assertTrue(fakeRepo.drafts.all { it.isSynced })
    }

    @Test
    fun `initSession sets isOffline when sync fails due to offline error`() = runTest {
        fakeRepo.submitResult = AppResult.Failure(AppError.Offline)
        fakeRepo.keep(
            DraftSample(
                id = "sample-1", plotId = "plot-1", plotName = "Lote Norte", campaignYear = 2026, batchId = null,
                sample = TreeSample(treeIdentifier = "A-01", shootsCount = 40, fruitSetCount = 20, isSynced = false),
            ),
        )

        val viewModel = viewModel()
        viewModel.initSession(plotId = "plot-1", plotName = "Lote Norte", campaignYear = 2026)

        val state = viewModel.uiState.value
        assertEquals(1, state.samples.size)
        assertFalse(state.samples[0].isSynced)
        assertTrue(state.isOffline)
        assertFalse(state.hasRecoveredConnection)
    }

    @Test
    fun `initSession loads detailed sampling and summary from repo when local drafts are empty`() = runTest {
        // No local drafts (e.g. plot completed or opening on a new device)
        fakeRepo.detailedResult = AppResult.Success(
            SamplingDetailedReport(
                summary = SamplingSummary(
                    plotId = "plot-1",
                    campaignYear = 2026,
                    evaluatedTreesCount = 5,
                    sampledShootsCount = 200,
                    sampledFruitSetCount = 120,
                    meanFruitsPerShoot = 0.60,
                    isRepresentative = true,
                    treesNeeded = 0,
                ),
                trees = listOf(
                    TreeSample(treeIdentifier = "A-01", shootsCount = 40, fruitSetCount = 24, isSynced = true),
                    TreeSample(treeIdentifier = "A-02", shootsCount = 40, fruitSetCount = 24, isSynced = true),
                    TreeSample(treeIdentifier = "A-03", shootsCount = 40, fruitSetCount = 24, isSynced = true),
                    TreeSample(treeIdentifier = "A-04", shootsCount = 40, fruitSetCount = 24, isSynced = true),
                    TreeSample(treeIdentifier = "A-05", shootsCount = 40, fruitSetCount = 24, isSynced = true),
                ),
            ),
        )
        val viewModel = viewModel()
        viewModel.initSession(plotId = "plot-1", plotName = "Lote Norte", campaignYear = 2026)

        val state = viewModel.uiState.value
        assertEquals(5, state.samples.size)
        assertEquals(5, state.evaluatedTreesCount)
        assertEquals(200, state.totalShootsCount)
        assertEquals(120, state.totalFruitsCount)
        assertEquals(0.60, state.meanFruitsPerShoot, 0.001)
        assertTrue(state.isRepresentative)
        assertEquals(5, state.submissionSummary?.evaluatedTreesCount)
        assertFalse(state.isLoading)
        assertFalse(state.isOffline)
    }

    @Test
    fun `addSample records sample locally and sets isOffline if network fails`() = runTest {
        fakeRepo.submitResult = AppResult.Failure(AppError.Offline)
        val viewModel = viewModel()
        viewModel.initSession(plotId = "plot-1", plotName = "Lote Norte", campaignYear = 2026)

        viewModel.addSample(
            treeIdentifier = "A-01",
            shootsCount = 42,
            fruitSetCount = 24,
            trunkCircumferenceCm = 85.0,
        )

        val state = viewModel.uiState.value
        assertEquals(1, state.samples.size)
        assertEquals("A-01", state.samples[0].treeIdentifier)
        assertFalse(state.samples[0].isSynced)
        assertTrue(state.isOffline)
        assertEquals(1, fakeRepo.drafts.size)
        assertFalse(fakeRepo.drafts[0].isSynced)
    }

    @Test
    fun `addSample records sample and syncs immediately to backend when online`() = runTest {
        val viewModel = viewModel()
        viewModel.initSession(plotId = "plot-1", plotName = "Lote Norte", campaignYear = 2026)

        viewModel.addSample(
            treeIdentifier = "A-01",
            shootsCount = 42,
            fruitSetCount = 24,
            trunkCircumferenceCm = 85.0,
        )

        val state = viewModel.uiState.value
        assertEquals(1, state.samples.size)
        assertEquals("A-01", state.samples[0].treeIdentifier)
        assertTrue(state.samples[0].isSynced)
        assertFalse(state.isOffline)
        assertEquals(1, fakeRepo.submittedBatches.size)
        assertEquals(1, fakeRepo.drafts.size)
        assertTrue(fakeRepo.drafts[0].isSynced)
    }

    private fun draft(id: String, tree: String, synced: Boolean = false, batchId: String? = null) = DraftSample(
        id = id, plotId = "plot-1", plotName = "Lote Norte", campaignYear = 2026, batchId = batchId,
        sample = TreeSample(treeIdentifier = tree, shootsCount = 40, fruitSetCount = 20, isSynced = synced),
    )

    @Test
    fun `a draft is always sent again with the batch id it keeps`() = runTest {
        fakeRepo.keep(draft("sample-1", "A-01", batchId = "batch-original"))
        fakeRepo.submitResult = AppResult.Failure(AppError.Server(500))
        val viewModel = viewModel()

        viewModel.initSession("plot-1", "Lote Norte", 2026)
        fakeRepo.submitResult = AppResult.Success(fakeRepo.detailedResult.let { (it as AppResult.Success).value.summary })
        viewModel.initSession("plot-1", "Lote Norte", 2026)

        assertEquals(listOf("batch-original", "batch-original"), fakeRepo.submittedBatchIds)
        assertTrue(fakeRepo.drafts.single().isSynced)
    }

    @Test
    fun `a draft the server refuses for good is dropped and its reason shown`() = runTest {
        fakeRepo.keep(draft("sample-1", "A-01", batchId = "batch-1"))
        fakeRepo.submitResult = AppResult.Failure(AppError.Conflict("La prescripcion ya fue emitida"))
        val viewModel = viewModel()

        viewModel.initSession("plot-1", "Lote Norte", 2026)

        assertTrue(fakeRepo.drafts.isEmpty())
        assertTrue(viewModel.uiState.value.samples.isEmpty())
        assertEquals("La prescripcion ya fue emitida", viewModel.uiState.value.rejectedMessage)
    }

    @Test
    fun `finishing with every tree already synced sends nothing again`() = runTest {
        fakeRepo.keep(draft("sample-1", "A-01", synced = true, batchId = "batch-1"))
        val viewModel = viewModel()
        viewModel.initSession("plot-1", "Lote Norte", 2026)
        var finished: SamplingSummary? = null

        viewModel.submitCurrentBatch(onSuccess = { finished = it })

        assertTrue(fakeRepo.submittedBatchIds.isEmpty())
        assertTrue(finished != null)
    }

    @Test
    fun `a draft another send already took is not sent twice`() = runTest {
        val stale = draft("sample-1", "A-01", batchId = "batch-1")
        fakeRepo.keep(stale.copy(sample = stale.sample.copy(isSynced = true)))

        val outcome = sync(listOf(stale))

        assertTrue(fakeRepo.submittedBatchIds.isEmpty())
        assertTrue(outcome.syncedTrees.isEmpty())
    }

    @Test
    fun `a draft left offline asks for a background send`() = runTest {
        fakeRepo.submitResult = AppResult.Failure(AppError.Offline)
        fakeRepo.keep(draft("sample-1", "A-01", batchId = "batch-1"))

        val outcome = sync(fakeRepo.drafts.toList())

        assertTrue(outcome.offline)
        assertTrue(outcome.pendingLeft)
        assertEquals(1, scheduler.scheduled)
        assertFalse(fakeRepo.drafts.single().isSynced)
    }

    // ---- The background worker and the screen share the same drafts --------------------------------------------

    private val conflict = AppResult.Failure(AppError.Conflict("La prescripcion ya fue emitida"))

    @Test
    fun `a tree the worker sends while the screen is open shows as sent and the banner as recovered`() = runTest {
        fakeRepo.submitResult = AppResult.Failure(AppError.Offline)
        val viewModel = viewModel()
        viewModel.initSession("plot-1", "Lote Norte", 2026)
        viewModel.addSample("A-01", shootsCount = 42, fruitSetCount = 24, trunkCircumferenceCm = null)
        assertTrue(viewModel.uiState.value.isOffline)

        fakeRepo.submitResult = AppResult.Success(summary(evaluated = 1))
        sync.syncAllPending()

        val state = viewModel.uiState.value
        assertTrue(state.samples.single().isSynced)
        assertFalse(state.isOffline)
        assertTrue(state.hasRecoveredConnection)
        assertEquals(1, state.syncedOnResumeCount)
        assertEquals(1, state.submissionSummary?.evaluatedTreesCount)
        assertEquals(2, fakeRepo.submittedBatchIds.size)
    }

    @Test
    fun `a tree the worker loses to a 409 while the screen is open leaves it with the reason`() = runTest {
        fakeRepo.submitResult = AppResult.Failure(AppError.Offline)
        val viewModel = viewModel()
        viewModel.initSession("plot-1", "Lote Norte", 2026)
        viewModel.addSample("A-01", shootsCount = 42, fruitSetCount = 24, trunkCircumferenceCm = null)

        fakeRepo.submitResult = conflict
        sync.syncAllPending()

        val state = viewModel.uiState.value
        assertTrue(state.samples.isEmpty())
        assertEquals("La prescripcion ya fue emitida", state.rejectedMessage)
        assertTrue(fakeRepo.drafts.isEmpty())
    }

    @Test
    fun `a tree the worker sent before the screen opens is shown as sent and not sent again`() = runTest {
        fakeRepo.keep(draft("sample-1", "A-01", batchId = "batch-1"))
        sync.syncAllPending()

        val viewModel = viewModel()
        viewModel.initSession("plot-1", "Lote Norte", 2026)

        assertEquals(listOf("batch-1"), fakeRepo.submittedBatchIds)
        assertTrue(viewModel.uiState.value.samples.single().isSynced)
    }

    @Test
    fun `a 409 the worker got before the screen opens is still shown once`() = runTest {
        fakeRepo.keep(draft("sample-1", "A-01", batchId = "batch-1"))
        fakeRepo.submitResult = conflict
        sync.syncAllPending()

        val first = viewModel()
        first.initSession("plot-1", "Lote Norte", 2026)
        val second = viewModel()
        second.initSession("plot-1", "Lote Norte", 2026)

        assertEquals("La prescripcion ya fue emitida", first.uiState.value.rejectedMessage)
        assertTrue(first.uiState.value.samples.isEmpty())
        assertEquals(null, second.uiState.value.rejectedMessage)
    }

    @Test
    fun `the screen sending first leaves nothing for the worker`() = runTest {
        val viewModel = viewModel()
        viewModel.initSession("plot-1", "Lote Norte", 2026)
        viewModel.addSample("A-01", shootsCount = 42, fruitSetCount = 24, trunkCircumferenceCm = null)

        val outcome = sync.syncAllPending()

        assertEquals(1, fakeRepo.submittedBatchIds.size)
        assertTrue(outcome.syncedTrees.isEmpty())
        assertFalse(viewModel.uiState.value.hasRecoveredConnection)
        assertTrue(viewModel.uiState.value.samples.single().isSynced)
    }

    private fun summary(evaluated: Int) = SamplingSummary(
        plotId = "plot-1",
        campaignYear = 2026,
        evaluatedTreesCount = evaluated,
        sampledShootsCount = 42,
        sampledFruitSetCount = 24,
        meanFruitsPerShoot = 0.57,
        isRepresentative = false,
        treesNeeded = 5 - evaluated,
    )
}
