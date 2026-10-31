package pe.edu.upc.viora.features.assistant.infrastructure.local

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

/**
 * An answer of the assistant kept on the phone, so the recent ones and the last one of a screen
 * reopen without signal. The cards, sources and buttons are JSON because they are only read whole.
 * [movedOn] marks that the producer tapped "Otra pregunta": the screen no longer reopens on it.
 */
@Entity(tableName = "assistant_answers", indices = [Index(value = ["plot_id"])])
data class AssistantAnswerEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "question_id")
    val questionId: String,
    @ColumnInfo(name = "question")
    val question: String?,
    @ColumnInfo(name = "plot_id")
    val plotId: String,
    @ColumnInfo(name = "created_at_epoch_ms")
    val createdAtEpochMs: Long,
    @ColumnInfo(name = "freshness")
    val freshness: String,
    @ColumnInfo(name = "text")
    val text: String,
    @ColumnInfo(name = "facts_json")
    val factsJson: String,
    @ColumnInfo(name = "sources_json")
    val sourcesJson: String,
    @ColumnInfo(name = "actions_json")
    val actionsJson: String,
    @ColumnInfo(name = "moved_on")
    val movedOn: Boolean,
)
