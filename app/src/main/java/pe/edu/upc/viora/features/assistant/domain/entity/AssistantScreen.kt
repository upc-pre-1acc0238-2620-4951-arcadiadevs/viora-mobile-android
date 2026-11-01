package pe.edu.upc.viora.features.assistant.domain.entity

/**
 * The screen the assistant is opened from. Each one has its own closed set of questions, and a
 * question id starts with the prefix of its screen (`plan.why_percent`, `chill.missing`). The names
 * are the `screen` values of the backend.
 */
enum class AssistantScreen(val questionPrefix: String) {
    /** Alternation of a plot (P40, P114-P119). */
    ALTERNATION("alternation."),

    /** Thinning plan of a plot (P61, P120-P122). */
    PLAN("plan."),

    /** Agroclimatic alert of a plot (T15, P123-P125): its most severe open alert. */
    ALERT("alert."),

    /** Climate of a plot: forecast and sensors (P90, P130-P132). */
    CLIMATE("climate."),

    /** Winter chill of a plot (P80, P133-P135). */
    WINTER_CHILL("chill."),

    /** Detail of a plot (P26, P136-P138). */
    PLOT_DETAIL("plot."),

    /** Nodes of a plot (P85, P139-P141). */
    SENSORS("sensors."),

    /** Summary of a sampling round (P54, P142-P144). */
    SAMPLING_SUMMARY("sampling."),

    /** The thinning just recorded (P63, P145-P147). */
    THINNING_DONE("thinning."),

    /** The campaign just closed (P73, P148-P150). */
    CAMPAIGN_CLOSED("campaign.");

    companion object {
        /** The screen a question belongs to, or null for a screen this version of the app does not know. */
        fun ofQuestion(questionId: String): AssistantScreen? = entries.firstOrNull { questionId.startsWith(it.questionPrefix) }
    }
}
