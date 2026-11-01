package pe.edu.upc.viora.features.assistant.infrastructure.local

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert

@Dao
interface DailySummaryDao {

    @Query("SELECT * FROM assistant_daily_summaries WHERE language = :language")
    suspend fun find(language: String): DailySummaryEntity?

    @Upsert
    suspend fun upsert(entity: DailySummaryEntity)

    /** Signing out (or another account signing in): see the context's tables wiper. */
    @Query("DELETE FROM assistant_daily_summaries")
    suspend fun wipeAll()
}
