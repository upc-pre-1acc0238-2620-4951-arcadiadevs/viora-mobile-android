package pe.edu.upc.viora.core.database

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CacheMetadataDao {

    @Query("SELECT fetched_at_epoch_ms FROM cache_metadata WHERE resource_key = :resourceKey")
    suspend fun fetchedAt(resourceKey: String): Long?

    @Query("SELECT fetched_at_epoch_ms FROM cache_metadata WHERE resource_key = :resourceKey")
    fun observeFetchedAt(resourceKey: String): Flow<Long?>

    @Upsert
    suspend fun upsert(entity: CacheMetadataEntity)

    @Query("DELETE FROM cache_metadata WHERE resource_key = :resourceKey")
    suspend fun delete(resourceKey: String)
}
