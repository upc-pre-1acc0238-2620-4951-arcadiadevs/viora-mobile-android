package pe.edu.upc.viora.features.assistant.application.usecase

import pe.edu.upc.viora.features.assistant.domain.repository.AssistantRepository
import javax.inject.Inject

/** "Otra pregunta": the screen no longer reopens on the answer, which stays in the history. */
class MoveOnFromAnswerUseCase @Inject constructor(
    private val repository: AssistantRepository,
) {
    suspend operator fun invoke(answerId: String) = repository.moveOn(answerId)
}
