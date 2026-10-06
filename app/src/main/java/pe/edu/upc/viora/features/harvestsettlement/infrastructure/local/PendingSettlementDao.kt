package pe.edu.upc.viora.features.harvestsettlement.infrastructure.local

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingSettlementDao {

    @Query("SELECT * FROM pending_settlements ORDER BY created_at")
    fun observeAll(): Flow<List<PendingSettlementEntity>>

    @Query("SELECT * FROM pending_settlements WHERE status = :status ORDER BY created_at")
    suspend fun findByStatus(status: String): List<PendingSettlementEntity>

    @Query("SELECT * FROM pending_settlements WHERE plot_id = :plotId AND campaign_year = :campaignYear LIMIT 1")
    suspend fun find(plotId: String, campaignYear: Int): PendingSettlementEntity?

    @Upsert
    suspend fun upsert(entity: PendingSettlementEntity)

    @Query("DELETE FROM pending_settlements WHERE plot_id = :plotId AND campaign_year = :campaignYear")
    suspend fun delete(plotId: String, campaignYear: Int)
}
