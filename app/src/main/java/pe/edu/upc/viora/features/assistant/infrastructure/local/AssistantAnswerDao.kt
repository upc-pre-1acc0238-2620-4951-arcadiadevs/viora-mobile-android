package pe.edu.upc.viora.features.assistant.infrastructure.local

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AssistantAnswerDao {

    @Query("SELECT * FROM assistant_answers WHERE created_at_epoch_ms >= :sinceEpochMs ORDER BY created_at_epoch_ms DESC LIMIT :limit")
    fun observeRecent(sinceEpochMs: Long, limit: Int): Flow<List<AssistantAnswerEntity>>

    @Query("SELECT * FROM assistant_answers WHERE id = :id")
    suspend fun findById(id: String): AssistantAnswerEntity?

    /** The newest answer about a plot to a question of a screen (its id starts with [prefix]). */
    @Query(
        "SELECT * FROM assistant_answers WHERE plot_id = :plotId AND question_id LIKE :prefix || '%' " +
            "AND created_at_epoch_ms >= :sinceEpochMs ORDER BY created_at_epoch_ms DESC LIMIT 1",
    )
    suspend fun findLatestOfScreen(plotId: String, prefix: String, sinceEpochMs: Long): AssistantAnswerEntity?

    @Query("SELECT id FROM assistant_answers WHERE moved_on = 1 AND id IN (:ids)")
    suspend fun movedOnAmong(ids: List<String>): List<String>

    @Upsert
    suspend fun upsertAll(entities: List<AssistantAnswerEntity>)

    @Query("UPDATE assistant_answers SET moved_on = 1 WHERE id = :id")
    suspend fun markMovedOn(id: String)

    @Query("DELETE FROM assistant_answers WHERE id NOT IN (:keep)")
    suspend fun deleteAllExcept(keep: List<String>)

    @Query("DELETE FROM assistant_answers WHERE created_at_epoch_ms < :beforeEpochMs")
    suspend fun deleteOlderThan(beforeEpochMs: Long)

    /** The server's history replaces the phone's: what it no longer lists left the 10 days or the 10 answers. */
    @Transaction
    suspend fun replaceAll(entities: List<AssistantAnswerEntity>) {
        deleteAllExcept(entities.map { it.id })
        upsertAll(entities)
    }

    /** Signing out (or another account signing in): the whole table goes, see the context's tables wiper. */
    @Query("DELETE FROM assistant_answers")
    suspend fun wipeAll()
}
