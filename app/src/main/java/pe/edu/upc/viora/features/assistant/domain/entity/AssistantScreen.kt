package pe.edu.upc.viora.features.assistant.domain.entity

/**
 * The screen the assistant is opened from. Each one has its own closed set of questions, and a
 * question id starts with the name of its screen (`plan.why_percent`).
 */
enum class AssistantScreen(val questionPrefix: String) {
    /** Alternation of a plot (P40, P114-P119). */
    ALTERNATION("alternation."),

    /** Thinning plan of a plot (P61, P120-P122). */
    PLAN("plan.");

    companion object {
        /** The screen a question belongs to, or null for a screen this version of the app does not know. */
        fun ofQuestion(questionId: String): AssistantScreen? = entries.firstOrNull { questionId.startsWith(it.questionPrefix) }
    }
}
