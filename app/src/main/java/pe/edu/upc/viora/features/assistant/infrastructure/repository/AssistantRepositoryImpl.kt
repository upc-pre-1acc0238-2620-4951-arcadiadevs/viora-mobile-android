package pe.edu.upc.viora.features.assistant.infrastructure.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.domain.IdempotencyKey
import pe.edu.upc.viora.core.domain.appResultOf
import pe.edu.upc.viora.core.domain.isConnectivity
import pe.edu.upc.viora.core.network.ApiCaller
import pe.edu.upc.viora.features.assistant.domain.entity.AssistantAnswer
import pe.edu.upc.viora.features.assistant.domain.entity.AssistantQuestion
import pe.edu.upc.viora.features.assistant.domain.entity.AssistantScreen
import pe.edu.upc.viora.features.assistant.domain.repository.AssistantRepository
import pe.edu.upc.viora.features.assistant.infrastructure.local.AssistantAnswerDao
import pe.edu.upc.viora.features.assistant.infrastructure.mapper.toDomain
import pe.edu.upc.viora.features.assistant.infrastructure.mapper.toDomainOrNull
import pe.edu.upc.viora.features.assistant.infrastructure.mapper.toEntity
import pe.edu.upc.viora.features.assistant.infrastructure.remote.AskQuestionRequestDto
import pe.edu.upc.viora.features.assistant.infrastructure.remote.AssistantAnswerDto
import pe.edu.upc.viora.features.assistant.infrastructure.remote.AssistantService
import java.time.Clock
import java.time.Duration
import javax.inject.Inject

/**
 * The server answers; the phone keeps what it answered. Every answer that arrives is saved, keeping
 * the "Otra pregunta" mark the producer already put on it, and the history the server lists
 * replaces the phone's. Without signal the saved answers are what the producer can reopen.
 */
class AssistantRepositoryImpl @Inject constructor(
    private val service: AssistantService,
    private val answerDao: AssistantAnswerDao,
    private val apiCaller: ApiCaller,
    private val json: Json,
    private val clock: Clock,
) : AssistantRepository {

    override suspend fun questions(screen: AssistantScreen, plotId: String): AppResult<List<AssistantQuestion>> =
        when (val result = apiCaller.call { service.getQuestions(screen.name, plotId) }) {
            is AppResult.Success -> AppResult.Success(result.value.questions.map { it.toDomain() })
            is AppResult.Failure -> result
        }

    override suspend fun ask(
        questionId: String,
        plotId: String,
        requestKey: IdempotencyKey,
        forceRefresh: Boolean,
    ): AppResult<AssistantAnswer> {
        val request = AskQuestionRequestDto(questionId, plotId, requestKey.value, forceRefresh)
        return when (val result = apiCaller.call { service.ask(request) }) {
            is AppResult.Success -> save(result.value)
            is AppResult.Failure -> result
        }
    }

    override suspend fun answerToReopen(screen: AssistantScreen, plotId: String): AppResult<AssistantAnswer?> =
        when (val result = apiCaller.call { service.getLatest(screen.name, plotId) }) {
            is AppResult.Success -> when (val saved = save(result.value)) {
                is AppResult.Success -> AppResult.Success(saved.value.takeUnless { isMovedOn(it.id) })
                is AppResult.Failure -> saved
            }
            is AppResult.Failure -> when {
                // No answer on that screen yet: the entry opens on the questions.
                result.error is AppError.NotFound -> AppResult.Success(null)
                result.error.isConnectivity -> AppResult.Success(
                    answerDao.findLatestOfScreen(plotId, screen.questionPrefix, historyStart())
                        ?.takeUnless { it.movedOn }
                        ?.toDomainOrNull(json),
                )
                else -> result
            }
        }

    override suspend fun answer(answerId: String): AppResult<AssistantAnswer> =
        when (val result = apiCaller.call { service.getAnswer(answerId) }) {
            is AppResult.Success -> save(result.value)
            is AppResult.Failure -> {
                val saved = if (result.error.isConnectivity) answerDao.findById(answerId)?.toDomainOrNull(json) else null
                if (saved != null) AppResult.Success(saved) else result
            }
        }

    override fun observeRecent(): Flow<List<AssistantAnswer>> =
        answerDao.observeRecent(historyStart(), HISTORY_SIZE).map { rows -> rows.mapNotNull { it.toDomainOrNull(json) } }

    override suspend fun refreshRecent(): AppResult<Unit> =
        when (val result = apiCaller.call { service.getAnswers(HISTORY_SIZE) }) {
            is AppResult.Success -> appResultOf {
                val answers = result.value.answers.mapNotNull { it.toDomainOrNull() }
                val movedOn = answerDao.movedOnAmong(answers.map { it.id }).toSet()
                answerDao.replaceAll(answers.map { it.toEntity(json, movedOn = it.id in movedOn) })
            }
            is AppResult.Failure -> result
        }

    override suspend fun moveOn(answerId: String) = answerDao.markMovedOn(answerId)

    /** Saves an answer that arrived, keeping its "Otra pregunta" mark, and drops those older than the history. */
    private suspend fun save(dto: AssistantAnswerDto): AppResult<AssistantAnswer> {
        val answer = dto.toDomainOrNull()
            ?: return AppResult.Failure(AppError.Unknown(IllegalStateException("Malformed assistant answer")))
        return appResultOf {
            answerDao.upsertAll(listOf(answer.toEntity(json, movedOn = isMovedOn(answer.id))))
            answerDao.deleteOlderThan(historyStart())
            answer
        }
    }

    private suspend fun isMovedOn(answerId: String): Boolean = answerDao.findById(answerId)?.movedOn == true

    private fun historyStart(): Long = clock.instant().minus(HISTORY).toEpochMilli()

    private companion object {
        /** The server keeps 10 answers for 10 days; the phone keeps the same. */
        const val HISTORY_SIZE = 10
        val HISTORY: Duration = Duration.ofDays(10)
    }
}
