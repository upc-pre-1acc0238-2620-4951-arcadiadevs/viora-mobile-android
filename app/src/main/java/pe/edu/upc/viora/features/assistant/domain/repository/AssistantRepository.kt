package pe.edu.upc.viora.features.assistant.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.domain.IdempotencyKey
import pe.edu.upc.viora.features.assistant.domain.entity.AssistantAnswer
import pe.edu.upc.viora.features.assistant.domain.entity.AssistantQuestion
import pe.edu.upc.viora.features.assistant.domain.entity.AssistantScreen

/**
 * The questions and answers of the assistant. Asking needs the server; the answers it gives are
 * kept on the phone (10 for 10 days), so the recent ones and the last one of a screen can be
 * reopened without signal.
 */
interface AssistantRepository {

    /** The questions of a screen for a plot, in display order. */
    suspend fun questions(screen: AssistantScreen, plotId: String): AppResult<List<AssistantQuestion>>

    /**
     * Asks a question. The same [requestKey] always gets the same answer, so a retry after a lost
     * response never writes a second one; [forceRefresh] ignores an answer saved for the same data.
     */
    suspend fun ask(questionId: String, plotId: String, requestKey: IdempotencyKey, forceRefresh: Boolean): AppResult<AssistantAnswer>

    /**
     * The answer a screen reopens on: the last one given on it about the plot, unless the producer
     * already moved on with "Otra pregunta". Null when there is none to reopen. Without signal it
     * comes from the phone.
     */
    suspend fun answerToReopen(screen: AssistantScreen, plotId: String): AppResult<AssistantAnswer?>

    /** One answer of the history, with its freshness from the server when there is signal. */
    suspend fun answer(answerId: String): AppResult<AssistantAnswer>

    /** The answers kept on the phone, newest first, at most 10 from the last 10 days. */
    fun observeRecent(): Flow<List<AssistantAnswer>>

    /** Brings the history and its freshness from the server. */
    suspend fun refreshRecent(): AppResult<Unit>

    /** "Otra pregunta": the screen stops reopening on this answer; it stays in the history. */
    suspend fun moveOn(answerId: String)
}
