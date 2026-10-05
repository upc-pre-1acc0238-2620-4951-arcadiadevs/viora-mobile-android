package pe.edu.upc.viora.features.harvestsettlement.infrastructure.local

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * Cached copy of a settlement. The thinning balance and the stabilization curve are flattened
 * into columns; enums and dates are stored as the server's strings and parsed in the mapper.
 */
@Entity(tableName = "harvest_settlements")
data class HarvestSettlementEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "report_id")
    val reportId: String,
    @ColumnInfo(name = "plot_id")
    val plotId: String,
    @ColumnInfo(name = "campaign_year")
    val campaignYear: Int,
    @ColumnInfo(name = "green_kg")
    val greenKg: Double,
    @ColumnInfo(name = "black_kg")
    val blackKg: Double,
    @ColumnInfo(name = "total_yield_kg")
    val totalYieldKg: Double,
    @ColumnInfo(name = "commercial_fruits_per_kg")
    val commercialFruitsPerKg: Double?,
    val notes: String?,
    val status: String,
    @ColumnInfo(name = "settled_at")
    val settledAt: String?,
    @ColumnInfo(name = "thinning_status")
    val thinningStatus: String,
    @ColumnInfo(name = "thinning_executed_date")
    val thinningExecutedDate: String?,
    @ColumnInfo(name = "thinning_prescribed_pct")
    val thinningPrescribedPct: Double?,
    @ColumnInfo(name = "thinning_actual_pct")
    val thinningActualPct: Double?,
    @ColumnInfo(name = "thinning_deviation_pp")
    val thinningDeviationPp: Double?,
    @ColumnInfo(name = "stabilization_status")
    val stabilizationStatus: String,
    @ColumnInfo(name = "baseline_campaigns")
    val baselineCampaigns: Int,
    @ColumnInfo(name = "settled_campaigns")
    val settledCampaigns: Int,
    @ColumnInfo(name = "baseline_yield_kg")
    val baselineYieldKg: Double?,
    @ColumnInfo(name = "baseline_alternation_index")
    val baselineAlternationIndex: Double?,
    @ColumnInfo(name = "managed_alternation_index")
    val managedAlternationIndex: Double?,
    @ColumnInfo(name = "amplitude_reduction_rate")
    val amplitudeReductionRate: Double?,
    @ColumnInfo(name = "target_achieved")
    val targetAchieved: Boolean?,
    @ColumnInfo(name = "interannual_variance_kg2")
    val interannualVarianceKg2: Double?,
    @ColumnInfo(name = "coefficient_of_variation")
    val coefficientOfVariation: Double?,
    @ColumnInfo(name = "required_consecutive_pairs")
    val requiredConsecutivePairs: Int,
)
