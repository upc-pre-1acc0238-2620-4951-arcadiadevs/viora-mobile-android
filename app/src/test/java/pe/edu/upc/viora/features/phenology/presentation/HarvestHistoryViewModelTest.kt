package pe.edu.upc.viora.features.phenology.presentation

import androidx.lifecycle.SavedStateHandle
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.phenology.application.usecase.ObserveBearingIndexUseCase
import pe.edu.upc.viora.features.phenology.application.usecase.ObserveHarvestHistoryUseCase
import pe.edu.upc.viora.features.phenology.application.usecase.ObserveHarvestLastRefreshUseCase
import pe.edu.upc.viora.features.phenology.application.usecase.RefreshHarvestHistoryUseCase
import pe.edu.upc.viora.features.phenology.domain.entity.BbiClass
import pe.edu.upc.viora.features.phenology.domain.entity.BearingIndex
import pe.edu.upc.viora.features.phenology.presentation.state.CampaignChangeKind
import pe.edu.upc.viora.features.phenology.presentation.state.HarvestHistoryUiState
import pe.edu.upc.viora.features.phenology.presentation.viewmodel.HarvestHistoryViewModel
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class HarvestHistoryViewModelTest {

    private val repository = FakeHarvestRepository()
    private val clock = Clock.fixed(Instant.parse("2026-10-04T12:00:00Z"), ZoneOffset.UTC)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = HarvestHistoryViewModel(
        savedStateHandle = SavedStateHandle(mapOf("plotId" to "plot-1", "plotName" to "")),
        observeRecords = ObserveHarvestHistoryUseCase(repository),
        observeIndex = ObserveBearingIndexUseCase(repository),
        observeLastRefresh = ObserveHarvestLastRefreshUseCase(repository),
        observePlot = ObservePlotUseCase(FakePlots()),
        refreshHistory = RefreshHarvestHistoryUseCase(repository),
        clock = clock,
    )

    private fun HarvestHistoryViewModel.content(): HarvestHistoryUiState.Content {
        val seen = mutableListOf<HarvestHistoryUiState>()
        val job = kotlinx.coroutines.CoroutineScope(Dispatchers.Main).let { scope ->
            uiState.onEach { seen += it }.launchIn(scope)
        }
        job.cancel()
        return seen.last() as HarvestHistoryUiState.Content
    }

    @Test
    fun `starts loading and refreshes once on start`() = runTest {
        repository.refreshResult = AppResult.Success(Unit)
        val vm = viewModel()

        assertEquals(1, repository.refreshCalls)
        assertEquals(HarvestHistoryUiState.Loading, vm.uiState.value)
    }

    @Test
    fun `shows the server index, its class and the interval chips for the cached records`() = runTest {
        repository.records.value = figmaRecords()
        repository.index.value = BearingIndex(0.51, 4, null)

        val content = viewModel().content()

        assertEquals(0.51, content.index!!, 0.0)
        assertEquals(BbiClass.SEVERE, content.bbiClass)
        assertEquals(listOf(0.51, 0.48, 0.53), content.intervals.map { it.value })
        assertEquals(listOf(2025, 2024, 2023, 2022), content.records.map { it.campaignYear })
        assertEquals("La Yarada 02", content.plotName)
        assertEquals(2.5, content.plotSubtitle!!.areaHectares, 0.0)
        assertEquals(2026, content.summary.nextYear)
        assertEquals(2021, content.suggestedYear)
        assertTrue(content.canEdit)
    }

    @Test
    fun `the class comes from the Figma thresholds not from the backend`() = runTest {
        repository.records.value = figmaRecords()
        repository.index.value = BearingIndex(0.45, 4, null)

        assertEquals(BbiClass.SEVERE, viewModel().content().bbiClass)

        repository.index.value = BearingIndex(0.30, 4, null)
        assertEquals(BbiClass.MODERATE, viewModel().content().bbiClass)
    }

    @Test
    fun `fewer than three campaigns show the insufficient variant without an index`() = runTest {
        repository.records.value = figmaRecords().takeLast(2)
        repository.index.value = BearingIndex(0.51, 2, null)

        val content = viewModel().content()

        assertNull(content.index)
        assertFalse(content.hasIndex)
        assertTrue(content.intervals.isEmpty())
        assertEquals(1, content.summary.missing)
        assertEquals(2023, content.suggestedYear)
    }

    @Test
    fun `no campaigns after a successful refresh is an empty content not an error`() = runTest {
        repository.lastRefresh.value = Instant.parse("2026-10-01T00:00:00Z")

        val content = viewModel().content()

        assertTrue(content.records.isEmpty())
        assertEquals(3, content.summary.missing)
        assertEquals(2025, content.suggestedYear)
    }

    @Test
    fun `a missing server index falls back to the local formula`() = runTest {
        repository.records.value = figmaRecords()

        assertEquals(0.51, viewModel().content().index!!, 0.0)
    }

    @Test
    fun `offline refresh with a cache keeps the data and blocks editing`() = runTest {
        repository.records.value = figmaRecords()
        repository.lastRefresh.value = Instant.parse("2026-09-20T10:00:00Z")
        repository.refreshResult = AppResult.Failure(AppError.Offline)

        val content = viewModel().content()

        assertTrue(content.offline)
        assertFalse(content.canEdit)
        assertEquals(Instant.parse("2026-09-20T10:00:00Z"), content.lastRefresh)
        assertEquals(4, content.records.size)
    }

    @Test
    fun `a server error with a cache is not reported as offline`() = runTest {
        repository.records.value = figmaRecords()
        repository.lastRefresh.value = Instant.parse("2026-09-20T10:00:00Z")
        repository.refreshResult = AppResult.Failure(AppError.Server(500))

        assertFalse(viewModel().content().offline)
    }

    @Test
    fun `failed refresh with nothing cached is an error and retry recovers`() = runTest {
        repository.refreshResult = AppResult.Failure(AppError.Offline)
        val vm = viewModel()
        val states = mutableListOf<HarvestHistoryUiState>()
        val job = vm.uiState.onEach { states += it }.launchIn(kotlinx.coroutines.CoroutineScope(Dispatchers.Main))

        assertEquals(HarvestHistoryUiState.Error(AppError.Offline), states.last())

        repository.refreshResult = AppResult.Success(Unit)
        repository.lastRefresh.value = Instant.parse("2026-10-04T00:00:00Z")
        vm.refresh()

        assertTrue(states.last() is HarvestHistoryUiState.Content)
        assertEquals(2, repository.refreshCalls)
        job.cancel()
    }

    @Test
    fun `a campaign change shows a notice and marks only an added row as new`() = runTest {
        repository.records.value = figmaRecords()
        val vm = viewModel()
        val states = mutableListOf<HarvestHistoryUiState>()
        val job = vm.uiState.onEach { states += it }.launchIn(kotlinx.coroutines.CoroutineScope(Dispatchers.Main))

        vm.onCampaignChanged(CampaignChangeKind.ADDED, 2021, "new-2021")
        var content = states.last() as HarvestHistoryUiState.Content
        assertEquals(CampaignChangeKind.ADDED, content.notice!!.kind)
        assertEquals(2021, content.notice!!.year)
        assertEquals("new-2021", content.newRecordId)

        vm.onCampaignChanged(CampaignChangeKind.CORRECTED, 2024, "r-2024")
        content = states.last() as HarvestHistoryUiState.Content
        assertNull(content.newRecordId)

        vm.dismissNotice()
        content = states.last() as HarvestHistoryUiState.Content
        assertNull(content.notice)
        assertNotNull(content)
        job.cancel()
    }
}
