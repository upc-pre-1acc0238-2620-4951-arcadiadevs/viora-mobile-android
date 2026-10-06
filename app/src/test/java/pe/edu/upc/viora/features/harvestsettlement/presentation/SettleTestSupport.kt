package pe.edu.upc.viora.features.harvestsettlement.presentation

import java.time.Instant
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.HarvestSettlement
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.PendingSettlement
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.PendingStatus
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettleHarvestDraft
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettleOutcome
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettlementStatus
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettlementSyncResult
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.StabilizationCurve
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.StabilizationStatus
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.ThinningBalance
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.ThinningStatus
import pe.edu.upc.viora.features.harvestsettlement.domain.repository.HarvestSettlementRepository
import pe.edu.upc.viora.features.plotmanagement.domain.entity.NewPlot
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.entity.PlotChanges
import pe.edu.upc.viora.features.plotmanagement.domain.repository.PlotRepository
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId
import pe.edu.upc.viora.core.domain.AppError

internal fun testPlot(id: String, name: String, hectares: Double = 2.5) = Plot(
    id = PlotId(id),
    name = name,
    variety = OliveVariety.SEVILLANA,
    areaHectares = hectares,
    treesPerHectare = 72,
    rowSpacingMeters = 7.0,
    treeSpacingMeters = 5.0,
    outline = emptyList(),
    lastPruningDate = null,
    isActive = true,
    revision = 1,
)

internal fun testSettlement(plotId: String, year: Int, kg: Double = 20_800.0) = HarvestSettlement(
    id = "s-$plotId-$year",
    reportId = "r-$plotId-$year",
    plotId = plotId,
    campaignYear = year,
    greenKg = kg * 0.6,
    blackKg = kg * 0.4,
    totalYieldKg = kg,
    commercialFruitsPerKg = null,
    notes = null,
    status = SettlementStatus.SETTLED,
    settledAt = Instant.parse("2026-05-12T10:00:00Z"),
    thinningBalance = ThinningBalance(ThinningStatus.NOT_RECORDED, null, null, null, null),
    stabilization = StabilizationCurve(StabilizationStatus.INSUFFICIENT_BASELINE, 0, 0, null, null, null, null, null, null, null, 0),
)

internal fun testPending(draft: SettleHarvestDraft, status: PendingStatus) = PendingSettlement(
    draft = draft,
    idempotencyKey = "key-${draft.plotId}",
    status = status,
    lastErrorCode = null,
    existing = null,
    createdAt = 0,
    updatedAt = 0,
)

internal fun testDraft(plotId: String, year: Int) = SettleHarvestDraft(
    plotId = plotId,
    campaignYear = year,
    greenOlivesKg = 12_500.0,
    blackOlivesKg = 8_300.0,
    weighedOn = java.time.LocalDate.of(year, 5, 14),
)

/** Repository fake for the settle presentation tests: only what the settle flow touches. */
internal class FakeSettleRepository : HarvestSettlementRepository {
    val cache = MutableStateFlow<List<HarvestSettlement>>(emptyList())
    val pending = MutableStateFlow<List<PendingSettlement>>(emptyList())
    var settleResult: (SettleHarvestDraft) -> AppResult<SettleOutcome> = { AppResult.Success(SettleOutcome.Settled(testSettlement(it.plotId, it.campaignYear))) }
    var refreshResult: AppResult<Unit> = AppResult.Success(Unit)
    /** When set, `settle` suspends until it completes: lets a test act while the call is in flight. */
    var gate: CompletableDeferred<Unit>? = null
    val settled = mutableListOf<Pair<SettleHarvestDraft, String?>>()
    val refreshedIds = mutableListOf<List<String>>()

    override fun observeAll(): Flow<List<HarvestSettlement>> = cache
    override suspend fun refresh(plotId: String): AppResult<Unit> = refreshResult
    override suspend fun refreshAll(plotIds: List<String>): AppResult<Unit> {
        refreshedIds += plotIds
        return refreshResult
    }

    override suspend fun settle(draft: SettleHarvestDraft, idempotencyKey: String?): AppResult<SettleOutcome> {
        settled += draft to idempotencyKey
        gate?.await()
        return settleResult(draft)
    }

    override fun observePending(): Flow<List<PendingSettlement>> = pending
    override suspend fun updatePending(draft: SettleHarvestDraft): AppResult<Unit> = error("not used")
    override suspend fun discardPending(plotId: String, campaignYear: Int): AppResult<Unit> = error("not used")
    override suspend fun syncPending(): SettlementSyncResult = error("not used")
}

internal class FakePlotsForSettle : PlotRepository {
    val cached = MutableStateFlow<List<Plot>>(emptyList())

    override fun observePlots(): Flow<List<Plot>> = cached
    override fun observePlot(id: PlotId): Flow<Plot?> = MutableStateFlow(cached.value.firstOrNull { it.id == id })
    override fun observeArchivedPlots(): Flow<List<Plot>> = MutableStateFlow(emptyList())
    override fun observeLastRefresh(): Flow<Instant?> = MutableStateFlow(null)
    override suspend fun refresh(): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun refreshArchived(): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun register(newPlot: NewPlot) = AppResult.Failure(AppError.Offline)
    override suspend fun update(id: PlotId, changes: PlotChanges) = AppResult.Failure(AppError.Offline)
    override suspend fun archive(id: PlotId) = AppResult.Failure(AppError.Offline)
    override suspend fun restore(id: PlotId) = AppResult.Failure(AppError.Offline)
}
