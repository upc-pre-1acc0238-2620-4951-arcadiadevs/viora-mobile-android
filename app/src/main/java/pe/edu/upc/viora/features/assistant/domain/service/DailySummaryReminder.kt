package pe.edu.upc.viora.features.assistant.domain.service

/** Asks the platform to remind the producer of the summary of the day every morning (Inicio, P110). */
interface DailySummaryReminder {

    /** Schedules the morning reminder; calling it again keeps the one already scheduled. */
    fun schedule()
}
