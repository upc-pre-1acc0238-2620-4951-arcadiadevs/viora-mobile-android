package pe.edu.upc.viora.features.croploadregulation.presentation

import android.content.Context
import android.content.ContextWrapper
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
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.croploadregulation.application.usecase.SubmitSamplingBatchUseCase
import pe.edu.upc.viora.features.croploadregulation.domain.entity.PlotSamplingOverview
import pe.edu.upc.viora.features.croploadregulation.domain.entity.SamplingSummary
import pe.edu.upc.viora.features.croploadregulation.domain.entity.ThinningEvent
import pe.edu.upc.viora.features.croploadregulation.domain.entity.TreeSample
import pe.edu.upc.viora.features.croploadregulation.domain.repository.ThinningRepository
import pe.edu.upc.viora.features.croploadregulation.infrastructure.local.DraftTreeSampleDao
import pe.edu.upc.viora.features.croploadregulation.infrastructure.local.DraftTreeSampleEntity
import pe.edu.upc.viora.features.croploadregulation.presentation.viewmodel.SamplingSessionViewModel

private class FakeDraftDao : DraftTreeSampleDao {
    val items = mutableListOf<DraftTreeSampleEntity>()

    override suspend fun getSamples(plotId: String, campaignYear: Int): List<DraftTreeSampleEntity> =
        items.filter { it.plotId == plotId && it.campaignYear == campaignYear }

    override suspend fun getAllSamples(): List<DraftTreeSampleEntity> = items

    override fun observeAllSamples(): Flow<List<DraftTreeSampleEntity>> =
        MutableStateFlow(items)

    override fun observePendingDraftSamplesCount(): Flow<Int> =
        MutableStateFlow(items.count { !it.isSynced })

    override suspend fun insertSample(sample: DraftTreeSampleEntity) {
        items.removeAll { it.id == sample.id }
        items.add(sample)
    }

    override suspend fun markSampleAsSynced(sampleId: String) {
        val index = items.indexOfFirst { it.id == sampleId }
        if (index != -1) {
            items[index] = items[index].copy(isSynced = true)
        }
    }

    override suspend fun clearSamples(plotId: String, campaignYear: Int) {
        items.removeAll { it.plotId == plotId && it.campaignYear == campaignYear }
    }
}

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
    val submittedBatches = mutableListOf<List<TreeSample>>()

    override suspend fun getPlotSamplingOverview(campaignYear: Int?): AppResult<List<PlotSamplingOverview>> =
        AppResult.Success(emptyList())

    override suspend fun getSamplingSummary(plotId: String, campaignYear: Int?): AppResult<SamplingSummary> =
        submitResult

    override suspend fun submitSamplingBatch(
        plotId: String,
        campaignYear: Int,
        batchId: String,
        samples: List<TreeSample>,
    ): AppResult<SamplingSummary> {
        submittedBatches.add(samples)
        return submitResult
    }

    override suspend fun getThinningEvents(campaignYear: Int?, plotId: String?): AppResult<List<ThinningEvent>> =
        AppResult.Success(emptyList())

    override fun observeActiveSampling(): Flow<PlotSamplingOverview?> =
        MutableStateFlow(null)

    override fun observePendingDraftSamplesCount(): Flow<Int> =
        MutableStateFlow(0)
}

@OptIn(ExperimentalCoroutinesApi::class)
class SamplingSessionViewModelTest {

    private val fakeDao = FakeDraftDao()
    private val fakeRepo = FakeThinningRepo()
    private val useCase = SubmitSamplingBatchUseCase(fakeRepo)
    private val fakeContext: Context = object : ContextWrapper(null) {}

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initSession syncs unsynced drafts and triggers connection recovered state`() = runTest {
        // Pre-populate Room DB with 2 unsynced drafts
        fakeDao.insertSample(
            DraftTreeSampleEntity(
                id = "sample-1",
                plotId = "plot-1",
                plotName = "Lote Norte",
                campaignYear = 2026,
                treeIdentifier = "A-01",
                shootsCount = 40,
                fruitSetCount = 20,
                isSynced = false,
            ),
        )
        fakeDao.insertSample(
            DraftTreeSampleEntity(
                id = "sample-2",
                plotId = "plot-1",
                plotName = "Lote Norte",
                campaignYear = 2026,
                treeIdentifier = "A-02",
                shootsCount = 38,
                fruitSetCount = 22,
                isSynced = false,
            ),
        )

        val viewModel = SamplingSessionViewModel(useCase, fakeDao, fakeContext)
        viewModel.initSession(plotId = "plot-1", plotName = "Lote Norte", campaignYear = 2026)

        val state = viewModel.uiState.value
        assertEquals(2, state.samples.size)
        assertTrue(state.samples.all { it.isSynced })
        assertTrue(state.hasRecoveredConnection)
        assertEquals(2, state.syncedOnResumeCount)
        assertFalse(state.isOffline)
        assertTrue(fakeDao.items.all { it.isSynced })
    }

    @Test
    fun `initSession sets isOffline when sync fails due to offline error`() = runTest {
        fakeRepo.submitResult = AppResult.Failure(AppError.Offline)
        fakeDao.insertSample(
            DraftTreeSampleEntity(
                id = "sample-1",
                plotId = "plot-1",
                plotName = "Lote Norte",
                campaignYear = 2026,
                treeIdentifier = "A-01",
                shootsCount = 40,
                fruitSetCount = 20,
                isSynced = false,
            ),
        )

        val viewModel = SamplingSessionViewModel(useCase, fakeDao, fakeContext)
        viewModel.initSession(plotId = "plot-1", plotName = "Lote Norte", campaignYear = 2026)

        val state = viewModel.uiState.value
        assertEquals(1, state.samples.size)
        assertFalse(state.samples[0].isSynced)
        assertTrue(state.isOffline)
        assertFalse(state.hasRecoveredConnection)
    }

    @Test
    fun `addSample records sample locally and sets isOffline if network fails`() = runTest {
        fakeRepo.submitResult = AppResult.Failure(AppError.Offline)
        val viewModel = SamplingSessionViewModel(useCase, fakeDao, fakeContext)
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
        assertEquals(1, fakeDao.items.size)
        assertFalse(fakeDao.items[0].isSynced)
    }

    @Test
    fun `addSample records sample and syncs immediately to backend when online`() = runTest {
        val viewModel = SamplingSessionViewModel(useCase, fakeDao, fakeContext)
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
        assertEquals(1, fakeDao.items.size)
        assertTrue(fakeDao.items[0].isSynced)
    }
}
