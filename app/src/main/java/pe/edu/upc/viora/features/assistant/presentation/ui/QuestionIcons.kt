package pe.edu.upc.viora.features.assistant.presentation.ui

import androidx.annotation.DrawableRes
import pe.edu.upc.viora.R
import pe.edu.upc.viora.features.assistant.domain.entity.QuestionIcon

/** The Viora icon of a question (contract: calendar, pruning, book, alert, harvest…). */
@DrawableRes
internal fun QuestionIcon.drawable(): Int = when (this) {
    QuestionIcon.CALENDAR -> R.drawable.ic_calendar_month
    QuestionIcon.PRUNING -> R.drawable.ic_content_cut
    QuestionIcon.BOOK -> R.drawable.ic_menu_book
    QuestionIcon.ALERT -> R.drawable.ic_warning
    QuestionIcon.HARVEST -> R.drawable.ic_inventory
    QuestionIcon.FRUIT -> R.drawable.ic_nutrition
    QuestionIcon.PLOT -> R.drawable.ic_map
    QuestionIcon.TEMP -> R.drawable.ic_thermostat
    QuestionIcon.WATER -> R.drawable.ic_water_drop
    QuestionIcon.SENSOR -> R.drawable.ic_sensors
    QuestionIcon.COUNT -> R.drawable.ic_tag
    QuestionIcon.HISTORY -> R.drawable.ic_menu_book
    QuestionIcon.REGISTER -> R.drawable.ic_edit_note
    QuestionIcon.VIEW -> R.drawable.ic_visibility
    QuestionIcon.PRIORITY -> R.drawable.ic_priority_high
    QuestionIcon.OTHER -> R.drawable.ic_chat_bubble
}
