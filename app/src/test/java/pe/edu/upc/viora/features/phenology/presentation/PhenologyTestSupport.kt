package pe.edu.upc.viora.features.phenology.presentation

import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.phenology.domain.entity.BearingIndex
import pe.edu.upc.viora.features.phenology.domain.entity.BearingYear
import pe.edu.upc.viora.features.phenology.domain.entity.HarvestRecord
import pe.edu.upc.viora.features.phenology.domain.repository.HarvestRecordRepository
import pe.edu.upc.viora.features.plotmanagement.domain.entity.NewPlot
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.entity.PlotChanges
import pe.edu.upc.viora.features.plotmanagement.domain.repository.PlotRepository
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId
import pe.edu.upc.viora.core.domain.AppError

internal fun harvest(
    year: Int,
    kg: Double,
    bearing: BearingYear = BearingYear.UNKNOWN,
    id: String = "r-$year",
    recordedAt: Instant = Instant.parse("2026-01-01T00:00:00Z"),
) = HarvestRecord(
    id = id,
    plotId = "plot-1",
    campaignYear = year,
    totalYieldKg = kg,
    greenKg = null,
    blackKg = null,
    bearing = bearing,
    recordedAt = recordedAt,
)

/** The Figma example: 2022 24 000, 2023 7 750, 2024 22 000, 2025 6 750 kg. */
internal fun figmaRecords(): List<HarvestRecord> = listOf(
    harvest(2022, 24_000.0, BearingYear.ON),
    harvest(2023, 7_750.0, BearingYear.OFF),
    harvest(2024, 22_000.0, BearingYear.ON),
    harvest(2025, 6_750.0, BearingYear.OFF),
)

internal class FakeHarvestRepository(initial: List<HarvestRecord> = emptyList()) : HarvestRecordRepository {
    val records = MutableStateFlow(initial)
    val index = MutableStateFlow<BearingIndex?>(null)
    val lastRefresh = MutableStateFlow<Instant?>(null)
    var refreshResult: AppResult<Unit> = AppResult.Success(Unit)
    var refreshCalls = 0
    var recordResult: ((Int, Double) -> AppResult<HarvestRecord>)? = null
    var rectifyResult: AppResult<HarvestRecord>? = null
    var removeResult: AppResult<Unit> = AppResult.Success(Unit)
    val recorded = mutableListOf<Pair<Int, Double>>()
    val rectified = mutableListOf<Pair<String, Double>>()
    val removed = mutableListOf<String>()

    override fun observeRecords(plotId: String): Flow<List<HarvestRecord>> = records
    override fun observeBearingIndex(plotId: String): Flow<BearingIndex?> = index
    override fun observeLastRefresh(plotId: String): Flow<Instant?> = lastRefresh
    override suspend fun refresh(plotId: String): AppResult<Unit> {
        refreshCalls++
        return refreshResult
    }

    override suspend fun record(plotId: String, campaignYear: Int, totalYieldKg: Double): AppResult<HarvestRecord> {
        recorded += campaignYear to totalYieldKg
        return recordResult?.invoke(campaignYear, totalYieldKg)
            ?: AppResult.Success(harvest(campaignYear, totalYieldKg, id = "new-$campaignYear"))
    }

    override suspend fun rectify(plotId: String, recordId: String, totalYieldKg: Double): AppResult<HarvestRecord> {
        rectified += recordId to totalYieldKg
        return rectifyResult ?: AppResult.Success(records.value.first { it.id == recordId }.copy(totalYieldKg = totalYieldKg))
    }

    override suspend fun remove(plotId: String, recordId: String): AppResult<Unit> {
        removed += recordId
        return removeResult
    }
}

internal class FakePlots(private val plot: Plot? = samplePlot()) : PlotRepository {
    override fun observePlot(id: PlotId): Flow<Plot?> = MutableStateFlow(plot)
    override fun observePlots(): Flow<List<Plot>> = MutableStateFlow(emptyList())
    override fun observeArchivedPlots(): Flow<List<Plot>> = MutableStateFlow(emptyList())
    override fun observeLastRefresh(): Flow<Instant?> = MutableStateFlow(null)
    override suspend fun refresh(): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun refreshArchived(): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun register(newPlot: NewPlot) = AppResult.Failure(AppError.Offline)
    override suspend fun update(id: PlotId, changes: PlotChanges) = AppResult.Failure(AppError.Offline)
    override suspend fun archive(id: PlotId) = AppResult.Failure(AppError.Offline)
    override suspend fun restore(id: PlotId) = AppResult.Failure(AppError.Offline)
}

internal fun samplePlot() = Plot(
    id = PlotId("plot-1"),
    name = "La Yarada 02",
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
