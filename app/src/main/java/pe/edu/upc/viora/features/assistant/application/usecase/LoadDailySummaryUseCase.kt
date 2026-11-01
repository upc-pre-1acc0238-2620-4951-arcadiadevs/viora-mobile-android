package pe.edu.upc.viora.features.assistant.application.usecase

import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.assistant.domain.entity.DailySummary
import pe.edu.upc.viora.features.assistant.domain.repository.AssistantRepository
import javax.inject.Inject

/** The summary of the day of Inicio (P110-P113) and of the morning notification. */
class LoadDailySummaryUseCase @Inject constructor(
    private val repository: AssistantRepository,
) {
    suspend operator fun invoke(): AppResult<DailySummary> = repository.dailySummary()
}
