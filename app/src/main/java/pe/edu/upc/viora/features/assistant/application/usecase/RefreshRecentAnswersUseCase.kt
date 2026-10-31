package pe.edu.upc.viora.features.assistant.application.usecase

import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.assistant.domain.repository.AssistantRepository
import javax.inject.Inject

/** Brings the history and whether each answer is still current from the server. */
class RefreshRecentAnswersUseCase @Inject constructor(
    private val repository: AssistantRepository,
) {
    suspend operator fun invoke(): AppResult<Unit> = repository.refreshRecent()
}
