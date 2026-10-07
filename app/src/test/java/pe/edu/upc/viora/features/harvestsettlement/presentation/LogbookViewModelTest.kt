package pe.edu.upc.viora.features.harvestsettlement.presentation

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.croploadregulation.application.usecase.GetThinningEventsUseCase
import pe.edu.upc.viora.features.croploadregulation.application.usecase.ObserveThinningEventsUseCase
import pe.edu.upc.viora.features.croploadregulation.domain.entity.PlotSamplingOverview
import pe.edu.upc.viora.features.croploadregulation.domain.entity.SamplingDetailedReport
import pe.edu.upc.viora.features.croploadregulation.domain.entity.SamplingSummary
import pe.edu.upc.viora.features.croploadregulation.domain.entity.ThinningEvent
import pe.edu.upc.viora.features.croploadregulation.domain.entity.TreeSample
import pe.edu.upc.viora.features.croploadregulation.domain.repository.ThinningRepository
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.ThinningEventType
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.ObserveSettledHarvestsUseCase
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.RefreshSettledHarvestsUseCase
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.HarvestSettlement
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettlementStatus
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.StabilizationCurve
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.StabilizationStatus
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.ThinningBalance
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.ThinningStatus
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.PendingSettlement
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettleHarvestDraft
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettleOutcome
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettlementSyncResult
import pe.edu.upc.viora.features.harvestsettlement.domain.repository.HarvestSettlementRepository
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.LogbookFilter
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.LogbookPeriod
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.LogbookUiState
import pe.edu.upc.viora.features.harvestsettlement.presentation.viewmodel.LogbookViewModel
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsLastRefreshUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.RefreshPlotsUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.entity.NewPlot
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.entity.PlotChanges
import pe.edu.upc.viora.features.plotmanagement.domain.repository.PlotRepository
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId

@OptIn(ExperimentalCoroutinesApi::class)
class LogbookViewModelTest {

    // Wednesday 2026-10-14: the week starts on Monday the 12th.
    private val clock = Clock.fixed(Instant.parse("2026-10-14T12:00:00Z"), ZoneOffset.UTC)
    private val settlements = FakeSettlements()
    private val plots = FakePlotRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(timeline: FakeTimeline? = null) = LogbookViewModel(
        observeSettlements = ObserveSettledHarvestsUseCase(settlements),
        observePlots = ObservePlotsUseCase(plots),
        observePlotsLastRefresh = ObservePlotsLastRefreshUseCase(plots),
        refreshPlots = RefreshPlotsUseCase(plots),
        refreshSettlements = RefreshSettledHarvestsUseCase(settlements),
        clock = clock,
        getThinningEvents = timeline?.let { GetThinningEventsUseCase(it) },
        observeThinningEvents = timeline?.let { ObserveThinningEventsUseCase(it) },
    )

    /** The state with an active collector, as the screen has it. */
    private fun LogbookViewModel.content(): LogbookUiState.Content {
        val job = CoroutineScope(Dispatchers.Main).let { scope ->
            uiState.launchIn(scope)
        }
        val state = uiState.value
        job.cancel()
        return state as LogbookUiState.Content
    }

    @Test
    fun `refreshes the cached plot ids on entry`() = runTest {
        plots.cached.value = listOf(plot("p1", "La Yarada 02"), plot("p2", "Lote Norte"))

        viewModel().content()

        assertEquals(listOf(listOf("p1", "p2")), settlements.refreshedIds)
        assertEquals(0, plots.refreshCalls)
    }

    @Test
    fun `downloads the plots first when none is cached`() = runTest {
        plots.afterRefresh = listOf(plot("p1", "La Yarada 02"))

        viewModel().content()

        assertEquals(1, plots.refreshCalls)
        assertEquals(listOf(listOf("p1")), settlements.refreshedIds)
    }

    @Test
    fun `is loading until the first refresh finishes with nothing cached`() = runTest {
        plots.cached.value = listOf(plot("p1", "La Yarada 02"))
        val gate = CompletableDeferred<Unit>()
        settlements.gate = gate
        val vm = viewModel()
        val job = vm.uiState.launchIn(CoroutineScope(Dispatchers.Main))

        assertEquals(LogbookUiState.Loading, vm.uiState.value)

        gate.complete(Unit)
        assertTrue(vm.uiState.value is LogbookUiState.Content)
        job.cancel()
    }

    @Test
    fun `a timeline downloaded before is drawn while the refresh is still running`() = runTest {
        plots.cached.value = listOf(plot("p1", "La Yarada 02"))
        settlements.gate = CompletableDeferred()
        val timeline = FakeTimeline(cached = listOf(samplingEvent("e1", "p1", "2026-10-13T10:00:00Z")))
        val vm = viewModel(timeline)
        val job = vm.uiState.launchIn(CoroutineScope(Dispatchers.Main))

        val state = vm.uiState.value as LogbookUiState.Content
        assertTrue(state.isRefreshing)
        assertEquals(listOf("e1"), state.groups.flatMap { it.entries }.map { it.id })
        assertEquals(1, timeline.downloads)
        job.cancel()
    }

    @Test
    fun `an empty timeline downloaded before is not a loading screen`() = runTest {
        plots.cached.value = listOf(plot("p1", "La Yarada 02"))
        settlements.gate = CompletableDeferred()
        val vm = viewModel(FakeTimeline(cached = emptyList()))
        val job = vm.uiState.launchIn(CoroutineScope(Dispatchers.Main))

        assertTrue((vm.uiState.value as LogbookUiState.Content).isEmpty)
        job.cancel()
    }

    @Test
    fun `empty cache after a successful refresh is an empty content`() = runTest {
        plots.cached.value = listOf(plot("p1", "La Yarada 02"))

        val content = viewModel().content()

        assertTrue(content.isEmpty)
        assertNull(content.refreshError)
    }

    @Test
    fun `groups by week month and earlier, newest first, with plot names`() = runTest {
        plots.cached.value = listOf(plot("p1", "La Yarada 02"))
        settlements.cache.value = listOf(
            settlement("a", "p1", 2023, "2026-03-01T10:00:00Z"),
            settlement("b", "p1", 2025, "2026-10-13T10:00:00Z"),
            settlement("c", "p1", 2024, "2026-10-02T10:00:00Z"),
            settlement("d", "p1", 2022, "2026-10-12T00:30:00Z"),
        )

        val content = viewModel().content()

        assertEquals(
            listOf(LogbookPeriod.THIS_WEEK, LogbookPeriod.THIS_MONTH, LogbookPeriod.EARLIER),
            content.groups.map { it.period },
        )
        assertEquals(listOf("b", "d"), content.groups[0].entries.map { it.id })
        assertEquals(listOf("c"), content.groups[1].entries.map { it.id })
        assertEquals(listOf("a"), content.groups[2].entries.map { it.id })
        assertEquals("La Yarada 02", content.groups[0].entries.first().plotName)
        assertEquals(24_000.0, content.groups[0].entries.first().totalYieldKg, 0.0)
    }

    @Test
    fun `an unknown plot has no name so the screen can fall back`() = runTest {
        plots.cached.value = listOf(plot("p1", "La Yarada 02"))
        settlements.cache.value = listOf(settlement("a", "gone", 2025, "2026-10-13T10:00:00Z", SettlementStatus.AUDITED))

        val entry = viewModel().content().groups.single().entries.single()

        assertNull(entry.plotName)
        assertEquals(SettlementStatus.AUDITED, entry.status)
    }

    @Test
    fun `all and harvests show the settlements, samplings and thinnings are empty`() = runTest {
        plots.cached.value = listOf(plot("p1", "La Yarada 02"))
        settlements.cache.value = listOf(settlement("a", "p1", 2025, "2026-10-13T10:00:00Z"))
        val vm = viewModel()
        val job = vm.uiState.launchIn(CoroutineScope(Dispatchers.Main))

        fun state() = vm.uiState.value as LogbookUiState.Content
        assertEquals(LogbookFilter.ALL, state().filter)
        assertFalse(state().isEmpty)

        vm.selectFilter(LogbookFilter.SAMPLINGS)
        assertTrue(state().isEmpty)
        assertEquals(LogbookFilter.SAMPLINGS, state().filter)

        vm.selectFilter(LogbookFilter.THINNINGS)
        assertTrue(state().isEmpty)

        vm.selectFilter(LogbookFilter.HARVESTS)
        assertFalse(state().isEmpty)
        job.cancel()
    }

    @Test
    fun `a failed refresh keeps the cached list and flags offline`() = runTest {
        plots.cached.value = listOf(plot("p1", "La Yarada 02"))
        settlements.cache.value = listOf(settlement("a", "p1", 2025, "2026-10-13T10:00:00Z"))
        settlements.refreshResult = AppResult.Failure(AppError.Offline)

        val content = viewModel().content()

        assertEquals(1, content.groups.single().entries.size)
        assertEquals(AppError.Offline, content.refreshError)
        assertTrue(content.offline)
    }

    @Test
    fun `a server failure keeps the list but is not offline, and retry recovers`() = runTest {
        plots.cached.value = listOf(plot("p1", "La Yarada 02"))
        settlements.refreshResult = AppResult.Failure(AppError.Server(500))
        val vm = viewModel()
        val job = vm.uiState.launchIn(CoroutineScope(Dispatchers.Main))

        val failed = vm.uiState.value as LogbookUiState.Content
        assertFalse(failed.offline)
        assertTrue(failed.refreshError is AppError.Server)

        settlements.refreshResult = AppResult.Success(Unit)
        vm.refresh()

        assertNull((vm.uiState.value as LogbookUiState.Content).refreshError)
        assertEquals(2, settlements.refreshedIds.size)
        job.cancel()
    }

    @Test
    fun `failing to download the plots is reported and no settlement refresh runs`() = runTest {
        plots.refreshResult = AppResult.Failure(AppError.Timeout)

        val content = viewModel().content()

        assertTrue(content.offline)
        assertTrue(settlements.refreshedIds.isEmpty())
    }

    private fun plot(id: String, name: String) = Plot(
        id = PlotId(id),
        name = name,
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

    private fun settlement(
        id: String,
        plotId: String,
        year: Int,
        settledAt: String,
        status: SettlementStatus = SettlementStatus.SETTLED,
    ) = HarvestSettlement(
        id = id,
        reportId = "r-$id",
        plotId = plotId,
        campaignYear = year,
        greenKg = 10_000.0,
        blackKg = 14_000.0,
        totalYieldKg = 24_000.0,
        commercialFruitsPerKg = null,
        notes = null,
        status = status,
        settledAt = Instant.parse(settledAt),
        thinningBalance = ThinningBalance(ThinningStatus.NOT_RECORDED, null, null, null, null),
        stabilization = StabilizationCurve(StabilizationStatus.INSUFFICIENT_SETTLEMENTS, 0, 0, null, null, null, null, null, null, null, 3),
    )
}

private fun samplingEvent(id: String, plotId: String, occurredAt: String) = ThinningEvent(
    id = id,
    eventType = ThinningEventType.SAMPLING_COMPLETED,
    prescriptionId = "rx-$id",
    plotId = plotId,
    plotName = "La Yarada 02",
    campaignYear = 2026,
    occurredAt = Instant.parse(occurredAt),
    evaluatedTreesCount = 5,
    meanFruitsPerShoot = 0.57,
)

/** The Room cache of the timeline ([cached] null = never downloaded) and its download. */
private class FakeTimeline(cached: List<ThinningEvent>?) : ThinningRepository {
    val events = MutableStateFlow(cached)
    var downloads = 0

    override fun observeThinningEvents(): Flow<List<ThinningEvent>?> = events
    override suspend fun getThinningEvents(campaignYear: Int?, plotId: String?): AppResult<List<ThinningEvent>> {
        downloads++
        return AppResult.Success(events.value.orEmpty())
    }
    override suspend fun getPlotSamplingOverview(campaignYear: Int?): AppResult<List<PlotSamplingOverview>> = error("not used")
    override suspend fun getSamplingSummary(plotId: String, campaignYear: Int?): AppResult<SamplingSummary> = error("not used")
    override suspend fun getSamplingDetailed(plotId: String, campaignYear: Int?): AppResult<SamplingDetailedReport> = error("not used")
    override suspend fun submitSamplingBatch(
        plotId: String,
        campaignYear: Int,
        batchId: String,
        samples: List<TreeSample>,
    ): AppResult<SamplingSummary> = error("not used")
    override fun observeActiveSampling(): Flow<PlotSamplingOverview?> = error("not used")
    override fun observePendingDraftSamplesCount(): Flow<Int> = error("not used")
}

private class FakeSettlements : HarvestSettlementRepository {
    val cache = MutableStateFlow<List<HarvestSettlement>>(emptyList())
    val refreshedIds = mutableListOf<List<String>>()
    var refreshResult: AppResult<Unit> = AppResult.Success(Unit)


    /** When set, [refreshAll] suspends until it completes. */
    var gate: CompletableDeferred<Unit>? = null

    override fun observeAll(): Flow<List<HarvestSettlement>> = cache
    override suspend fun refresh(plotId: String): AppResult<Unit> = refreshResult
    override suspend fun settle(draft: SettleHarvestDraft, idempotencyKey: String?): AppResult<SettleOutcome> =
        error("not used")
    override fun observePending(): Flow<List<PendingSettlement>> = error("not used")
    override suspend fun updatePending(draft: SettleHarvestDraft): AppResult<Unit> = error("not used")
    override suspend fun discardPending(plotId: String, campaignYear: Int): AppResult<Unit> = error("not used")
    override suspend fun syncPending(): SettlementSyncResult = error("not used")
    override suspend fun refreshAll(plotIds: List<String>): AppResult<Unit> {
        refreshedIds += plotIds
        gate?.await()
        return refreshResult
    }
}

private class FakePlotRepository : PlotRepository {
    val cached = MutableStateFlow<List<Plot>>(emptyList())
    var afterRefresh: List<Plot>? = null
    var refreshResult: AppResult<Unit> = AppResult.Success(Unit)
    var refreshCalls = 0

    override fun observePlots(): Flow<List<Plot>> = cached
    override fun observePlot(id: PlotId): Flow<Plot?> = MutableStateFlow(cached.value.firstOrNull { it.id == id })
    override fun observeArchivedPlots(): Flow<List<Plot>> = MutableStateFlow(emptyList())
    override fun observeLastRefresh(): Flow<Instant?> = MutableStateFlow(null)
    override suspend fun refresh(): AppResult<Unit> {
        refreshCalls++
        if (refreshResult is AppResult.Success) afterRefresh?.let { cached.value = it }
        return refreshResult
    }
    override suspend fun refreshArchived(): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun register(newPlot: NewPlot) = AppResult.Failure(AppError.Offline)
    override suspend fun update(id: PlotId, changes: PlotChanges) = AppResult.Failure(AppError.Offline)
    override suspend fun archive(id: PlotId) = AppResult.Failure(AppError.Offline)
    override suspend fun restore(id: PlotId) = AppResult.Failure(AppError.Offline)
}
