package pe.edu.upc.viora.features.assistant

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.domain.IdempotencyKey
import pe.edu.upc.viora.features.assistant.domain.entity.AnswerFact
import pe.edu.upc.viora.features.assistant.domain.entity.AssistantAnswer
import pe.edu.upc.viora.features.assistant.domain.entity.AssistantQuestion
import pe.edu.upc.viora.features.assistant.domain.entity.AssistantScreen
import pe.edu.upc.viora.features.assistant.domain.entity.FactTone
import pe.edu.upc.viora.features.assistant.domain.entity.Freshness
import pe.edu.upc.viora.features.assistant.domain.entity.QuestionIcon
import pe.edu.upc.viora.features.assistant.domain.repository.AssistantRepository
import java.time.Instant

const val PLOT_ID = "550e8400-e29b-41d4-a716-446655440001"

fun question(id: String = "alternation.why_index", label: String = "¿Por qué mi índice es 0,51?", enabled: Boolean = true) =
    AssistantQuestion(id = id, label = label, icon = QuestionIcon.CALENDAR, enabled = enabled, disabledReason = if (enabled) null else "Faltan datos")

fun answer(
    id: String = "0b6a5a3e-5c4f-4b8e-9a35-4b7e3e5f1a10",
    questionId: String = "alternation.why_index",
    question: String? = "¿Por qué mi índice es 0,51?",
    plotId: String = PLOT_ID,
    createdAt: Instant = Instant.parse("2026-10-30T15:00:00Z"),
    freshness: Freshness = Freshness.UP_TO_DATE,
) = AssistantAnswer(
    id = id,
    questionId = questionId,
    question = question,
    plotId = plotId,
    createdAt = createdAt,
    freshness = freshness,
    text = "Tu índice de vecería es 0,51: vecería severa.",
    facts = listOf(
        AnswerFact("0,51", "índice de vecería", "vecería severa", FactTone.PRIMARY),
        AnswerFact("10,0 → 4,0", "t/ha · tu mejor y tu menor campaña", "2024 y 2023", FactTone.HIGHLIGHT),
    ),
    sources = listOf("Basado en tus 4 campañas registradas"),
    actions = emptyList(),
)

/** One ask the fake received. */
data class AskCall(val questionId: String, val plotId: String, val key: IdempotencyKey, val forceRefresh: Boolean)

class FakeAssistantRepository : AssistantRepository {
    var questions: AppResult<List<AssistantQuestion>> = AppResult.Success(listOf(question()))
    var askResult: AppResult<AssistantAnswer> = AppResult.Success(answer())
    var reopen: AppResult<AssistantAnswer?> = AppResult.Success(null)
    var opened: AppResult<AssistantAnswer> = AppResult.Failure(AppError.Offline)
    val recent = MutableStateFlow<List<AssistantAnswer>>(emptyList())
    var refreshResult: AppResult<Unit> = AppResult.Success(Unit)
    val asks = mutableListOf<AskCall>()
    val movedOn = mutableListOf<String>()
    var questionCalls = 0
    var refreshCalls = 0

    override suspend fun questions(screen: AssistantScreen, plotId: String): AppResult<List<AssistantQuestion>> {
        questionCalls++
        return questions
    }

    override suspend fun ask(questionId: String, plotId: String, requestKey: IdempotencyKey, forceRefresh: Boolean): AppResult<AssistantAnswer> {
        asks += AskCall(questionId, plotId, requestKey, forceRefresh)
        return askResult
    }

    override suspend fun answerToReopen(screen: AssistantScreen, plotId: String): AppResult<AssistantAnswer?> = reopen

    override suspend fun answer(answerId: String): AppResult<AssistantAnswer> = opened

    override fun observeRecent(): Flow<List<AssistantAnswer>> = recent

    override suspend fun refreshRecent(): AppResult<Unit> {
        refreshCalls++
        return refreshResult
    }

    override suspend fun moveOn(answerId: String) {
        movedOn += answerId
    }
}
