package pe.edu.upc.viora.features.assistant.infrastructure.remote

import kotlinx.serialization.Serializable

/** `GET /assistant/questions` (QuestionCatalogResource). */
@Serializable
data class QuestionCatalogDto(
    val screen: String,
    val plotId: String? = null,
    val questions: List<QuestionDto> = emptyList(),
)

@Serializable
data class QuestionDto(
    val questionId: String,
    val label: String,
    val icon: String? = null,
    val enabled: Boolean,
    val disabledReason: String? = null,
)

/** Body of `POST /assistant/answers`. */
@Serializable
data class AskQuestionRequestDto(
    val questionId: String,
    val plotId: String,
    val clientRequestId: String,
    val forceRefresh: Boolean,
)

/** An answer (AssistantAnswerResource). */
@Serializable
data class AssistantAnswerDto(
    val answerId: String,
    val questionId: String,
    val question: String? = null,
    val plotId: String,
    val createdAt: String,
    val freshness: String,
    val text: String,
    val facts: List<AnswerFactDto> = emptyList(),
    val sources: List<AnswerSourceDto> = emptyList(),
    val actions: List<AnswerActionDto> = emptyList(),
)

@Serializable
data class AnswerFactDto(val value: String, val label: String, val pill: String, val tone: String)

@Serializable
data class AnswerSourceDto(val label: String)

@Serializable
data class AnswerActionDto(val type: String, val label: String, val target: String)

/** `GET /assistant/answers`. */
@Serializable
data class AnswerHistoryDto(val answers: List<AssistantAnswerDto> = emptyList())
