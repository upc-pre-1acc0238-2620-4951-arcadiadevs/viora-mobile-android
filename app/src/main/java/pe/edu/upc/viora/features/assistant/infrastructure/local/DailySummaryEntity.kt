package pe.edu.upc.viora.features.assistant.infrastructure.local

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * The summary of the day kept on the phone, one per language, so Inicio and the morning notification
 * still have it without signal. [day] is the producer's day it was written on: a summary of another
 * day is never shown as today's.
 */
@Entity(tableName = "assistant_daily_summaries")
data class DailySummaryEntity(
    @PrimaryKey
    @ColumnInfo(name = "language")
    val language: String,
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "day")
    val day: String,
    @ColumnInfo(name = "created_at_epoch_ms")
    val createdAtEpochMs: Long,
    @ColumnInfo(name = "text")
    val text: String,
    @ColumnInfo(name = "facts_json")
    val factsJson: String,
    @ColumnInfo(name = "sources_json")
    val sourcesJson: String,
)
