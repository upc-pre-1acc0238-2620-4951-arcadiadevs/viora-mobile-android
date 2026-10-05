package pe.edu.upc.viora.features.croploadregulation.infrastructure.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query

@Dao
interface DraftTreeSampleDao {

    @Query("SELECT * FROM draft_tree_samples WHERE plot_id = :plotId AND campaign_year = :campaignYear ORDER BY created_at ASC")
    suspend fun getSamples(plotId: String, campaignYear: Int): List<DraftTreeSampleEntity>

    @Query("SELECT * FROM draft_tree_samples ORDER BY created_at ASC")
    suspend fun getAllSamples(): List<DraftTreeSampleEntity>

    @Query("SELECT * FROM draft_tree_samples ORDER BY created_at ASC")
    fun observeAllSamples(): kotlinx.coroutines.flow.Flow<List<DraftTreeSampleEntity>>

    @Query("SELECT COUNT(*) FROM draft_tree_samples WHERE is_synced = 0")
    fun observePendingDraftSamplesCount(): kotlinx.coroutines.flow.Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSample(sample: DraftTreeSampleEntity)

    @Query("UPDATE draft_tree_samples SET is_synced = 1 WHERE id = :sampleId")
    suspend fun markSampleAsSynced(sampleId: String)

    @Query("DELETE FROM draft_tree_samples WHERE plot_id = :plotId AND campaign_year = :campaignYear")
    suspend fun clearSamples(plotId: String, campaignYear: Int)
}
