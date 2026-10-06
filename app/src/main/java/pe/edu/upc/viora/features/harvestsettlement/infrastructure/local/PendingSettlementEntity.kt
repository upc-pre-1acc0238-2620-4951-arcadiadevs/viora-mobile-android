package pe.edu.upc.viora.features.harvestsettlement.infrastructure.local

import androidx.room3.ColumnInfo
import androidx.room3.Entity

/**
 * A settlement that has not reached the server yet: the draft fields plus the sync bookkeeping.
 * There is at most one row per plot and campaign (the composite primary key), so editing a pending
 * settlement replaces the row and keeps [idempotencyKey]. [status] is the domain `PendingStatus`
 * name; the `existing_*` columns hold the server's summary when the status is `CONFLICT`.
 * Dates are ISO strings and timestamps are epoch milliseconds.
 */
@Entity(tableName = "pending_settlements", primaryKeys = ["plot_id", "campaign_year"])
data class PendingSettlementEntity(
    @ColumnInfo(name = "plot_id")
    val plotId: String,
    @ColumnInfo(name = "campaign_year")
    val campaignYear: Int,
    @ColumnInfo(name = "green_olives_kg")
    val greenOlivesKg: Double,
    @ColumnInfo(name = "black_olives_kg")
    val blackOlivesKg: Double,
    @ColumnInfo(name = "weighed_on")
    val weighedOn: String,
    @ColumnInfo(name = "mill_ticket_number")
    val millTicketNumber: String?,
    @ColumnInfo(name = "commercial_fruits_per_kg")
    val commercialFruitsPerKg: Double?,
    val notes: String?,
    @ColumnInfo(name = "idempotency_key")
    val idempotencyKey: String,
    val status: String,
    @ColumnInfo(name = "last_error_code")
    val lastErrorCode: String?,
    @ColumnInfo(name = "attempt_count")
    val attemptCount: Int,
    @ColumnInfo(name = "existing_total_yield_kg")
    val existingTotalYieldKg: Double?,
    @ColumnInfo(name = "existing_receipt_number")
    val existingReceiptNumber: String?,
    @ColumnInfo(name = "existing_weighed_on")
    val existingWeighedOn: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
)
