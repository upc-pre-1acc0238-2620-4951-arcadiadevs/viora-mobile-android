package pe.edu.upc.viora.features.assistant.domain.entity

import java.time.Instant

/**
 * The summary of the day of Inicio (Figma P110-P113): what to do this week across the producer's
 * whole field, written by the server at 06:30 Lima. It has the voice, exactly three cards (the days
 * of the thinning window, the maximum of the week and the open alerts) and what it is based on.
 */
data class DailySummary(
    val id: String,
    val createdAt: Instant,
    val text: String,
    val facts: List<AnswerFact>,
    val sources: List<String>,
) {
    /**
     * The summary as an answer of the sheet ("Ver completo", P113), so the sheet draws it like any
     * other answer; [question] is the title it carries ("¿Qué hago esta semana?").
     */
    fun asAnswer(question: String): AssistantAnswer = AssistantAnswer(
        id = id,
        questionId = QUESTION_ID,
        question = question,
        plotId = null,
        createdAt = createdAt,
        freshness = Freshness.UP_TO_DATE,
        text = text,
        facts = facts,
        sources = sources,
        actions = emptyList(),
    )

    companion object {
        /** The id the server gives the summary among the Inicio questions. */
        const val QUESTION_ID = "home.daily_summary"
    }
}
