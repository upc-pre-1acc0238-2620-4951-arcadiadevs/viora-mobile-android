package pe.edu.upc.viora.features.assistant.domain.entity

import java.time.Instant

/**
 * An answer of the assistant to a closed question about a plot, as the server wrote it: the text of
 * the voice, exactly two cards, what it is based on and the screens it can open. The figures are
 * always the server's; the language model only rephrased the sentences.
 */
data class AssistantAnswer(
    val id: String,
    val questionId: String,
    /** The question as the producer read it; null only for answers saved before the server kept it. */
    val question: String?,
    val plotId: String,
    val createdAt: Instant,
    val freshness: Freshness,
    val text: String,
    val facts: List<AnswerFact>,
    val sources: List<String>,
    val actions: List<AnswerAction>,
) {
    /** The screen the question was asked from; null for a screen this version does not know. */
    val screen: AssistantScreen? get() = AssistantScreen.ofQuestion(questionId)

    val isStale: Boolean get() = freshness == Freshness.STALE
}

/** Whether the plot still has the figures the answer was written from. */
enum class Freshness {
    UP_TO_DATE,

    /** The plot has new data since (a sampling, a harvest): the answer is dimmed and can be asked again. */
    STALE,
}

/** A bento card under the voice: the figure shown big, what it is, and the note in its pill. */
data class AnswerFact(val value: String, val label: String, val pill: String, val tone: FactTone)

/** PRIMARY is the forest green card, HIGHLIGHT the pale yellow one (Figma P117). */
enum class FactTone { PRIMARY, HIGHLIGHT }

/** A button that opens a screen of the app; [target] is a route like `thinning/execute?plotId=…`. */
data class AnswerAction(val label: String, val target: String)
