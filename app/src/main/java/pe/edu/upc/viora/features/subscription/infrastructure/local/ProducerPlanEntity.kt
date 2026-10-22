package pe.edu.upc.viora.features.subscription.infrastructure.local

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

/** Decimal values are stored as text so SQLite never rounds the commercial policy through REAL. */
@Entity(tableName = "producer_plan_offers")
data class ProducerPlanEntity(
    @PrimaryKey val code: String,
    @ColumnInfo(name = "catalog_key") val catalogKey: String,
    val currency: String,
    @ColumnInfo(name = "monthly_rate_per_ha") val monthlyRatePerHa: String,
    @ColumnInfo(name = "tax_rate") val taxRate: String,
    @ColumnInfo(name = "minimum_quota_ha") val minimumQuotaHa: String,
    @ColumnInfo(name = "quota_increment_ha") val quotaIncrementHa: String,
    @ColumnInfo(name = "billing_months") val billingMonths: Int,
    @ColumnInfo(name = "tariff_version") val tariffVersion: String,
)
