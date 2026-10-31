package pe.edu.upc.viora.features.assistant.application.usecase

import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.assistant.domain.entity.AssistantAnswer
import pe.edu.upc.viora.features.assistant.domain.repository.AssistantRepository
import javax.inject.Inject

/** Reopens an answer of the history (P118). */
class OpenAnswerUseCase @Inject constructor(
    private val repository: AssistantRepository,
) {
    suspend operator fun invoke(answerId: String): AppResult<AssistantAnswer> = repository.answer(answerId)
}
