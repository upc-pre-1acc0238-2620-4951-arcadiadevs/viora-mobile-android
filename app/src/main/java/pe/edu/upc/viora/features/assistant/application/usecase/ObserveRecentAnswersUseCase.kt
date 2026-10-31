package pe.edu.upc.viora.features.assistant.application.usecase

import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.features.assistant.domain.entity.AssistantAnswer
import pe.edu.upc.viora.features.assistant.domain.repository.AssistantRepository
import javax.inject.Inject

/** The recent answers kept on the phone, newest first (P118, P128). */
class ObserveRecentAnswersUseCase @Inject constructor(
    private val repository: AssistantRepository,
) {
    operator fun invoke(): Flow<List<AssistantAnswer>> = repository.observeRecent()
}
