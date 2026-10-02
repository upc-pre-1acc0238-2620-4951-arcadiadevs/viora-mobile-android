package pe.edu.upc.viora.core.database

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * When a cached resource was last refreshed from the server. The UI shows this as
 * "actualizado hace ..." and offline screens must never claim the data is current.
 *
 * `resourceKey` is free-form but should be stable and scoped, e.g. `plots`, `telemetry:{plotId}`.
 */
@Entity(tableName = "cache_metadata")
data class CacheMetadataEntity(
    @PrimaryKey
    @ColumnInfo(name = "resource_key")
    val resourceKey: String,
    @ColumnInfo(name = "fetched_at_epoch_ms")
    val fetchedAtEpochMs: Long,
)
