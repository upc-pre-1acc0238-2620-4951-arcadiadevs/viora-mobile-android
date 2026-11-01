package pe.edu.upc.viora.features.assistant.application.usecase

import pe.edu.upc.viora.features.assistant.domain.service.DailySummaryReminder
import javax.inject.Inject

/** Keeps the morning reminder of the summary of the day scheduled while the producer uses Inicio. */
class ScheduleDailySummaryReminderUseCase @Inject constructor(
    private val reminder: DailySummaryReminder,
) {
    operator fun invoke() = reminder.schedule()
}
