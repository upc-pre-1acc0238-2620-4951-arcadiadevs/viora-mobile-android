package pe.edu.upc.viora.features.assistant.application.usecase

import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.assistant.domain.entity.AssistantAnswer
import pe.edu.upc.viora.features.assistant.domain.entity.AssistantScreen
import pe.edu.upc.viora.features.assistant.domain.repository.AssistantRepository
import javax.inject.Inject

/** The answer the entry of a screen reopens on until "Otra pregunta" (P114v, P117). */
class FindAnswerToReopenUseCase @Inject constructor(
    private val repository: AssistantRepository,
) {
    suspend operator fun invoke(screen: AssistantScreen, plotId: String): AppResult<AssistantAnswer?> =
        repository.answerToReopen(screen, plotId)
}
