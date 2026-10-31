package pe.edu.upc.viora.features.assistant.application.usecase

import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.domain.IdempotencyKey
import pe.edu.upc.viora.features.assistant.domain.entity.AssistantAnswer
import pe.edu.upc.viora.features.assistant.domain.repository.AssistantRepository
import javax.inject.Inject

/**
 * Asks a closed question. A retry of the same attempt must pass the same [IdempotencyKey], so the
 * server answers it once; "Volver a preguntar con datos nuevos" passes forceRefresh (P119).
 */
class AskAssistantUseCase @Inject constructor(
    private val repository: AssistantRepository,
) {
    suspend operator fun invoke(
        questionId: String,
        plotId: String,
        requestKey: IdempotencyKey,
        forceRefresh: Boolean = false,
    ): AppResult<AssistantAnswer> = repository.ask(questionId, plotId, requestKey, forceRefresh)
}
