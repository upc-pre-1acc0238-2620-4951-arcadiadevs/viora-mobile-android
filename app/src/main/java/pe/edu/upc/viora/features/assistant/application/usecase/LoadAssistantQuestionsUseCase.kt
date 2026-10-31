package pe.edu.upc.viora.features.assistant.application.usecase

import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.assistant.domain.entity.AssistantQuestion
import pe.edu.upc.viora.features.assistant.domain.entity.AssistantScreen
import pe.edu.upc.viora.features.assistant.domain.repository.AssistantRepository
import javax.inject.Inject

/** The closed questions of a screen for a plot (Figma P115, P121). */
class LoadAssistantQuestionsUseCase @Inject constructor(
    private val repository: AssistantRepository,
) {
    suspend operator fun invoke(screen: AssistantScreen, plotId: String): AppResult<List<AssistantQuestion>> =
        repository.questions(screen, plotId)
}
