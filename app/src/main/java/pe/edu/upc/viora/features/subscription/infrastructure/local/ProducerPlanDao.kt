package pe.edu.upc.viora.features.subscription.infrastructure.local

import androidx.room3.Dao
import androidx.room3.Embedded
import androidx.room3.Query
import androidx.room3.Relation
import androidx.room3.Transaction
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.core.database.CacheMetadataEntity

const val PRODUCER_PLAN_CATALOG_KEY = "subscription:producer-plans"

/** One transaction reads the metadata and its offers, including a successfully empty catalog. */
data class CachedProducerPlanCatalog(
    @Embedded val metadata: CacheMetadataEntity,
    @Relation(parentColumns = ["resource_key"], entityColumns = ["catalog_key"]) val offers: List<ProducerPlanEntity>,
)

@Dao
interface ProducerPlanDao {
    @Transaction
    @Query("SELECT * FROM cache_metadata WHERE resource_key = :key")
    fun observeCatalog(key: String): Flow<CachedProducerPlanCatalog?>

    @Query("DELETE FROM producer_plan_offers WHERE catalog_key = :key")
    suspend fun deleteOffers(key: String)

    @Upsert
    suspend fun upsertOffers(offers: List<ProducerPlanEntity>)

    @Query("INSERT OR REPLACE INTO cache_metadata (resource_key, fetched_at_epoch_ms) VALUES (:key, :fetchedAt)")
    suspend fun stamp(key: String, fetchedAt: Long)

    /** A failed replacement cannot update just the timestamp or leave only part of a catalog. */
    @Transaction
    suspend fun replaceCatalog(offers: List<ProducerPlanEntity>, fetchedAt: Long) {
        deleteOffers(PRODUCER_PLAN_CATALOG_KEY)
        upsertOffers(offers)
        stamp(PRODUCER_PLAN_CATALOG_KEY, fetchedAt)
    }
}
