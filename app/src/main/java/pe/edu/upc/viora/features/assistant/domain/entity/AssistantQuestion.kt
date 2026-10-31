package pe.edu.upc.viora.features.assistant.domain.entity

/**
 * A closed question the producer can tap. The server writes the [label] with the real figures of
 * the plot, in the language of the app; the app never composes it. A question without enough data
 * comes disabled with the [disabledReason] the row shows.
 */
data class AssistantQuestion(
    val id: String,
    val label: String,
    val icon: QuestionIcon,
    val enabled: Boolean,
    val disabledReason: String?,
)

/** The icon key of a question in the Viora icon catalog; an unknown key falls back to [OTHER]. */
enum class QuestionIcon {
    CALENDAR, PRUNING, BOOK, ALERT, HARVEST, FRUIT, PLOT, TEMP, WATER, SENSOR, COUNT, HISTORY, REGISTER, VIEW, PRIORITY, OTHER;

    companion object {
        fun fromKey(key: String?): QuestionIcon = when (key?.lowercase()) {
            "calendar" -> CALENDAR
            "pruning" -> PRUNING
            "book" -> BOOK
            "alert" -> ALERT
            "harvest" -> HARVEST
            "fruit" -> FRUIT
            "lote" -> PLOT
            "temp" -> TEMP
            "water" -> WATER
            "sensor" -> SENSOR
            "count" -> COUNT
            "history" -> HISTORY
            "reg" -> REGISTER
            "view" -> VIEW
            "prio" -> PRIORITY
            else -> OTHER
        }
    }
}
