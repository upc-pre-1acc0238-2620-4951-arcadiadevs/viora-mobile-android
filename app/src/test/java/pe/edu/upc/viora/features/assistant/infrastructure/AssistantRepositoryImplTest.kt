package pe.edu.upc.viora.features.assistant.infrastructure

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.domain.IdempotencyKey
import pe.edu.upc.viora.core.domain.Uuid
import pe.edu.upc.viora.core.network.ApiCaller
import pe.edu.upc.viora.core.network.ApiErrorMapper
import pe.edu.upc.viora.features.assistant.PLOT_ID
import pe.edu.upc.viora.features.assistant.domain.entity.AssistantScreen
import pe.edu.upc.viora.features.assistant.domain.entity.Freshness
import pe.edu.upc.viora.features.assistant.domain.entity.QuestionIcon
import pe.edu.upc.viora.features.assistant.infrastructure.local.AssistantAnswerDao
import pe.edu.upc.viora.features.assistant.infrastructure.local.AssistantAnswerEntity
import pe.edu.upc.viora.features.assistant.infrastructure.local.DailySummaryDao
import pe.edu.upc.viora.features.assistant.infrastructure.local.DailySummaryEntity
import pe.edu.upc.viora.features.assistant.infrastructure.remote.AnswerHistoryDto
import pe.edu.upc.viora.features.assistant.infrastructure.remote.AskQuestionRequestDto
import pe.edu.upc.viora.features.assistant.infrastructure.remote.AssistantAnswerDto
import pe.edu.upc.viora.features.assistant.infrastructure.remote.AssistantService
import pe.edu.upc.viora.features.assistant.infrastructure.remote.DailySummaryDto
import pe.edu.upc.viora.features.assistant.infrastructure.remote.QuestionCatalogDto
import pe.edu.upc.viora.features.assistant.infrastructure.repository.AssistantRepositoryImpl
import retrofit2.Response
import java.io.IOException
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

private val NOW: Instant = Instant.parse("2026-10-30T15:00:00Z")
private val WIRE = Json { ignoreUnknownKeys = true }

/** The answers table in memory, with the same queries as the DAO. */
private class InMemoryAnswerDao : AssistantAnswerDao {
    val rows = MutableStateFlow<Map<String, AssistantAnswerEntity>>(emptyMap())

    override fun observeRecent(sinceEpochMs: Long, limit: Int): Flow<List<AssistantAnswerEntity>> = rows.map { all ->
        all.values.filter { it.createdAtEpochMs >= sinceEpochMs }.sortedByDescending { it.createdAtEpochMs }.take(limit)
    }

    override suspend fun findById(id: String) = rows.value[id]

    override suspend fun findLatestOfScreen(plotId: String?, prefix: String, sinceEpochMs: Long) = rows.value.values
        .filter { it.plotId == plotId && it.questionId.startsWith(prefix) && it.createdAtEpochMs >= sinceEpochMs }
        .maxByOrNull { it.createdAtEpochMs }

    override suspend fun movedOnAmong(ids: List<String>) = rows.value.values.filter { it.movedOn && it.id in ids }.map { it.id }

    override suspend fun upsertAll(entities: List<AssistantAnswerEntity>) {
        rows.value = rows.value + entities.associateBy { it.id }
    }

    override suspend fun markMovedOn(id: String) {
        rows.value[id]?.let { rows.value = rows.value + (id to it.copy(movedOn = true)) }
    }

    override suspend fun deleteAllExcept(keep: List<String>) {
        rows.value = rows.value.filterKeys { it in keep }
    }

    override suspend fun deleteOlderThan(beforeEpochMs: Long) {
        rows.value = rows.value.filterValues { it.createdAtEpochMs >= beforeEpochMs }
    }

    override suspend fun wipeAll() {
        rows.value = emptyMap()
    }
}

/** The summaries table in memory. */
private class InMemorySummaryDao : DailySummaryDao {
    val rows = mutableMapOf<String, DailySummaryEntity>()

    override suspend fun find(language: String) = rows[language]

    override suspend fun upsert(entity: DailySummaryEntity) {
        rows[entity.language] = entity
    }

    override suspend fun wipeAll() = rows.clear()
}

private class FakeAssistantService : AssistantService {
    var questions: () -> Response<QuestionCatalogDto> = { Response.success(QuestionCatalogDto("ALTERNATION")) }
    var asked: () -> Response<AssistantAnswerDto> = { Response.success(dto()) }
    var latest: () -> Response<AssistantAnswerDto> = { notFound() }
    var history: () -> Response<AnswerHistoryDto> = { Response.success(AnswerHistoryDto()) }
    var single: () -> Response<AssistantAnswerDto> = { notFound() }
    var daily: () -> Response<DailySummaryDto> = { notFound() }
    val requests = mutableListOf<AskQuestionRequestDto>()
    val latestPlots = mutableListOf<String?>()

    override suspend fun getQuestions(screen: String, plotId: String?) = questions()
    override suspend fun ask(request: AskQuestionRequestDto): Response<AssistantAnswerDto> {
        requests += request
        return asked()
    }
    override suspend fun getAnswers(limit: Int) = history()
    override suspend fun getLatest(screen: String, plotId: String?): Response<AssistantAnswerDto> {
        latestPlots += plotId
        return latest()
    }
    override suspend fun getAnswer(answerId: String) = single()
    override suspend fun getDailySummary() = daily()
}

private fun <T> notFound(): Response<T> = Response.error(
    404,
    """{"status":404,"code":"ANSWER_NOT_FOUND"}""".toResponseBody("application/problem+json".toMediaType()),
)

private fun dto(
    id: String = "0b6a5a3e-5c4f-4b8e-9a35-4b7e3e5f1a10",
    questionId: String = "alternation.why_index",
    createdAt: Instant = NOW.minusSeconds(120),
    freshness: String = "UP_TO_DATE",
    facts: Int = 2,
    plotId: String? = PLOT_ID,
) = WIRE.decodeFromString<AssistantAnswerDto>(
    """
    {"answerId":"$id","questionId":"$questionId","question":"¿Por qué mi índice es 0,51?","plotId":${plotId?.let { "\"$it\"" } ?: "null"},
     "createdAt":"$createdAt","dataVersion":"sha256:abc","freshness":"$freshness",
     "text":"Tu índice de vecería es 0,51.",
     "facts":[${(1..facts).joinToString(",") { """{"value":"$it","label":"l$it","pill":"p$it","tone":"${if (it == 1) "PRIMARY" else "HIGHLIGHT"}"}""" }}],
     "sources":[{"label":"Basado en tus 4 campañas registradas"}],
     "actions":[{"type":"DEEPLINK","label":"Ver el plan","target":"thinning/plan?plotId=$PLOT_ID"},{"type":"SHARE","label":"x","target":"y"}],
     "model":{"provider":"GROQ","name":"openai/gpt-oss-20b","fallback":false}}
    """.trimIndent(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class AssistantRepositoryImplTest {

    private val dao = InMemoryAnswerDao()
    private val summaries = InMemorySummaryDao()
    private val service = FakeAssistantService()
    private val json = Json { ignoreUnknownKeys = true }
    private val repository = AssistantRepositoryImpl(
        service = service,
        answerDao = dao,
        summaryDao = summaries,
        apiCaller = ApiCaller(ApiErrorMapper(json), UnconfinedTestDispatcher()),
        json = json,
        clock = Clock.fixed(NOW, ZoneOffset.UTC),
        zone = ZoneOffset.UTC,
    )
    private val key = IdempotencyKey.of(Uuid.parse("00000000-0000-4000-8000-000000000001"))

    @Test
    fun theQuestionsKeepTheServerOrderAndAnUnknownIconFallsBack() = runTest {
        service.questions = {
            Response.success(json.decodeFromString(
                """{"screen":"ALTERNATION","questions":[
                  {"questionId":"alternation.why_index","label":"¿Por qué mi índice es 0,51?","icon":"calendar","enabled":true},
                  {"questionId":"alternation.next_year","label":"¿Cuánto cosecharé en 2027?","icon":"rocket","enabled":false,"disabledReason":"Faltan datos de calibre"}]}""",
            ))
        }

        val questions = (repository.questions(AssistantScreen.ALTERNATION, PLOT_ID) as AppResult.Success).value

        assertEquals(listOf("alternation.why_index", "alternation.next_year"), questions.map { it.id })
        assertEquals(listOf(QuestionIcon.CALENDAR, QuestionIcon.OTHER), questions.map { it.icon })
        assertEquals("Faltan datos de calibre", questions[1].disabledReason)
    }

    @Test
    fun anAnswerIsSentWithItsKeyAndKeptOnThePhone() = runTest {
        val answer = (repository.ask("alternation.why_index", PLOT_ID, key, forceRefresh = true) as AppResult.Success).value

        assertEquals(AskQuestionRequestDto("alternation.why_index", PLOT_ID, key.value, true), service.requests.single())
        assertEquals("¿Por qué mi índice es 0,51?", answer.question)
        // Only the buttons the app can open stay
        assertEquals(listOf("thinning/plan?plotId=$PLOT_ID"), answer.actions.map { it.target })
        assertEquals(listOf(answer), repository.observeRecent().first())
    }

    @Test
    fun anAnswerWithoutItsTwoCardsIsAFailureAndIsNotKept() = runTest {
        service.asked = { Response.success(dto(facts = 3)) }

        val result = repository.ask("alternation.why_index", PLOT_ID, key, forceRefresh = false)

        assertTrue(result is AppResult.Failure)
        assertTrue(dao.rows.value.isEmpty())
    }

    @Test
    fun aScreenWithoutAnswersOpensOnItsQuestions() = runTest {
        assertNull((repository.answerToReopen(AssistantScreen.ALTERNATION, PLOT_ID) as AppResult.Success).value)
    }

    @Test
    fun afterOtraPreguntaTheScreenNoLongerReopensOnTheAnswer() = runTest {
        service.latest = { Response.success(dto()) }
        val reopened = (repository.answerToReopen(AssistantScreen.ALTERNATION, PLOT_ID) as AppResult.Success).value!!

        repository.moveOn(reopened.id)

        assertNull((repository.answerToReopen(AssistantScreen.ALTERNATION, PLOT_ID) as AppResult.Success).value)
    }

    @Test
    fun withoutSignalTheScreenReopensOnTheAnswerKeptOnThePhone() = runTest {
        repository.ask("alternation.why_index", PLOT_ID, key, forceRefresh = false)
        service.latest = { throw IOException("no signal") }

        val reopened = (repository.answerToReopen(AssistantScreen.ALTERNATION, PLOT_ID) as AppResult.Success).value

        assertEquals("0b6a5a3e-5c4f-4b8e-9a35-4b7e3e5f1a10", reopened?.id)
        assertNull((repository.answerToReopen(AssistantScreen.PLAN, PLOT_ID) as AppResult.Success).value)
    }

    @Test
    fun anExpiredPlanIsNotTakenForAMissingAnswer() = runTest {
        service.latest = { Response.error(403, "{}".toResponseBody("application/json".toMediaType())) }

        val result = repository.answerToReopen(AssistantScreen.ALTERNATION, PLOT_ID)

        assertTrue((result as AppResult.Failure).error is AppError.Forbidden)
    }

    @Test
    fun theHistoryOfTheServerReplacesThePhonesAndKeepsTheOtraPreguntaMark() = runTest {
        repository.ask("alternation.why_index", PLOT_ID, key, forceRefresh = false)
        repository.moveOn("0b6a5a3e-5c4f-4b8e-9a35-4b7e3e5f1a10")
        service.asked = { Response.success(dto(id = "9b6a5a3e-5c4f-4b8e-9a35-4b7e3e5f1a19")) }
        repository.ask("alternation.why_index", PLOT_ID, key, forceRefresh = false)
        service.history = { Response.success(AnswerHistoryDto(listOf(dto(freshness = "STALE")))) }

        repository.refreshRecent()

        val kept = dao.rows.value.values.single()
        assertEquals("0b6a5a3e-5c4f-4b8e-9a35-4b7e3e5f1a10", kept.id)
        assertTrue(kept.movedOn)
        assertEquals(Freshness.STALE, repository.observeRecent().first().single().freshness)
    }

    @Test
    fun answersOlderThanTheTenDaysOfHistoryLeaveThePhone() = runTest {
        repository.ask("alternation.why_index", PLOT_ID, key, forceRefresh = false)
        dao.rows.value = dao.rows.value.mapValues { (_, row) ->
            row.copy(createdAtEpochMs = NOW.minus(Duration.ofDays(11)).toEpochMilli())
        }

        assertTrue(repository.observeRecent().first().isEmpty())
        service.asked = { Response.success(dto(id = "9b6a5a3e-5c4f-4b8e-9a35-4b7e3e5f1a19")) }
        repository.ask("alternation.why_index", PLOT_ID, key, forceRefresh = false)
        assertEquals(setOf("9b6a5a3e-5c4f-4b8e-9a35-4b7e3e5f1a19"), dao.rows.value.keys)
    }

    @Test
    fun withoutSignalARecentAnswerOpensFromThePhone() = runTest {
        repository.ask("alternation.why_index", PLOT_ID, key, forceRefresh = false)
        service.single = { throw IOException("no signal") }

        val opened = repository.answer("0b6a5a3e-5c4f-4b8e-9a35-4b7e3e5f1a10")

        assertEquals("Tu índice de vecería es 0,51.", (opened as AppResult.Success).value.text)
    }

    @Test
    fun anInicioQuestionAboutTheWholeFieldGoesWithoutPlotAndReopensWithoutSignal() = runTest {
        service.asked = { Response.success(dto(questionId = "home.week_plan", plotId = null)) }

        val asked = repository.ask("home.week_plan", null, key, forceRefresh = false)

        assertNull(service.requests.single().plotId)
        assertNull((asked as AppResult.Success).value.plotId)
        service.latest = { throw IOException("no signal") }
        val reopened = (repository.answerToReopen(AssistantScreen.HOME, null) as AppResult.Success).value
        assertEquals("home.week_plan", reopened?.questionId)
        assertEquals(listOf<String?>(null), service.latestPlots)
        // The same question about one plot is another answer
        assertNull((repository.answerToReopen(AssistantScreen.HOME, PLOT_ID) as AppResult.Success).value)
    }

    @Test
    fun theSummaryOfTheDayIsKeptAndServedWithoutSignalOnlyWhileItIsToday() = runTest {
        service.daily = { Response.success(summaryDto(createdAt = NOW.minusSeconds(3600))) }

        val fresh = repository.dailySummary()

        assertEquals(listOf("12", "34°", "2"), (fresh as AppResult.Success).value.facts.map { it.value })
        service.daily = { throw IOException("no signal") }
        assertEquals("summary-1", (repository.dailySummary() as AppResult.Success).value.id)

        // Yesterday's summary is never shown as today's
        summaries.rows.replaceAll { _, row -> row.copy(day = "2026-10-29") }
        assertTrue((repository.dailySummary() as AppResult.Failure).error.let { it is AppError.Offline || it is AppError.Timeout })
    }

    @Test
    fun aSummaryWithoutItsThreeCardsIsNotShownAndNoPlotsIsAConflict() = runTest {
        service.daily = { Response.success(summaryDto(cards = 2)) }
        assertTrue(repository.dailySummary() is AppResult.Failure)

        service.daily = { Response.error(409, "{}".toResponseBody("application/json".toMediaType())) }
        assertTrue((repository.dailySummary() as AppResult.Failure).error is AppError.Conflict)
    }
}

private fun summaryDto(createdAt: Instant = NOW, cards: Int = 3) = WIRE.decodeFromString<DailySummaryDto>(
    """
    {"summaryId":"summary-1","questionId":"home.daily_summary","plotId":null,"createdAt":"$createdAt",
     "dataVersion":"sha256:abc","freshness":"UP_TO_DATE","text":"Hoy toca aclarear La Yarada 02.",
     "facts":[${listOf("12" to "días de ventana", "34°" to "máx. jueves", "2" to "alertas activas").take(cards)
        .joinToString(",") { (v, l) -> """{"value":"$v","label":"$l","pill":"p","tone":"PRIMARY"}""" }}],
     "sources":[{"label":"Basado en tus lotes"}],
     "model":{"provider":"GROQ","name":"openai/gpt-oss-20b","fallback":false}}
    """.trimIndent(),
)
